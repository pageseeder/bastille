/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.weborganic.bastille.xml;

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
 * This generator returns the content of an XML file from the specified path.
 *
 * <p>Sample Berlioz config:
 * {@code
 *   <generator class="org.weborganic.berlioz.ext.GetXMLFromPath" name="xml-from-path" target="main">
 *     <parameter name="path" source="uri" value="path"/>
 *   </generator>
 * }
 *
 *
 * @author Christophe Lauret
 * @version 0.6.8 - 29 June 2011
 * @since 0.6.0
 */
public final class GetXMLFromPath implements ContentGenerator, Cacheable {

  /**
   * Logger for debugging
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(GetXMLFromPath.class);

  /**
   * Stores the XML.
   */
  private volatile Cache cache = null;

  @Override
  public String getETag(ContentRequest req) {
    String path = req.getParameter("path");
    if (path == null) return null;
    File folder = XMLConfiguration.getXMLRootFolder(req);
    String ext = XMLConfiguration.getXMLExtension(req);
    File file = new File(folder, path + ext);
    return path+"__"+file.length()+"x"+file.lastModified();
  }

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {
    String path = req.getParameter("path");
    if (path == null) throw new BerliozException("Path parameter is missing");

    // Setup the cache
    if (this.cache == null) this.cache = XMLHelper.initCache();

    // Identify the file
    path = normalise(path);
    File folder = XMLConfiguration.getXMLRootFolder(req);
    String ext = XMLConfiguration.getXMLExtension(req);
    File file = new File(folder, path + ext);

    // Grab the data
    Element cached = this.cache.get(path);
    String data = null;
    if (cached == null || cached.getLastUpdateTime() < file.lastModified()) {
      data = XMLHelper.load(file, req, LOGGER);
      this.cache.put(new Element(path, data));
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
    if (pathInfo.endsWith("/")) {
      return pathInfo.substring(0, pathInfo.length()-1);
    }
    return pathInfo;
  }

}
