/*
 * Copyright (c) 2011 weborganic systems pty. ltd.
 */
package com.weborganic.bastille.xml;

import java.io.File;

import org.weborganic.berlioz.content.ContentRequest;
import org.weborganic.berlioz.content.Environment;


/**
 * Centralises the configuration options for this package.
 * 
 * <p>This class is used to ensure that the same configuration options are used by all generators
 * in this package. 
 * 
 * @author Christophe Lauret
 * @version 0.6.8 - 8 June 2011
 * @since 0.6.1
 */
public final class XMLConfiguration {

  /** Utility class */
  private XMLConfiguration() {
  }

  /**
   * The key for the location of the XML path.
   */
  public static final String BASTILLE_XML_ROOT = "bastille.xml.root";

  /**
   * The key for the extension used by XML files.
   */
  public static final String BASTILLE_XML_EXTENSION = "bastille.xml.extension";

  /**
   * Returns the XML Root folder used by generators in this package.
   * 
   * <p>The XML root folder can be defined in the <code>config-[mode].xml</code> using the key
   * {@value BASTILLE_XML_ROOT}.
   * 
   * @param req The content request.
   * @return the XML Root folder as defined in the configuration or "xml" if undefined.
   */
  public static File getXMLRootFolder(ContentRequest req) {
    Environment env = req.getEnvironment();
    String root = env.getProperty(BASTILLE_XML_ROOT, "xml");
    return env.getPrivateFile(root);
  }

  /**
   * Returns the XML extension used by generators in this package.
   * 
   * <p>The XML extension can be defined in the <code>config-[mode].xml</code> using the key
   * {@value BASTILLE_XML_EXTENSION}.
   * 
   * @param req The content request.
   * @return the XML extension as defined in the configuration or ".xml" if undefined.
   */
  public static String getXMLExtension(ContentRequest req) {
    Environment env = req.getEnvironment();
    return env.getProperty(BASTILLE_XML_EXTENSION, ".xml");
  }

}
