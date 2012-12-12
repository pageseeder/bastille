/*
 * This file is part of the Bastille library.
 *
 * Available under a commercial licence, contact Weborganic.
 *
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package org.weborganic.bastille.util;

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
   * @return the corresponding byte array or <code>null</code>.
   */
  public static byte[] getResource(String name) {
    byte[] data = null;
    InputStream in = null;
    try {
      ClassLoader loader = Resources.class.getClassLoader();
      in = loader.getResourceAsStream(name);
      data = IOUtils.toByteArray(in);
      in.close();
    } catch (IOException ex) {
      LOGGER.warn("An error occurred while retrieving resource", ex);
    } finally {
      IOUtils.closeQuietly(in);
    }
    return data;
  }

}
