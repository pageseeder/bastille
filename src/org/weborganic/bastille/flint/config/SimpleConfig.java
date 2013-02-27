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
 * @version 27 February 2013
 */
public final class SimpleConfig extends BaseConfig implements IFlintConfig {

  /**
   * The logger for this.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(SimpleConfig.class);

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
  public void reload() {
    load(this.getDefaultConfig(), this.getIXMLDirectory());
  }

  @Override
  public IndexConfig get(String name) {
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

}
