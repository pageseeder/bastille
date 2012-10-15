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

import javax.xml.parsers.ParserConfigurationException;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.content.Cacheable;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentRequest;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import com.topologi.diffx.xml.XMLHelper;
import com.topologi.diffx.xml.XMLStringWriter;
import com.topologi.diffx.xml.XMLWriter;

/**
 * Returns an overview of the folder by Berlioz path.
 *
 * <p>The overview is generated from the first header and the first paragraph from each PageSeeder XML.
 *
 * <h3>Configuration</h3>
 * <p>No configuration required for this generator.</p>
 *
 * <h3>Parameters</h3>
 * <p>No parameters necessary.</p>
 *
 * <h3>Returned XML</h3>
 * <pre>{@code
 *  <overview folder="[folder]">
 *    <entry file="[filename]">
 *      <title>[firsttitle]</title>
 *      <summary>[firstpara]</summary>
 *    </entry>
 *    ...
 *  <overview>
 * }</pre>
 *
 * <h3>Deployment </h3>
 * <pre>{@code
 * <generator class="com.weborganic.bastille.xml.GetFolderOverview" name="overview" target="main" />
 * }</pre>
 *
 * @author Christophe Lauret
 * @version 0.6.35 - 21 May 2012
 * @since 0.6.33
 */
public final class GetContentFolderOverview implements ContentGenerator, Cacheable {

  /**
   * Logger for this generator.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(GetContentFolderOverview.class);

  /**
   * The cache for this generator.
   */
  private volatile Cache cache = null;

  @Override
  public String getETag(ContentRequest req) {
    File dir = getDirectory(req);
    List<File> files = getContents(dir);
    long mostrecent = lastModified(files);
    return normalise(req.getBerliozPath()) + '_' + mostrecent;
  }

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {
    LOGGER.debug(req.getBerliozPath());

    // Get all the files
    String pathInfo = normalise(req.getBerliozPath());
    File dir = getDirectory(req);
    if (dir.exists() && dir.isDirectory()) {
      List<File> files = getContents(dir);
      long modified = lastModified(files);

      // Initialise the cache
      if (this.cache == null) this.cache = newCache();

      Element cached = this.cache.get(pathInfo);
      String data = null;
      if ((cached == null) || (cached.getLastUpdateTime() < modified)) {
        XMLStringWriter buffer = new XMLStringWriter(false);
        processOverview(dir, files, buffer);
        data = buffer.toString();
        this.cache.put(new Element(pathInfo, data));
      } else {
        data = (String)cached.getObjectValue();
      }

      xml.writeXML(data);
    }
  }

  /**
   * Creates a new cache entry for this generator.
   * @return the cache for this generator.
   */
  private Cache newCache()  {
    // Create cache
    CacheManager manager = CacheManager.getInstance();
    // May have been created with another service.
    Cache c = manager.getCache("XMLFolderOverview");
    if (c == null) {
      manager.addCache("XMLFolderOverview");
    }
    return manager.getCache("XMLFolderOverview");
  }

  /**
   * Generate the overview documents for the files.
   *
   * @param dir   The directory to scan.
   * @param files The list of files to process.
   * @param xml   The XML output.
   * @throws IOException Should any error occur.
   */
  private void processOverview(File dir, List<File> files, XMLWriter xml) throws IOException {
    xml.openElement("overview");
    xml.attribute("folder", dir.getName());
    try {
      SummaryGrabber grabber = new SummaryGrabber();
      XMLReader reader = XMLHelper.makeXMLReader(grabber);
      for (File f : files) {
        XMLHelper.parse(reader, f);
        xml.openElement("entry");
        xml.attribute("name", f.getName());
        String title = grabber.getTitle();
        if (title != null)
          xml.element("title", title);
        String summary = grabber.getSummary();
        if (summary != null)
          xml.element("summary", summary);
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
   * @param req the content request.
   * @return the list of XML files in the directory.
   */
  private static File getDirectory(ContentRequest req) {
    PSMLFile dir = PSMLConfig.getContentFolder(req.getBerliozPath());
    return dir.file();
  }

  /**
   * Returns the list of XML files in the specified directory.
   *
   * @param dir the directory
   * @return the list of XML files in the directory.
   */
  private static List<File> getContents(File dir) {
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
   * Removes any trailing '/' from the path info
   *
   * @param pathInfo the path info
   * @return the normalised path info.
   */
  private static String normalise(String pathInfo) {
    if ((pathInfo != null) && (pathInfo.endsWith("/"))) {
      return pathInfo.substring(0, pathInfo.length() - 1);
    }
    return pathInfo;
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
   *
   * @author Christophe Lauret
   * @version 14 May 2012
   */
  private static class SummaryGrabber extends DefaultHandler {

    /**
     * Whether to skip the section.
     */
    private boolean skipSection = false;

    /**
     * The internal buffer.
     */
    private StringBuilder buffer = null;

    /**
     * The internal buffer.
     */
    private String title = null;

    /**
     * The internal buffer.
     */
    private String summary = null;

    @Override
    public void startDocument() throws SAXException {
      this.title = null;
      this.summary = null;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
      if (!this.skipSection) {
        if (this.title == null && "heading1".equals(qName)) {
          this.buffer = new StringBuilder();
        } else if (this.summary == null && "para".equals(qName)) {
          this.buffer = new StringBuilder();
        }
      }
      if ("section".equals(qName)) {
        this.skipSection = attributes.getValue("format") != null;
      }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
      if (!this.skipSection) {
        if (this.title == null && "heading1".equals(qName)) {
          this.title = this.buffer.toString();
          this.buffer = null;
        } else if (this.summary == null && "para".equals(qName)) {
          this.summary = this.buffer.toString();
          this.buffer = null;
        }
      } else if ("section".equals(qName)) {
        this.skipSection = false;
      }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
      if (this.buffer != null)
        this.buffer.append(ch, start, length);
    }

    /**
     * @return the title (content of first <code>heading1</code> element).
     */
    public String getTitle() {
      return this.title;
    }

    /**
     * @return the summary of the file (content of first <code>para</code> element).
     */
    public String getSummary() {
      return this.summary;
    }
  }

}
