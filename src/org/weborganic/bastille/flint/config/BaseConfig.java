/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.weborganic.bastille.flint.config;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.lucene.document.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weborganic.bastille.util.FileFilters;
import org.weborganic.flint.IndexConfig;


/**
 * A base class for flint configurations.
 *
 * @author Christophe Lauret
 * @version 27 February 2013
 */
public abstract class BaseConfig implements IFlintConfig {

  /**
   * The logger for this.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(BaseConfig.class);

  /**
   * The default folder name for the index files.
   */
  protected static final String DEFAULT_INDEX_LOCATION = "index";

  /**
   * The default templates to use for processing the data and generate the indexable XML.
   */
  protected static final String DEFAULT_ITEMPLATES_LOCATION = "ixml";

  /**
   * The default templates to use for processing the data and generate the indexable XML.
   */
  protected static final String DEFAULT_TEMPLATES_NAME = "default.xsl";

  /**
   * Where the index is located.
   */
  private final File _directory;

  /**
   * Where the global itemplates are.
   */
  private final File _ixml;

  /**
   * The index configuration containing all the templates for the indexers.
   */
  private final IndexConfig _defaultConfig;

  /**
   * Whether multiple indexes are in use.
   */
  private final boolean _isMultiple;

  /**
   * Creates a new legacy config.
   *
   * @param directory  The directory containing a single or a collection of indexes.
   * @param ixml       The directory containing the ixml templates to use.
   * @param isMultiple <code>true</code> if there are multiple indexes; <code>false</code> for a single index.
   */
  BaseConfig(File directory, File ixml, boolean isMultiple) {
    this._directory = directory;
    this._defaultConfig = new IndexConfig();
    this._ixml = ixml;
    this._isMultiple = isMultiple;
  }

  @Override
  public final File getDirectory() {
    return this._directory;
  }

  @Override
  public final boolean hasMultiple() {
    return this._isMultiple;
  }

  @Override
  public final List<File> getIXMLTemplates() {
    if (this._ixml.exists() && this._ixml.isDirectory()) {
      File[] xslt = this._ixml.listFiles(FileFilters.getXSLTFiles());
      return Arrays.asList(xslt);
    } else {
      LOGGER.warn("There are no ixml templates in your /WEB-INF/xml directory");
      return Collections.emptyList();
    }
  }

  /**
   * @return the default index configuration
   */
  public final IndexConfig getDefaultConfig() {
    return this._defaultConfig;
  }

  /**
   * @return the directory containing the ixml templates.
   */
  public final File getIXMLDirectory() {
    return this._ixml;
  }

  @Override
  public abstract void reload();

  @Override
  public abstract IndexConfig get(String name);

  @Override
  public abstract String toPath(File f);

  @Override
  public abstract File toFile(Document doc);

  // static helpers
  // ----------------------------------------------------------------------------------------------


  /**
   * Checks whether the specified index directory contains multiple indexes.
   *
   * @param directory the directory
   * @return <code>true</code> If there are multiple directories; <code>false</code> for a single index.
   */
  protected static boolean hasMultiple(File directory) {
//    if (GlobalSettings.get("bastille.index.multiple", true)) {
//      LOGGER.debug("Set to multiple index configuration");
//      return true;
//    }
    // Look for subdirectories
    if (directory.exists() && directory.isDirectory()) {
      final FileFilter directoriesOnly = new FileFilter() {
        @Override
        public boolean accept(File f) {
          return f.isDirectory();
        }
      };
      File[] subdirs = directory.listFiles(directoriesOnly);
      if (subdirs.length > 0) {
        LOGGER.info("Detected multiple index configuration");
        return true;
      }
    }
    // No directory, it must be a single index
    LOGGER.info("Detected multiple index configuration");
    return false;
  }

}
