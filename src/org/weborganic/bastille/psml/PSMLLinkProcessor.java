/*
 * This file is part of the Bastille library.
 *
 * Available under a commercial licence, contact Weborganic.
 *
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package org.weborganic.bastille.psml;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

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
 * A utility class to process links in PSML data.
 *
 * @author Christophe Lauret
 * @version 25 November 2012
 */
public final class PSMLLinkProcessor {

  /**
   * Logger for this generator.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(PSMLLinkProcessor.class);

  /**
   * Utility class.
   */
  private PSMLLinkProcessor() {
  }

  /**
   * Name of the cache.
   */
  public static final String CACHE_NAME = "PSMLProcessed";

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
   * @param psml The PSML file to process.
   *
   * @return the XML content as a string.
   *
   * @throws BerliozException If an error occurs during parsing.
   * @throws IOException      Should any error occur.
   */
  public static String process(PSMLFile psml) throws BerliozException, IOException {
    // Get all the files
    File file = psml.file();
    if (file.exists() && !file.isDirectory()) {
      // Initialize if required.
      if (cache == null) { init(); }

      // Attempt to grab the content
      Element cached = cache.get(psml.path());
      CachedProcessed entry = cached != null? (CachedProcessed)cached.getObjectValue() : null;

      // Check for freshness
      long modified = System.currentTimeMillis();
      if (entry != null) {
        modified = lastModified(entry._linked);
      }

      // Attempt to grab the content
      String data = null;
      if (cached == null || cached.getLastUpdateTime() < modified) {

        // Process
        XMLStringWriter xml = new XMLStringWriter(false);
        try {

          xml.openElement("psml-file");
          xml.attribute("name", file.getName());
          xml.attribute("base", psml.getBase());
          xml.attribute("status", "ok");
          List<File> linked = processLinks(psml, xml);
          xml.closeElement();
          xml.flush();

          // Cache
          data = xml.toString();
          entry = new CachedProcessed(data, linked);
          cache.put(new Element(psml.path(), entry));

        } catch (IOException ex) {

          xml = new XMLStringWriter(false);
          xml.openElement("psml-file");
          xml.attribute("name", file.getName());
          xml.attribute("base", psml.getBase());
          xml.attribute("status", "error");
          xml.writeComment(ex.getMessage());
          xml.closeElement();
          xml.flush();
          data = xml.toString();
        }

      } else {
        data = entry.data();
      }
      return data;
    }
    // Will return the standard PSML
    return PSMLConfig.load(psml);
  }

  /**
   * Returns the etag for the PSML file based on the last modified date of the file and all transcluded documents.
   *
   * @param psml the PSML file.
   * @return The corresponding etag or <code>null</code> if the file does not exist.
   *
   * @throws NullPointerException if the file is <code>null</code>.
   */
  public static String getEtag(PSMLFile psml) {
    // Get all the files
    File file = psml.file();
    if (!file.exists()) return null;
    // No cache yet, return the last modified date of file
    long modified = file.lastModified();
    if (cache != null) {
      // Attempt to grab the content
      Element cached = cache.get(psml.path());
      if (cached != null) {
        CachedProcessed entry = (CachedProcessed)cached.getObjectValue();
        if (entry != null) {
          modified = lastModified(entry.linked());
        }
      }
    }
    return Long.toString(modified);
  }

  /**
   * Generate the overview documents for the files.
   *
   * @param source The source PSML file.
   * @param xml    The XML output.
   *
   * @return the list of processed links
   *
   * @throws IOException Should any error occur.
   */
  private static List<File> processLinks(PSMLFile source, XMLWriter xml) throws IOException {
    PSMLLinkProcessorHandler handler = new PSMLLinkProcessorHandler(source, xml);
    return processLinks(source, handler);
  }

  /**
   * Generate the overview documents for the files.
   *
   * @param source  The source PSML file.
   * @param handler The XML output.
   *
   * @return the list of processed links
   *
   * @throws IOException Should any error occur.
   */
  static List<File> processLinks(PSMLFile source, PSMLLinkProcessorHandler handler) throws IOException {
    try {
      XMLReader reader = XMLHelper.makeXMLReader(handler);
      try {
        XMLHelper.parse(reader, source.file());
      } catch (SAXException ex) {
        LOGGER.warn("Unparseable file found: {}", source.file().getName(), ex.getMessage());
      }
    } catch (ParserConfigurationException ex) {
      throw new IOException(ex);
    } catch (SAXException ex) {
      throw new IOException(ex);
    }
    return handler.getLinks();
  }

  /**
   * @param files the list of files to check
   * @return the date of the last modified file in the list.
   */
  private static long lastModified(List<File> files) {
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
      LOGGER.info("Initialising cache for PSML processed data");
      manager = CacheManager.getInstance();
      // May have been created with another service.
      cache = manager.getEhcache(CACHE_NAME);
      if (cache == null) {
        LOGGER.warn("No cache exists for PSML processed data! Create a new cache entry for {} in your cache config!", CACHE_NAME);
        manager.addCache(CACHE_NAME);
        LOGGER.info("Created new cache named {} to store PSML data", CACHE_NAME);
        cache = manager.getEhcache(CACHE_NAME);
      }
    }
  }

  /**
   * A cached processed PSML.
   *
   * @author Christophe Lauret
   * @version 21 November 2012
   */
  private static final class CachedProcessed implements Serializable {

    /**
     * As per requirement by the <code>Serializable</code> interface.
     */
    private static final long serialVersionUID = -688073404317798992L;

    /**
     * The data to store.
     */
    private final String _data;

    /**
     * The list
     */
    private final List<File> _linked;

    /**
     * @param data the XML data.
     * @param linked the list of files linked.
     */
    public CachedProcessed(String data, List<File> linked) {
      this._data = data;
      this._linked = linked;
    }

    /**
     * @return the data to store.
     */
    public String data() {
      return this._data;
    }

    /**
     * @return the linked files.
     */
    public List<File> linked() {
      return this._linked;
    }
  }

}
