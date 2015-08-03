/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
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
