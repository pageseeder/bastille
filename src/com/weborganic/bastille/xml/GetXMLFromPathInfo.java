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
import org.weborganic.berlioz.content.ContentGeneratorBase;
import org.weborganic.berlioz.content.ContentRequest;
import org.weborganic.berlioz.content.Environment;

import com.topologi.diffx.xml.XMLWriter;

/**
 * This generator returns the content of an XML file using the Berlioz servlet path info.
 *
 * <p>For example, if the Berlioz servlet is mapped to '/html/*', 'html/Ping/pong' will try to
 * look for XML file 'Ping/pong.xml' in the XML folder. 
 * 
 * <p>Sample Berlioz config:
 * {@code
 *   <generator class="org.weborganic.berlioz.ext.GetXMLFromPathInfo" name="xml-from-pathinfo" target="main"/>
 * }
 * 
 * <p>To define the location of the XML folder, use the Berlioz config property:
 * <code>berlioz.ext.GetXMLFromPathInfo.folder</code>.
 * 
 * <p>To disable caching of the XML, use the boolean Berlioz config property:
 * <code>berlioz.ext.GetXMLFromPathInfo.cache</code>.
 * 
 * @author Christophe Lauret
 * @version 5 July 2010
 */
public final class GetXMLFromPathInfo extends ContentGeneratorBase implements ContentGenerator, Cacheable {

  /**
   * Logger for debugging
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(GetXMLFromPathInfo.class);

  /**
   * Stores the XML.
   */
  private volatile Cache cache = null;

  /**
   * {@inheritDoc}
   */
  public String getETag(ContentRequest req) {
    File folder = getXMLFolder(req);
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
    File folder = getXMLFolder(req);
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

  /**
   * Returns the XML folder from the specified content request.
   */
  private File getXMLFolder(ContentRequest req) {
    Environment env = req.getEnvironment();
    String folderName = env.getProperty("berlioz.ext.GetXMLFromPathInfo.folder","xml");
    return env.getPrivateFile(folderName);
  }
}
