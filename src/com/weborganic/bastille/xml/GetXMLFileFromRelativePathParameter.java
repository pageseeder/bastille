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
import org.weborganic.berlioz.util.MD5;

import com.topologi.diffx.xml.XMLWriter;

/**
 * This generator returns the static content by providing relative path parameter.
 *
 * <p>Only return the content under the website root folder, by default ( WEB-INF/xml ).</p>
 *
 * <h3>Parameter</h3>
 * <ul>
 * <li><code>relative-path</code> defines the request relative path from the berlioz website root.</li>
 * </ul>
 * <p>Use the element <code>parameter</code> to define <code>relative-path</code> in server.xml. </p>
 *
 * <p>Sample Berlioz config:</p>
 * <pre> {@code
 *   <generator class="com.weborganic.bastille.xml.GetXMLFileFromRelativePathParameter" name="navigation" target="navigation">
 *     <parameter name="relative-path" value="config/navigation.xml" />
 *   </generator>
 * } </pre>
 *
 *
 * @author Christophe Lauret
 * @author Ciber Cai
 * @version 8 July 2011
 */
public final class GetXMLFileFromRelativePathParameter implements ContentGenerator, Cacheable {

  /**
   * Logger for debugging
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(GetXMLFileFromRelativePathParameter.class);

  /**
   * Stores the XML.
   */
  private volatile Cache cache = null;

  @Override
  public String getETag(ContentRequest req) {
    StringBuilder etag = new StringBuilder();
    etag.append(req.getParameter("relative-path", "")).append("%");
    etag.append(XMLConfiguration.getXMLRootFolder(req)).append("%");
    File file = new File(XMLConfiguration.getXMLRootFolder(req), addXMLExtension(req.getParameter("relative-path", "")));
    if (file != null && file.exists()) {
      etag.append(file.length()).append("%");
      etag.append(file.lastModified()).append("%");
    } else {
      etag.append("not-found").append("%");
    }
    return MD5.hash(etag.toString());

  }

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {

    String relativepath = addXMLExtension(req.getParameter("relative-path", ""));
    File rootfolder = XMLConfiguration.getXMLRootFolder(req);

    LOGGER.debug("relative path  {} ", relativepath);
    LOGGER.debug("root folder {} ", rootfolder);

    // Setup the cache
    if (this.cache == null) {
      this.cache = XMLHelper.initCache();
    }

    Element cached = this.cache.get(getETag(req));
    String data = null;

    // only process if the variable relativepath is not empty.
    if (!relativepath.isEmpty()) {
      File reqfile = new File(rootfolder, relativepath);
      LOGGER.debug("request file {} ", reqfile);

      if (cached == null || cached.getLastUpdateTime() < reqfile.lastModified()) {
        data = XMLHelper.load(reqfile, req, LOGGER);
        this.cache.put(new Element(reqfile.getAbsolutePath(), data));
      } else {
        data = (String) cached.getObjectValue();
      }
    }

    // Write on the output
    xml.writeXML(data);
  }

  // private functions
  // ----------------------------------------------------------------------------------------------

  /***
   * Add the extension ".xml" to request value.
   * @param loc defines the request location file.
   * @return path somewhat normalised
   */
  public static String addXMLExtension(String loc) {

    // Return empty
    // if empty
    // or if request tries to reach parent location.

    if (loc == null || loc.isEmpty() || loc.contains("..")) {
      return "";
    }

    // Add .xml extension
    if (loc != null && loc.toLowerCase().endsWith(".xml")) {
      return loc.substring(0, loc.lastIndexOf(".xml")) + ".xml";
    } else {
      return loc + ".xml";
    }
  }

}
