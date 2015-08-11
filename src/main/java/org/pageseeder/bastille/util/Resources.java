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
package org.pageseeder.bastille.util;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class to return resources.
 *
 * @author Christophe Lauret
 */
public final class Resources {

  /**
   * A logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(Resources.class);

  /** Utility class. */
  private Resources() {
  }

  /**
   * Returns the requested resource or <code>null</code>.
   *
   * @param name the name of the resource to retrieve.
   *
   * @return the corresponding byte array or <code>null</code> if not found or I/O error occurs.
   */
  public static byte[] getResource(String name) {
    byte[] data = null;
    InputStream in = null;
    try {
      ClassLoader loader = Resources.class.getClassLoader();
      in = loader.getResourceAsStream(name);
      if (in != null) {
        data = IOUtils.toByteArray(in);
      }
    } catch (IOException ex) {
      LOGGER.warn("An error occurred while retrieving resource", ex);
    } finally {
      IOUtils.closeQuietly(in);
    }
    return data;
  }

}
