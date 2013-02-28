/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.weborganic.bastille.flint.config;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.lucene.document.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weborganic.bastille.psml.PSMLConfig;
import org.weborganic.bastille.util.FileFilters;
import org.weborganic.berlioz.GlobalSettings;
import org.weborganic.flint.IndexConfig;
import org.weborganic.flint.local.LocalFileContentType;


/**
 * A configuration for multiple indexes which uses templates depending on the index.
 *
 * <p>This configuration is suitable for when:
 * <ul>
 *   <li>All indexes are located in the <code>[WEB-INF]/index</code> directory</li>
 *   <li>Each index is a single folder in the index directory</li>
 *   <li>Only one set of templates located in <code>[WEB-INF]/ixml/default.xsl</code> is used.</li>
 * </ul>
 *
 * @author Christophe Lauret
 * @version 28 February 2013
 */
public final class ComplexConfig extends BaseDefaultConfig implements IFlintConfig {

  /**
   * The logger for this.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(ComplexConfig.class);

  /**
   * The location of the public
   */
  private static final File PUBLIC = GlobalSettings.getRepository().getParentFile();

  /**
   * The location of the private folders for XML.
   */
  private static final File PRIVATE_XML = new File(GlobalSettings.getRepository(), "xml");

  /**
   * The location of the private folders for PSML.
   */
  private static final File PRIVATE_PSML = new File(GlobalSettings.getRepository(), "psml");

  /**
   * The index configuration containing all the templates for the indexers.
   */
  private final ConcurrentMap<String, IndexConfig> _configs;

  /**
   * Creates a new legacy config.
   *
   * @param directory  The directory containing a single or a collection of indexes.
   * @param ixml       The directory containing the ixml templates to use.
   * @param isMultiple <code>true</code> if there are multiple indexes; <code>false</code> for a single index.
   */
  private ComplexConfig(File directory, File ixml, boolean isMultiple) {
    super(directory, ixml, isMultiple);
    this._configs = new ConcurrentHashMap<String, IndexConfig>();
    load(this._configs, this.getDefaultConfig(), ixml);
  }

  @Override
  public void reload() {
    load(this._configs, this.getDefaultConfig(), this.getTemplatesDirectory());
  }

  @Override
  public final List<File> getTemplates() {
    File ixml = getTemplatesDirectory();
    if (ixml.exists() && ixml.isDirectory()) {
      File[] xslt = ixml.listFiles(FileFilters.getXSLTFiles());
      return Arrays.asList(xslt);
    } else {
      LOGGER.warn("There are no ixml templates in your /WEB-INF/xml directory");
      return Collections.emptyList();
    }
  }

  @Override
  public IndexConfig getIndexConfig(String name) {
    IndexConfig config = this._configs.get(name);
    return config != null? config : this.getDefaultConfig();
  }

  @Override
  public String toPath(File f) {
    return SimpleConfig.asPath(f);
  }

  @Override
  public File toFile(Document doc) {
    return SimpleConfig.asFile(doc);
  }

  // static helpers
  // ----------------------------------------------------------------------------------------------

  /**
   * Returns a new legacy config instance loading the setting from the global settings.
   *
   * @return a new legacy config instance.
   */
  public static ComplexConfig newInstance() {
    // Location of the index
    File directory = new File(GlobalSettings.getRepository(), DEFAULT_INDEX_LOCATION);
    File itemplates = new File(GlobalSettings.getRepository(), DEFAULT_ITEMPLATES_LOCATION);
    boolean isMultiple = hasMultiple(directory);
    return new ComplexConfig(directory, itemplates, isMultiple);
  }

  /**
   * @param iconfig
   * @param ixml
   */
  private static void load(ConcurrentMap<String, IndexConfig> configs, IndexConfig defaultConfig, File ixml) {
    if (ixml.exists() && ixml.isDirectory()) {
      File[] xslt = ixml.listFiles(FileFilters.getXSLTFiles());
      configs.clear();
      for (File x : xslt) {
        URI uri = x.toURI();
        if (DEFAULT_TEMPLATES_NAME.equals(x)) {
          defaultConfig.setTemplates(LocalFileContentType.SINGLETON,  "text/xml", uri);
          defaultConfig.setTemplates(LocalFileContentType.SINGLETON, PSMLConfig.MEDIATYPE, uri);
        } else {
          IndexConfig config = new IndexConfig();
          String name = x.getName().substring(0, x.getName().length() - 4);
          configs.put(name, config);
          config.setTemplates(LocalFileContentType.SINGLETON,  "text/xml", uri);
          config.setTemplates(LocalFileContentType.SINGLETON, PSMLConfig.MEDIATYPE, uri);
        }
      }
    } else {
      LOGGER.warn("Unable to find your IXML templates!");
    }
  }

}
