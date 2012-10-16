/*
 * This file is part of the Bastille library.
 *
 * Available under a commercial licence, contact Weborganic.
 *
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package com.weborganic.bastille.psml;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weborganic.berlioz.GlobalSettings;
import org.weborganic.berlioz.xml.XMLCopy;

import com.topologi.diffx.xml.XMLStringWriter;

/**
 * PSML configuration.
 *
 * @author Christophe Lauret
 * @version 56 October 2012
 */
public final class PSMLConfig {

  /**
   * Utility class.
   */
  private PSMLConfig() {
  }

  /**
   * Logger for debugging
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(PSMLConfig.class);

  /**
   * The key for the root folder containing <i>all</i> PSML files, including config and content.
   */
  public static final String BASTILLE_PSML_ROOT = "bastille.psml.root";

  /**
   * The default folder used by PSML files.
   */
  public static final String DEFAULT_PSML_ROOT = "psml";

  /**
   * The default extension used by PSML files.
   */
  public static final String DEFAULT_PSML_EXTENSION = ".psml";

  /**
   * Returns the config file from the path.
   *
   * @param pathInfo The path info from within the "config" folder.
   *
   * @return The corresponding PSML file.
   */
  public static PSMLFile getConfigFile(String pathInfo) {
    return getFile(attach("config", pathInfo));
  }

  /**
   * Returns the content file from the path.
   *
   * @param pathInfo The path info from within the "content" folder.
   *
   * @return The corresponding PSML file.
   */
  public static PSMLFile getContentFile(String pathInfo) {
    return getFile(attach("content", pathInfo));
  }

  /**
   * Returns the content folder from the path.
   *
   * @param pathInfo The path info from within the "content" folder.
   *
   * @return A PSML file for a folder in the content folder.
   */
  public static PSMLFile getContentFolder(String pathInfo) {
    return getFolder(attach("content", pathInfo));
  }

  /**
   * Returns the config file from the path.
   *
   * @param pathInfo The path info from within the root folder.
   *
   * @return A PSML file for a folder in the content folder.
   */
  public static PSMLFile getFolder(String pathInfo) {
    String path = normalise(pathInfo);
    File root = getRoot();
    File file = new File(root, path);
    return new PSMLFile(path, file);
  }

  /**
   * Returns the config file from the path.
   *
   * @param pathInfo The path info from within the root folder.
   *
   * @return A PSML file for a folder in the content folder.
   */
  public static PSMLFile getFile(String pathInfo) {
    String path = normalise(pathInfo) + PSMLConfig.DEFAULT_PSML_EXTENSION;
    File root = getRoot();
    File file = new File(root, path);
    return new PSMLFile(path, file);
  }

  /**
   * Returns the root folder for all files.
   *
   * <p>The PSML root folder can be defined in the <code>config-[mode].xml</code> using the key
   * {@value BASTILLE_PSML_ROOT}.
   *
   * @return the XML Root folder as defined in the configuration or "xml" if undefined.
   */
  public static File getRoot() {
    String name = GlobalSettings.get(BASTILLE_PSML_ROOT, DEFAULT_PSML_ROOT);
    File folder = new File(GlobalSettings.getRepository(), name);
    if (!folder.exists()) {
      LOGGER.warn("Creating PSML root folder ({})", name);
      boolean created = folder.mkdirs();
      if (!created) {
        LOGGER.warn("Unable to create PSML root folder ({})", name);
      }
    } else if (!folder.isDirectory()) {
      LOGGER.warn("PSML root folder ({}) is not a directory!", name);
    }
    return folder;
  }

  /**
   * Loads the specified XML file and returns it as a string.
   *
   * <p>The XML returned is wrapped in the following XML:
   * <pre>{@code  <psml-file name="[filename]" base="[basedir]" status="[status]"> ... </psml-file>}</pre>
   *
   * @param psml The PSML file to load
   *
   * @return the content of the XML file.
   *
   * @throws IOException If an error occurs while trying to read or write the XML.
   */
  public static String load(PSMLFile psml) throws IOException {
    File file = psml.file();
    XMLStringWriter xml = new XMLStringWriter(false, false);
    xml.openElement("psml-file");
    xml.attribute("name", file.getName());

    // Compute the base directory
    String base = psml.path();
    base = "/" + base.substring(0, base.length() - file.getName().length());
    xml.attribute("base", base);

    // All good, print to the XML stream
    if (file.exists()) {
      xml.attribute("status", "ok");
      XMLCopy.copyTo(file, xml);
      LOGGER.info("loaded {}", file.getAbsolutePath());

    // The requested file could not be found
    } else {
      xml.attribute("status", "not-found");
      // TODO FIX error message
      xml.writeText("Unable to find file: "+psml.path());
      LOGGER.info("{} does not exist", file.getAbsolutePath());
    }
    xml.closeElement();
    xml.flush();
    return xml.toString();
  }

  // private helpers
  // ----------------------------------------------------------------------------------------------

  /**
   * Filters and normalises the value in the path informations.
   *
   * @param path The path to normalise.
   * @return The same path without an '/' at the end.
   */
  private static String normalise(String path) {
    if (path.endsWith("/")) {
      return path.substring(0, path.length()-1);
    }
    return path;
  }

  /**
   * Attach the value of the main folder to the pathInfo value avoiding doubling up the '/'.
   *
   * @param main     The main directory ("config" or "content").
   * @param pathInfo The pathInfo to attach.
   *
   * @return The main attached to the pathInfo.
   */
  private static String attach(String main, String pathInfo) {
    StringBuilder path = new StringBuilder(main);
    if (pathInfo.length() > 0 && pathInfo.charAt(0) != '/') path.append('/');
    path.append(pathInfo);
    return path.toString();
  }

}
