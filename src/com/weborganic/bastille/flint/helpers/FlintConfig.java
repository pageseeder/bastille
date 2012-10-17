/*
 * This file is part of the Bastille library.
 *
 * Available under a commercial licence, contact Weborganic.
 *
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package com.weborganic.bastille.flint.helpers;

import java.io.File;

import org.weborganic.berlioz.GlobalSettings;

/**
 * Centralizes the configuration for flint.
 *
 * @author Christophe Lauret
 * @version 17 October 2012
 */
public final class FlintConfig {

  /**
   * The name of the property to define where the index should be located.
   */
  private static final String DEFAULT_INDEX_CONFIG = "index";

  /**
   * The name of the property to define where the ixml shoudl be located.
   */
  private static final String DEFAULT_ITEMPLATES_CONFIG = "ixml";

  /**
   * The default folder name for the index files.
   */
  protected static final String DEFAULT_INDEX_LOCATION = "index";

  /**
   * The default templates to use for processing the data and generate the indexable XML.
   */
  protected static final String DEFAULT_ITEMPLATES_LOCATION = "ixml/default.xsl";

  /**
   * Where the index is located.
   */
  private static volatile File directory = null;

  /**
   * Where the itemplates are.
   */
  private static volatile File itemplates = null;

  /**
   * Utility class.
   */
  private FlintConfig() {
  }

  /**
   * Initialize the Flint from the global configuration.
   */
  protected static synchronized void init() {
    // Location of the index
    if (directory == null) {
      File dir = GlobalSettings.getDirProperty(DEFAULT_INDEX_CONFIG);
      if (dir == null) {
        dir = new File(GlobalSettings.getRepository(), DEFAULT_INDEX_LOCATION);
      }
      directory = dir;
    }
    // Location of the itemplates
    if (itemplates == null) {
      File ixml = GlobalSettings.getDirProperty(DEFAULT_ITEMPLATES_CONFIG);
      if (ixml == null) {
        ixml = new File(GlobalSettings.getRepository(), DEFAULT_ITEMPLATES_LOCATION);
      }
      itemplates = ixml;
    }
  }

  /**
   * @return the default location of the index.
   */
  public static File directory() {
    if (directory == null) init();
    return directory;
  }

  /**
   * @return the itemplates to process the data.
   */
  public static File itemplates() {
    if (itemplates == null) init();
    return itemplates;
  }
}
