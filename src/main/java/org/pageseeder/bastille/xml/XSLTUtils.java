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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Hashtable;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.pageseeder.berlioz.BerliozException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class to help with XSLT operations.
 *
 * @deprecated Will be removed in 0.12
 *
 * @author Christophe Lauret
 * @version 0.6.3 - 3 May 2013
 * @since 0.6.0
 */
@Deprecated
public final class XSLTUtils {

  /** Logger for this class */
  private static final Logger LOGGER = LoggerFactory.getLogger(XSLTUtils.class);

  /**
   * Maps XSLT templates to their URL as a string for easy retrieval.
   */
  private static final Map<String, Templates> CACHE = new Hashtable<>();

  /** Utility class. */
  private XSLTUtils() {
  }

  /**
   * Return the XSLT templates from the given style.
   *
   * @param url A URL to a template.
   *
   * @return the corresponding XSLT templates object or <code>null</code> if the URL was <code>null</code>.
   *
   * @throws BerliozException If XSLT templates could not be loaded from the specified URL.
   */
  public static Templates getTemplates(URL url) throws BerliozException {
    if (url == null) return null;
    // load the templates from the source file
    InputStream in = null;
    Templates templates = null;
    try {
      LOGGER.debug("Loading templates from URL: {}", url);
      in = url.openStream();
      Source source = new StreamSource(in);
      source.setSystemId(url.toString());
      TransformerFactory factory = TransformerFactory.newInstance();
      templates = factory.newTemplates(source);
    } catch (TransformerConfigurationException ex) {
      LOGGER.error("Transformer exception: {}", ex.getMessageAndLocation(), ex);
      throw new BerliozException("Transformer exception while trying to load XSLT templates"+ url, ex);
    } catch (IOException ex) {
      LOGGER.error("IO error while trying to load template: {}", url);
      throw new BerliozException("IO error while trying to load XSLT templates"+ url, ex);
    } finally {
      IOUtils.closeQuietly(in);
    }
    return templates;
  }

  /**
   * Return the XSLT templates from the given style.
   *
   * @param url   A URL to a template.
   * @param cache <code>true</code> to load and store from the cache or not;
   *              <code>false</code> to force load from the URL and clear the cache entry for this URL.
   *
   * @return the corresponding XSLT templates object or <code>null</code> if the URL was <code>null</code>.
   *
   * @throws BerliozException If XSLT templates could not be loaded from the specified URL.
   */
  public static Templates getTemplates(URL url, boolean cache) throws BerliozException {
    if (url == null) return null;
    Templates templates = cache? CACHE.get(url.toString()) : null;
    if (templates == null) {
      templates = getTemplates(url);
      if (cache) {
        CACHE.put(url.toString(), templates);
      } else {
        CACHE.remove(url.toString());
      }
    }
    return templates;
  }

  /**
   * Return the XSLT templates from the given style.
   *
   * <p>This method will first try to load the resource using the class loader used for this class.
   *
   * <p>Use this class to load XSLT from the system.
   *
   * @param resource The path to a resource.
   *
   * @return the corresponding XSLT templates object;
   *         or <code>null</code> if the resource could not be found.
   *
   * @throws BerliozException If the loading fails.
   */
  public static Templates getTemplatesFromResource(String resource) throws BerliozException {
    ClassLoader loader = XSLTUtils.class.getClassLoader();
    URL url = loader.getResource(resource);
    return getTemplates(url);
  }

  /**
   * Return the XSLT templates from the given style.
   *
   * <p>This method will first try to load the resource using the class loader used for this class.
   *
   * <p>Use this class to load XSLT from the system.
   *
   * @param resource The path to a resource.
   * @param cache <code>true</code> to load and store from the cache or not;
   *              <code>false</code> to load from the URL and clear the cache entry for this resource.
   *
   * @return the corresponding XSLT templates object;
   *         or <code>null</code> if the resource could not be found.
   *
   * @throws BerliozException If the loading fails.
   */
  public static Templates getTemplatesFromResource(String resource, boolean cache) throws BerliozException {
    if (resource == null) return null;
    Templates templates = cache? CACHE.get(resource) : null;
    if (templates == null) {
      templates = getTemplatesFromResource(resource);
      if (cache) {
        CACHE.put(resource, templates);
      } else {
        CACHE.remove(resource);
      }
    }
    return templates;
  }

  /**
   * Clears the internal XSLT cache.
   */
  public void clearCache() {
    LOGGER.debug("Clearing XSLT cache.");
    CACHE.clear();
  }

}
