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
import java.net.URI;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weborganic.bastille.psml.PSMLConfig;
import org.weborganic.berlioz.GlobalSettings;
import org.weborganic.berlioz.util.FileUtils;
import org.weborganic.flint.IndexConfig;
import org.weborganic.flint.local.LocalFileContentType;


/**
 * The configuration used up until Bastille 0.7.
 *
 * <p>Replaced by Generic config.
 *
 * @author Christophe Lauret
 * @version 19 October 2012
 */
public final class LegacyConfig implements IFlintConfig {

  /**
   * The logger for this.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(LegacyConfig.class);

  /**
   * The default folder name for the index files.
   */
  private static final String DEFAULT_INDEX_LOCATION = "index";

  /**
   * The default templates to use for processing the data and generate the indexable XML.
   */
  protected static final String DEFAULT_ITEMPLATES_LOCATION = "ixml/default.xsl";

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
   * Where the index is located.
   */
  private final File _directory;

  /**
   * Where the global itemplates are.
   */
  private final File _itemplates;

  /**
   * Whether multiple indexes are in use.
   */
  private final boolean _isMultiple;

  /**
   * Creates a new legacy config.
   *
   * @param directory  The directory containing a single or a collection of indexes.
   * @param itemplates The itemplates to use.
   * @param isMultiple <code>true</code> if there are multiple indexes; <code>false</code> for a single index.
   */
  private LegacyConfig(File directory, File itemplates, boolean isMultiple) {
    this._directory = directory;
    this._itemplates = itemplates;
    this._isMultiple = isMultiple;
  }

  @Override
  public File getDirectory() {
    return this._directory;
  }

  @Override
  public File getIXMLTemplates(String mediatype) {
    return this._itemplates;
  }

  @Override
  public Map<String, File> getIXMLTemplates() {
    return null;
  }

  @Override
  public boolean hasMultiple() {
    return this._isMultiple;
  }

  @Override
  public void configure(IndexConfig iconfig) {
    URI uri = this._itemplates.toURI();
    iconfig.setTemplates(LocalFileContentType.SINGLETON, "text/xml", uri);
    iconfig.setTemplates(LocalFileContentType.SINGLETON, PSMLConfig.MEDIATYPE, uri);
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
  public static LegacyConfig newInstance() {
    // Location of the index
    File directory = new File(GlobalSettings.getRepository(), DEFAULT_INDEX_LOCATION);
    File itemplates = new File(GlobalSettings.getRepository(), DEFAULT_ITEMPLATES_LOCATION);
    boolean isMultiple = hasMultiple(directory);
    return new LegacyConfig(directory, itemplates, isMultiple);
  }

  /**
   * Returns a new legacy config instance loading the setting from the global settings.
   *
   * @param xslt The XSLT to use.
   *
   * @return a new legacy config instance.
   */
  public static LegacyConfig newInstance(File xslt) {
    // Location of the index
    File directory = new File(GlobalSettings.getRepository(), DEFAULT_INDEX_LOCATION);
    boolean isMultiple = hasMultiple(directory);
    return new LegacyConfig(directory, xslt, isMultiple);
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
  public static String asPath(File f) {
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
  public static File asFile(Document doc) {
    String path = doc.get("path");
    String mediatype = doc.get("mediatype");
    boolean isPublic = "public".equals(doc.get("visilibity"));
    if (isPublic) {
      return new File(PUBLIC, path);
    } else {
      if ("application/vnd.pageseeder.psml+xml".equals(mediatype))
        return new File(PRIVATE_PSML, path+".psml");
      else
        return new File(PRIVATE_XML, path+".xml");
    }
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
