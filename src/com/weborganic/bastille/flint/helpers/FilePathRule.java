/*
 * This file is part of the Bastille library.
 *
 * Available under a commercial licence, contact Weborganic.
 *
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package com.weborganic.bastille.flint.helpers;

import java.io.File;

import org.apache.lucene.document.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weborganic.berlioz.GlobalSettings;
import org.weborganic.berlioz.util.FileUtils;
import org.weborganic.flint.util.Beta;

/**
 * This class is used to map the path to the actual indexed file.
 *
 * <p>This version uses a simple mapping between the path and the source file.
 *
 * <p>Files are divided in two groups using the visibility field:
 * - private: all files within the "WEB-INF/xml" folder
 * - public: any file directly accessible via the Web
 *
 * @author Christophe Lauret
 * @version 0.7.4 - 18 October 2012
 * @since 0.6.0
 */
@Beta
public final class FilePathRule {

  /**
   * The logger for this.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(FilePathRule.class);

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
  public static String toPath(File f) {
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
    } catch (Exception ex) {
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
   * @param doc The Lucene document.
   * @return The corresponding file.
   */
  public static File toFile(Document doc) {
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
