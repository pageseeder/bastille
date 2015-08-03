/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.pageseeder.bastille.flint.config;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.lucene.document.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.pageseeder.bastille.util.FileFilters;
import org.pageseeder.berlioz.GlobalSettings;
import org.pageseeder.berlioz.util.FileUtils;
import org.pageseeder.flint.IndexConfig;


/**
 * A base class for flint configurations using the default settings.
 *
 * <p>The default settings assumes that:
 * <ul>
 *   <li>All indexes are located in the same directory, by default <code>/WEB-INF/index</code></li>
 *   <li>All templates are located in the same directory, by default <code>/WEB-INF/ixml</code></li>
 *   <li>Source files are located in <code>/WEB-INF/xml</code> or <code>/WEB-INF/psml</code></li>
 *   <li>All indexes use the <code>LocalIndex</code> Flint implementation</li>
 * </ul>
 *
 * <p>It also provides some useful utility methods that can be used by implementation.
 *
 * @author Christophe Lauret
 * @version 28 February 2013
 */
public abstract class BaseDefaultConfig implements IFlintConfig {

  /**
   * The logger for this.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(BaseDefaultConfig.class);

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
   * The location of the public
   */
  protected static final File PUBLIC = GlobalSettings.getRepository().getParentFile();

  /**
   * The location of the private folders for XML.
   */
  protected static final File PRIVATE_XML = new File(GlobalSettings.getRepository(), "xml");

  /**
   * The location of the private folders for PSML.
   */
  protected static final File PRIVATE_PSML = new File(GlobalSettings.getRepository(), "psml");

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
  BaseDefaultConfig(File directory, File ixml, boolean isMultiple) {
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
  public final List<String> getIndexNames() {
    File index = getDirectory();
    if (hasMultiple()) {
      return listIndexes(index);
    } else {
      return Collections.singletonList("");
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
  public final File getTemplatesDirectory() {
    return this._ixml;
  }

  // static helpers
  // ----------------------------------------------------------------------------------------------

  /**
   * Returns the list of index names in the specified directory
   *
   * @param directory The root directory of the indexes.
   *
   * @return the list of index names corresponding to each folder name.
   */
  protected static List<String> listIndexes(File directory) {
    List<String> names = new ArrayList<String>();
    if (directory.isDirectory()) {
      File[] indexes = directory.listFiles(FileFilters.getFolders());
      for (File index : indexes) {
        names.add(index.getName());
      }
    }
    return names;
  }

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
      File[] subdirs = directory.listFiles(FileFilters.getFolders());
      if (subdirs.length > 0) {
        LOGGER.info("Detected multiple index configuration");
        return true;
      }
    }
    // No directory, it must be a single index
    LOGGER.info("Detected multiple index configuration");
    return false;
  }

  /**
   * Returns the relative web path for the specified file.
   *
   * <p>This method returns:
   * <ul>
   *   <li>for private files, the relative path from the '/WEB-INF/xml' without the '.xml' extension.
   *   <li>for public files, the relative path from the Web application root.
   * </ul>
   *
   * @param f for the specified file.
   * @return the corresponding path or "" if an error occurs
   */
  protected static String asPath(File f) {
    boolean isPrivateXML = f.getName().endsWith(".xml");
    boolean isPrivatePSML = f.getName().endsWith(".psml");
    try {
      if (isPrivateXML) {
        String path = FileUtils.path(PRIVATE_XML, f);
        return path.substring(0, path.length()-4);
      } else if (isPrivatePSML) {
        String path = FileUtils.path(PRIVATE_PSML, f);
        return path.substring(0, path.length()-5);
      } else {
        return FileUtils.path(PUBLIC, f);
      }
    } catch (IllegalArgumentException ex) {
      LOGGER.warn("Error while extracting path from file {}: {}", f.getAbsolutePath(), ex);
    }
    return "";
  }

  /**
   * Returns the file corresponding to the specified Lucene document.
   *
   * <p>This method looks at the field named "visibility" to determine whether it is a public or
   * private file.
   *
   * <p>It also looks at the mediatype to know in which directory it can be found.
   *
   * @param doc The Lucene document.
   * @return The corresponding file.
   */
  protected static File asFile(Document doc) {
    String path = doc.get("path");
    boolean isPublic = "public".equals(doc.get("visibility"));
    if (isPublic) {
      return new File(PUBLIC, path);
    } else {
      String mediatype = doc.get("mediatype");
      if ("application/vnd.pageseeder.psml+xml".equals(mediatype))
        return new File(PRIVATE_PSML, path+".psml");
      else
        return new File(PRIVATE_XML, path+".xml");
    }
  }
}
