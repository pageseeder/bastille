/*
 * This file is part of the Bastille library.
 *
 * Available under a commercial licence, contact Weborganic.
 *
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package com.weborganic.bastille.psml;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weborganic.berlioz.BerliozException;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.topologi.diffx.xml.XMLHelper;
import com.topologi.diffx.xml.XMLStringWriter;
import com.topologi.diffx.xml.XMLWriter;

/**
 * A utility class to generate the overview data.
 *
 * @author Christophe Lauret
 * @version 0.6.35 - 21 May 2012
 * @since 0.6.33
 */
public final class PSMLOverviews {

  /**
   * Logger for this generator.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(PSMLOverviews.class);

  /**
   * Utility class.
   */
  private PSMLOverviews() {
  }

  /**
   * Name of the cache.
   */
  public static final String CACHE_NAME = "PSMLOverview";

  /**
   * Reuse the same cache manager to avoid I/O problems (configuration seems to be parsed for each getInstance).
   */
  private static volatile CacheManager manager = null;

  /**
   * The cache containing all the PSML entries.
   */
  private static volatile Ehcache cache = null;

  /**
   * Generate the overview documents for the files for the specified folder.
   *
   * @param folder The directory to scan.
   *
   * @return the XML content as a string.
   *
   * @throws BerliozException If an error occurs during parsing.
   * @throws IOException      Should any error occur.
   */
  public static String getOverview(PSMLFile folder) throws BerliozException, IOException {
    // Get all the files
    File dir = folder.file();
    if (dir.exists() && dir.isDirectory()) {
      List<File> files = getContents(dir);
      long modified = lastModified(files);

      // Initialise if required.
      if (cache == null) { init(); }

      // Attempt to grab the content
      Element cached = cache.get(folder.path());
      String data = null;
      if (cached == null || cached.getLastUpdateTime() < modified) {
        XMLStringWriter buffer = new XMLStringWriter(false);
        processOverview(folder, files, buffer);
        data = buffer.toString();
        cache.put(new Element(folder.path(), data));
      } else {
        data = (String)cached.getObjectValue();
      }
      return data;
    }
    return "<no-overview/>";
  }

  /**
   * Generate the overview documents for the files.
   *
   * @param folder The directory to scan.
   * @param files  The list of files to process.
   * @param xml    The XML output.
   *
   * @throws IOException Should any error occur.
   */
  private static void processOverview(PSMLFile folder, List<File> files, XMLWriter xml) throws IOException {
    xml.openElement("overview");
    File dir = folder.file();
    xml.attribute("folder", dir.getName());

    // Compute the base directory
    String base = folder.path();
    base = "/" + base.substring(0, base.length() - dir.getName().length());
    xml.attribute("base", base);

    try {
      PSMLOverviewHandler handler = new PSMLOverviewHandler();
      XMLReader reader = XMLHelper.makeXMLReader(handler);
      for (File f : files) {
        xml.openElement("entry");
        xml.attribute("name", f.getName());

        try {
          // Parse and make it resist bad files...
          XMLHelper.parse(reader, f);

          // Grab the title and summary
          String title = handler.getTitle();
          if (title != null)
            xml.element("title", title);
          String summary = handler.getSummary();
          if (summary != null)
            xml.element("summary", summary);

          // Return the properties we could find
          for (Entry<String, String> property : handler.getProperties().entrySet()) {
            xml.openElement("property");
            xml.attribute("name", property.getKey());
            xml.attribute("value", property.getValue());
            xml.closeElement();
          }

        } catch (SAXException ex) {
          LOGGER.warn("Unparseable file found: {}", f.getName());
          xml.attribute("error", "unparsable");
        }

        xml.closeElement();
      }
    } catch (ParserConfigurationException ex) {
      throw new IOException(ex);
    } catch (SAXException ex) {
      throw new IOException(ex);
    }
    xml.closeElement();
  }

  /**
   * Returns the list of XML files in the specified directory.
   *
   * @param dir the directory
   * @return the list of XML files in the directory.
   */
  public static List<File> getContents(File dir) {
    List<File> files = null;
    if (dir.exists() && dir.isDirectory()) {
      File[] subs = dir.listFiles(new PSMLFileFilter());
      files = Arrays.asList(subs);
    } else {
      files = Collections.emptyList();
    }
    return files;
  }

  /**
   * @param files the list of files to check
   * @return the date of the last modified file in the list.
   */
  public static long lastModified(List<File> files) {
    long mostrecent = 0;
    for (File f : files) {
      long date = f.lastModified();
      if (date > mostrecent) mostrecent = date;
    }
    return mostrecent;
  }

  /**
   * Initialises the cache for PSML files.
   */
  private static synchronized void init() {
    // Create cache
    if (manager == null) {
      LOGGER.info("Initialising cache for PSML overview data");
      manager = CacheManager.getInstance();
      // May have been created with another service.
      cache = manager.getEhcache(CACHE_NAME);
      if (cache == null) {
        LOGGER.warn("No cache exists for PSML overview data! Create a new cache entry for {} in your cache config!", CACHE_NAME);
        manager.addCache(CACHE_NAME);
        LOGGER.info("Created new cache named {} to store PSML data", CACHE_NAME);
        cache = manager.getEhcache(CACHE_NAME);
      }
    }
  }

}
