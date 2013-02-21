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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.lucene.document.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weborganic.bastille.psml.PSMLConfig;
import org.weborganic.berlioz.GlobalSettings;
import org.weborganic.berlioz.util.FileUtils;
import org.weborganic.flint.IndexConfig;
import org.weborganic.flint.local.LocalFileContentType;


/**
 * @author Christophe Lauret
 * @version 19 October 2012
 */
public final class GenericConfig implements IFlintConfig {

  /**
   * The logger for this.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(GenericConfig.class);

  /**
   * The default folder name for the index files.
   */
  protected static final String DEFAULT_INDEX_LOCATION = "index";

  /**
   * The location of the public
   */
  private final File _public = GlobalSettings.getRepository().getParentFile();

  /**
   * The location of the private folders for XML.
   */
  private final File _private = GlobalSettings.getRepository();

  /**
   * Where the index is located.
   */
  private File _directory;

  /**
   * Maps the itemplates to a media type.
   */
  private Map<String, File> _itemplates = new HashMap<String, File>();

  /**
   * Whether multiple indexes are in use.
   */
  private final boolean _isMultiple;

  /**
   * Creates a new generic config.
   *
   * @param directory the directory where the index(es) is stored.
   * @param ixml      the directory where the ixml information is location.
   */
  protected GenericConfig(File directory, File ixml, boolean isMultiple) {
    this._directory = directory;
    this._itemplates.put("text/xml", new File(ixml, "ixml.xsl"));
    this._itemplates.put("application/xml", new File(ixml, "ixml.xsl"));
    this._itemplates.put(PSMLConfig.MEDIATYPE, new File(ixml, "ipsml.xsl"));
    this._isMultiple = isMultiple;
  }

  /**
   * @return the directory containing the index.
   */
  @Override
  public File getDirectory() {
    return this._directory;
  }

  @Override
  public File getIXMLTemplates(String mediatype) {
    return this._itemplates.get(mediatype);
  }

  @Override
  public Map<String, File> getIXMLTemplates() {
    return Collections.unmodifiableMap(this._itemplates);
  }

  @Override
  public boolean hasMultiple() {
    return this._isMultiple;
  }

  @Override
  public void configure(IndexConfig iconfig) {
    for (Entry<String, File> itemplate : this._itemplates.entrySet()) {
      String mediatype = itemplate.getKey();
      URI xslt = itemplate.getValue().toURI();
      iconfig.setTemplates(LocalFileContentType.SINGLETON, mediatype, xslt);
    }
  }

  @Override
  public String toPath(File f) {
    boolean isPublic = f.getPath().indexOf("WEB-INF") == -1;
    try {
      if (isPublic) {
        return FileUtils.path(this._public, f);
      } else {
        return FileUtils.path(this._private, f);
      }
    } catch (Exception ex) {
      LOGGER.warn("Error while extracting path from file {}: {}", f.getAbsolutePath(), ex);
      return null;
    }
  }

  @Override
  public File toFile(Document doc) {
    String path = doc.get("path");
    boolean isPublic = "public".equals(doc.get("visilibity"));
    if (isPublic) {
      return new File(this._public, path);
    } else {
      return new File(this._private, path);
    }
  }

  /**
   * Returns a new config instance loading the setting from the global settings.
   *
   * @return a new config instance.
   */
  public static GenericConfig newInstance() {
    File directory = GlobalSettings.getDirProperty("bastille.flint.index");
    if (directory == null) {
      directory = new File(GlobalSettings.getRepository(), "index");
    }
    File ixml = new File(GlobalSettings.getRepository(), "ixml");
    // TODO: compute single/multiple index
    return new GenericConfig(directory, ixml, false);
  }

}
