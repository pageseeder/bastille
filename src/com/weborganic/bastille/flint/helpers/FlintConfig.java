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

  // Constants for the configuration
  // ----------------------------------------------------------------------------------------------

  /**
   * The default folder name for the index files.
   */
  protected static final String DEFAULT_INDEX_LOCATION = "index";

  /**
   * The default templates to use for processing the data and generate the indexable XML.
   */
  protected static final String DEFAULT_ITEMPLATES_LOCATION = "ixml/default.xsl";

  // Configuration that require initialisation
  // ----------------------------------------------------------------------------------------------

  /**
   * Whether to operate in legacy mode.
   */
  private static volatile boolean _legacy = false;

  /**
   * Whether multiple indexes are in use.
   */
  private static volatile boolean _isMultiple = false;

  /**
   * Where the index is located.
   */
  private static volatile File _directory = null;

  /**
   * Where the itemplates are.
   */
  private static volatile File _itemplates = null;

  /**
   * Utility class.
   */
  private FlintConfig() {
  }

  /**
   * Initialize the Flint from the global configuration.
   *
   * <bastille>
   *   <flint index="index" itemplates="ixml/default.xsl" multiple="false" legacy="false"/>
   * </bastille>
   */
  protected static synchronized void init() {
    // Location of the index
    File directory = GlobalSettings.getDirProperty("bastille.flint.index");
    if (directory == null) {
      directory = new File(GlobalSettings.getRepository(), DEFAULT_INDEX_LOCATION);
    }
    _directory = directory;
    // Location of the itemplates
    File itemplates = GlobalSettings.getDirProperty("bastille.flint.itemplates");
    if (itemplates == null) {
      itemplates = new File(GlobalSettings.getRepository(), DEFAULT_ITEMPLATES_LOCATION);
    }
    _itemplates = itemplates;
    // Is it multiple index?
    _isMultiple = GlobalSettings.get("bastille.flint.multiple", false);
    _legacy = GlobalSettings.get("bastille.flint.legacy", false);
  }

  /**
   * @return the default location of the index.
   */
  public static File directory() {
    if (_directory == null) init();
    return _directory;
  }

  /**
   * @return the itemplates to process the data.
   */
  public static File itemplates() {
    if (_itemplates == null) init();
    return _itemplates;
  }

  /**
   * Indicates whether to use the legacy mode.
   *
   * <p>In legacy mode, Flint stores path differently.
   *
   * @return <code>true</code> if Flint is in legacy mode;
   *         <code>false</code> for a single index.
   */
  public static boolean inLegacyMode() {
    return _legacy;
  }

  /**
   * Indicates whether flint is configured for multiple indexes.
   *
   * @return <code>true</code> if Flint is configured for multiple indexes;
   *         <code>false</code> for a single index.
   */
  public static boolean hasMultiple() {
    return _isMultiple;
  }

  /**
   * Returns the index master for a single index.
   *
   * @return the index master for a single index.
   */
  public static IndexMaster getMaster() {
    IndexMaster single = SingleIndex.master();
    return single;
  }

  /**
   * Returns the Index master for the specified name.
   *
   * @param name the name of the index or <code>null</code> for a single index.
   *
   * @return the index master for the specified index.
   */
  public static IndexMaster getMaster(String name) {
    if (name == null) return getMaster();
    File directory = new File(directory(), name);
    return MultipleIndex.getMaster(directory);
  }

}
