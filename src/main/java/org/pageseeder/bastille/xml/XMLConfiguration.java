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

import org.pageseeder.berlioz.content.ContentRequest;
import org.pageseeder.berlioz.content.Environment;


/**
 * Centralises the configuration options for this package.
 *
 * <p>This class is used to ensure that the same configuration options are used by all generators
 * in this package.
 *
 * @deprecated Will be removed in 0.12
 *
 * @author Christophe Lauret
 * @version 0.6.8 - 8 June 2011
 * @since 0.6.1
 */
@Deprecated
public final class XMLConfiguration {

  /** Utility class */
  private XMLConfiguration() {
  }

  /**
   * The key for the location of the XML path.
   */
  public static final String BASTILLE_XML_ROOT = "bastille.xml.root";

  /**
   * The key for the extension used by XML files.
   */
  public static final String BASTILLE_XML_EXTENSION = "bastille.xml.extension";

  /**
   * Returns the XML Root folder used by generators in this package.
   *
   * <p>The XML root folder can be defined in the <code>config-[mode].xml</code> using the key
   * {@value BASTILLE_XML_ROOT}.
   *
   * @param req The content request.
   * @return the XML Root folder as defined in the configuration or "xml" if undefined.
   */
  public static File getXMLRootFolder(ContentRequest req) {
    Environment env = req.getEnvironment();
    String root = env.getProperty(BASTILLE_XML_ROOT, "xml");
    return env.getPrivateFile(root);
  }

  /**
   * Returns the XML extension used by generators in this package.
   *
   * <p>The XML extension can be defined in the <code>config-[mode].xml</code> using the key
   * {@value BASTILLE_XML_EXTENSION}.
   *
   * @param req The content request.
   * @return the XML extension as defined in the configuration or ".xml" if undefined.
   */
  public static String getXMLExtension(ContentRequest req) {
    Environment env = req.getEnvironment();
    return env.getProperty(BASTILLE_XML_EXTENSION, ".xml");
  }

}
