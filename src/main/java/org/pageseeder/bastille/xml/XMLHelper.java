/*
 * Copyright 2015 Allette Systems (Australia)
 * http://www.allette.com.au
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pageseeder.bastille.xml;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import org.pageseeder.berlioz.content.ContentRequest;
import org.pageseeder.berlioz.xml.XMLCopy;
import org.pageseeder.xmlwriter.XMLWriter;
import org.pageseeder.xmlwriter.XMLWriterImpl;
import org.slf4j.Logger;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

/**
 * Utility class providing useful functions for content generators.
 *
 * <p>Should not be public.
 *
 * @deprecated Will be removed in 0.12
 *
 * @author Christophe Lauret
 * @version 0.6.6 - 27 May 2011
 * @since 0.6.0
 */
@Deprecated
public final class XMLHelper {

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
   * @return the Cache instance.
   */
  public static synchronized Cache initCache() {
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
   * Loads the specified XML file and returns it as a string.
   *
   * @param file   The file to load
   * @param req    The content request to display the file in case of error.
   * @param logger To log info on the correct logger.
   *
   * @return the content of the XML file.
   *
   * @throws IOException If an error occurs while trying to read or write the XML.
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
      // TODO FIX error message
      buffer.writeText("Unable to find file: "+req.getBerliozPath()+".xml");
      logger.info("{} does not exist", file.getAbsolutePath());
    }
    buffer.closeElement();
    buffer.flush();
    return w.toString();
  }

}
