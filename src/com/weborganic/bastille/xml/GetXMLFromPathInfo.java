/*
 * This file is part of the Bastille library.
 *
 * Available under a commercial licence, contact Weborganic.
 *
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package com.weborganic.bastille.xml;

import java.io.File;
import java.io.IOException;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.content.Cacheable;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentRequest;

import com.topologi.diffx.xml.XMLWriter;

/**
 * This generator returns the content of an XML file using the Berlioz servlet path info.
 *
 * <p>For example, if the Berlioz servlet is mapped to '/html/*', 'html/Ping/pong' will try to
 * look for XML file 'Ping/pong.xml' in the XML folder.
 *
 * <h3>Configuration</h3>
 * <p>The root XML folder can be configured globally using the Berlioz configuration:
 * <p>For example:
 * <pre>{@code
 * <node name="bastille">
 *   <map/>
 *   <node name="xml">
 *     <map>
 *       <entry key="root"     value="xml/content"/>
 *     </map>
 *   </node>
 * </node>
 * }</pre>
 *
 * <p>To define the location of the XML folder, use the Berlioz config property:
 * <code>bastille.xml.root</code>.
 *
 * @author Christophe Lauret
 * @version 0.6.0 - 5 July 2010
 * @since 0.6.0
 */
public final class GetXMLFromPathInfo implements ContentGenerator, Cacheable {

  /**
   * Logger for debugging
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(GetXMLFromPathInfo.class);

  /**
   * Stores the XML.
   */
  private volatile Cache cache = null;

  @Override
  public String getETag(ContentRequest req) {
    File folder = XMLConfiguration.getXMLRootFolder(req);
    String ext = XMLConfiguration.getXMLExtension(req);
    String pathInfo = normalise(req.getPathInfo());
    File file = new File(folder, pathInfo + ext);
    return pathInfo+"__"+file.length()+"x"+file.lastModified();
  }

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {
    LOGGER.debug(req.getPathInfo());

    // Setup the cache
    if (this.cache == null) this.cache = XMLHelper.initCache();

    // Identify the file
    String pathInfo = normalise(req.getPathInfo());
    File folder = XMLConfiguration.getXMLRootFolder(req);
    String ext = XMLConfiguration.getXMLExtension(req);
    File file = new File(folder, pathInfo + ext);

    // Grab the data
    Element cached = this.cache.get(pathInfo);
    String data = null;
    if (cached == null || cached.getLastUpdateTime() < file.lastModified()) {
      data = XMLHelper.load(file, req, LOGGER);
      this.cache.put(new Element(pathInfo, data));
    } else {
      data = (String)cached.getObjectValue();
    }

    // Write on the output
    xml.writeXML(data);
  }

  // private helpers
  // ----------------------------------------------------------------------------------------------

  /**
   * Filters and normalises the value in the path informations.
   */
  private String normalise(String pathInfo) {
    if (pathInfo != null && pathInfo.endsWith("/")) {
      return pathInfo.substring(0, pathInfo.length()-1);
    }
    return pathInfo;
  }

}
