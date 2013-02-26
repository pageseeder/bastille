/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.weborganic.bastille.flint.config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weborganic.bastille.flint.helpers.IndexMaster;
import org.weborganic.berlioz.GlobalSettings;


/**
 * Centralizes the configuration for flint.
 *
 * @author Christophe Lauret
 * @version 0.8.7 - 25 February 2013
 * @since 0.7.9
 */
public final class FlintConfig {

  // static methods
  // ----------------------------------------------------------------------------------------------

  /**
   * The list of all masters created.
   */
  private static final ConcurrentMap<File, IndexMaster> MASTERS = new ConcurrentHashMap<File, IndexMaster>();

  /**
   * Logger for debugging
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(FlintConfig.class);

  /**
   * Whether to operate in legacy mode.
   */
  private static volatile boolean legacy = false;

  /**
   * The flint configuration actually in use.
   */
  private static volatile IFlintConfig iconfig = null;

  /**
   * Creates new analyzers when needed.
   */
  private static volatile AnalyzerFactory analyzerFactory = null;

  /**
   * Utility class.
   */
  private FlintConfig() {
  }

  /**
   * Allows implementation to setup the Flint config and bypass the default auto setup.
   *
   * <p>This method can only be called once.
   *
   * @param config The global configuration to use for Flint.
   *
   * @throws IllegalStateException if the setup method has already been called once.
   */
  public static synchronized void setup(IFlintConfig config) {
    if (iconfig != null)
      throw new IllegalStateException("The configuration has already been set to "+iconfig.getClass().getName());
    iconfig = config;
    LOGGER.info("Setup Flint config manually using {}", iconfig.getClass().getName());
  }

  /**
   * Initialize the Flint from the global configuration.
   */
  private static synchronized void autoSetup() {
    // Detect which version of the configuration we should load
    legacy = isLegacy();
    if (legacy) {
      LOGGER.info("Auto-Setup Flint config using LegacyConfig");
      iconfig = LegacyConfig.newInstance();
    } else {
      LOGGER.info("Auto-Setup Flint config using GenericConfig");
      iconfig = GenericConfig.newInstance();
    }
  }

  /**
   * @return the default location of the index.
   */
  public static synchronized File directory() {
    if (iconfig == null) autoSetup();
    return iconfig.getDirectory();
  }

  /**
   * Indicates whether flint is configured for multiple indexes.
   *
   * @return <code>true</code> if Flint is configured for multiple indexes;
   *         <code>false</code> for a single index.
   */
  public static synchronized boolean hasMultiple() {
    if (iconfig == null) autoSetup();
    return iconfig.hasMultiple();
  }

  /**
   * Returns the index master for a single index.
   *
   * @return the index master for a single index.
   */
  public static synchronized IndexMaster getMaster() {
    if (iconfig == null) autoSetup();
    if (iconfig.hasMultiple()) {
      LOGGER.warn("Requesting a single index in multiple index configuration!");
    }
    IndexMaster single = getOrCreateMaster(FlintConfig.directory());
    return single;
  }

  /**
   * Returns the Index master for the specified name.
   *
   * @param name the name of the index or <code>null</code> for a single index.
   *
   * @return the index master for the specified index.
   */
  public static synchronized IndexMaster getMaster(String name) {
    if (iconfig == null) autoSetup();
    if (name == null) return getMaster();
    if (!iconfig.hasMultiple()) {
      LOGGER.warn("Requesting a named index in single index configuration!");
    }
    File directory = new File(directory(), name);
    return getOrCreateMaster(directory);
  }

  /**
   * Returns the flint configuration used by default.
   *
   * @return the flint configuration used by default.
   */
  public static synchronized IFlintConfig get() {
    if (iconfig == null) autoSetup();
    return iconfig;
  }

  /**
   * Set the default analyzer factory.
   *
   * @param factory the <code>AnalyzerFactory</code> to use as default.
   */
  public static synchronized void setAnalyzerFactory(AnalyzerFactory factory) {
    analyzerFactory = factory;
  }

  /**
   * @return the list of index masters currently in use.
   */
  public static synchronized List<IndexMaster> getMasters() {
    return new ArrayList<IndexMaster>(MASTERS.values());
  }

  /**
   * Returns a new analyser form the specifies AnalyzerFactory or a new StandardAnalyser otherwise.
   *
   * <p>This method always returns a value.
   *
   * @return A new Analyzer.
   */
  public static synchronized Analyzer newAnalyzer() {
    if (analyzerFactory == null)
      return new StandardAnalyzer(Version.LUCENE_30);
    return analyzerFactory.getAnalyzer();
  }

  // private helpers
  // ----------------------------------------------------------------------------------------------

  /**
   * Indicates whether we are in legacy mode.
   *
   * @return <code>true</code> if we are in legacy mode;
   *         <code>false</code> otherwise.
   */
  private static boolean isLegacy() {
    File itemplates = new File(GlobalSettings.getRepository(), LegacyConfig.DEFAULT_ITEMPLATES_LOCATION);
    return itemplates.exists();
  }

  /**
   * @param index The directory containing the index to return
   *
   * @return the master for the given index root
   */
  public static IndexMaster getOrCreateMaster(File index) {
    if (index == null) return null;
    IndexMaster master;
    synchronized (MASTERS) {
      master = MASTERS.get(index);
      if (master == null) {
        master = new IndexMaster(index);
        MASTERS.put(index, master);
      }
    }
    return master;
  }

}
