/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.pageseeder.bastille.flint.config;

import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.List;

import org.apache.lucene.document.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.pageseeder.bastille.psml.PSMLConfig;
import org.pageseeder.berlioz.GlobalSettings;
import org.pageseeder.flint.IndexConfig;
import org.pageseeder.flint.local.LocalFileContentType;


/**
 * A simple configuration for single/multiple indexes which uses only one template common to all indexes.
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
public final class SimpleConfig extends BaseDefaultConfig implements IFlintConfig {

  /**
   * The logger for this.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(SimpleConfig.class);

  /**
   * Creates a new legacy config.
   *
   * @param directory  The directory containing a single or a collection of indexes.
   * @param ixml       The directory containing the ixml templates to use.
   * @param isMultiple <code>true</code> if there are multiple indexes; <code>false</code> for a single index.
   */
  private SimpleConfig(File directory, File ixml, boolean isMultiple) {
    super(directory, ixml, isMultiple);
    load(this.getDefaultConfig(), ixml);
  }

  @Override
  public final List<File> getTemplates() {
    File ixml = getTemplatesDirectory();
    return Collections.singletonList(new File(ixml, DEFAULT_TEMPLATES_NAME));
  }

  @Override
  public void reload() {
    load(this.getDefaultConfig(), this.getTemplatesDirectory());
  }

  @Override
  public IndexConfig getIndexConfig(String name) {
    return this.getDefaultConfig();
  }

  @Override
  public String toPath(File f) {
    return asPath(f);
  }

  @Override
  public File toFile(Document doc) {
    return asFile(doc);
  }

  // static helpers
  // ----------------------------------------------------------------------------------------------

  /**
   * Returns a new legacy config instance loading the setting from the global settings.
   *
   * @return a new legacy config instance.
   */
  public static SimpleConfig newInstance() {
    // Location of the index
    File directory = new File(GlobalSettings.getRepository(), DEFAULT_INDEX_LOCATION);
    File itemplates = new File(GlobalSettings.getRepository(), DEFAULT_ITEMPLATES_LOCATION);
    boolean isMultiple = hasMultiple(directory);
    return new SimpleConfig(directory, itemplates, isMultiple);
  }

  /**
   * @param config the config to load
   * @param ixml   the ixml directory
   */
  private static void load(IndexConfig config, File ixml) {
    File xslt = new File(ixml, DEFAULT_TEMPLATES_NAME);
    if (xslt.exists()) {
      URI uri = xslt.toURI();
      config.setTemplates(LocalFileContentType.SINGLETON,  "text/xml", uri);
      config.setTemplates(LocalFileContentType.SINGLETON, PSMLConfig.MEDIATYPE, uri);
    } else {
      LOGGER.warn("Unable to find your IXML templates!");
    }
  }

}
