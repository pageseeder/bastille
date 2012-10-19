/*
 * This file is part of the Bastille library.
 *
 * Available under a commercial licence, contact Weborganic.
 *
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package com.weborganic.bastille.flint.config;

import java.io.File;
import java.net.URI;

import org.apache.lucene.document.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weborganic.berlioz.GlobalSettings;
import org.weborganic.berlioz.util.FileUtils;
import org.weborganic.flint.IndexConfig;
import org.weborganic.flint.local.LocalFileContentType;

import com.weborganic.bastille.psml.PSMLConfig;

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
  private boolean _isMultiple = false;

  /**
   * Creates a new legacy config.
   *
   * @param directory  the directory containing a single or a collection of indexes.
   * @param itemplates The itemplates to use.
   */
  protected LegacyConfig(File directory, File itemplates) {
    this._directory = directory;
    this._itemplates = itemplates;
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
    return new LegacyConfig(directory, itemplates);
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
    return new LegacyConfig(directory, xslt);
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

}
