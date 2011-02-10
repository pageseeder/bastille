package com.weborganic.bastille.xml;

import java.io.File;
import java.io.IOException;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.GlobalSettings;
import org.weborganic.berlioz.content.Cacheable;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentGeneratorBase;
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
 * @version 5 July 2010
 */
public final class GetXMLFromPath extends ContentGeneratorBase implements ContentGenerator, Cacheable {

  /**
   * Logger for debugging
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(GetXMLFromPath.class);

  /**
   * Stores the XML.
   */
  private volatile Cache cache = null;

  /**
   * {@inheritDoc}
   */
  public String getETag(ContentRequest req) {
    File folder = new File(GlobalSettings.getRepository(), "xml");
    String pathInfo = normalise(req.getPathInfo());
    File file = new File(folder, pathInfo+".xml");
    return pathInfo+"__"+file.length()+"x"+file.lastModified();
  }

  /**
   * {@inheritDoc}
   */
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {
    LOGGER.debug(req.getPathInfo());

    // Setup the cache
    if (this.cache == null) this.cache = XMLHelper.initCache();

    // Identify the file
    String pathInfo = normalise(req.getPathInfo());
    File folder = new File(GlobalSettings.getRepository(), req.getParameter("folder", "xml"));
    File file = new File(folder, pathInfo+".xml");

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
    if (pathInfo.endsWith("/")) {
      return pathInfo.substring(0, pathInfo.length()-1);
    }
    return pathInfo;
  }

}
