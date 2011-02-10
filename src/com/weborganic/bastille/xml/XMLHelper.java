package com.weborganic.bastille.xml;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import org.slf4j.Logger;
import org.weborganic.berlioz.content.ContentRequest;
import org.weborganic.berlioz.xml.XMLCopy;

import com.topologi.diffx.xml.XMLWriter;
import com.topologi.diffx.xml.XMLWriterImpl;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

/**
 * Utility class providing useful functions for content generators.
 * 
 * <p>Should not be public.
 * 
 * @author Christophe Lauret
 * @version 5 July 2010 
 */
public class XMLHelper {

  /**
   * Name of the cache.
   */
  public static final String CACHE_NAME = "XMLFileContent";

  /**
   * No constructor. 
   */
  private XMLHelper() {
  }

  /**
   * Initialises the cache.
   */
  public static final synchronized Cache initCache() {
    // Create cache
    CacheManager manager = CacheManager.getInstance();
    // May have been created with another service.
    Cache cache = manager.getCache(CACHE_NAME);
    if (cache == null) {
      manager.addCache(CACHE_NAME);
    }
    return manager.getCache(CACHE_NAME);
  }

  /**
   * 
   */
  public static String load(File file, ContentRequest req, Logger logger) throws IOException {
    StringWriter w = new StringWriter();
    XMLWriter buffer = new XMLWriterImpl(w);

    buffer.openElement("content-file");
    buffer.attribute("name", file.getName());

    // All good, print to the XML stream
    if (file.exists()) {
      buffer.attribute("status", "ok");
      XMLCopy.copyTo(file, buffer);
      logger.info("loaded {}", file.getAbsolutePath());

    // The requested could not be found 
    } else {
      buffer.attribute("status", "not-found");
      buffer.writeText("Unable to find file: "+req.getPathInfo());
      logger.info("{} does not exist", file.getAbsolutePath());
    }
    buffer.closeElement();
    buffer.flush();
    return w.toString();
  }

}
