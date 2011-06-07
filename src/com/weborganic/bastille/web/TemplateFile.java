/*
 * Copyright (c) 2011 weborganic systems pty. ltd.
 */
package com.weborganic.bastille.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weborganic.berlioz.GlobalSettings;
import org.weborganic.berlioz.util.MD5;
import org.weborganic.berlioz.xml.XMLCopy;

import com.topologi.diffx.xml.XMLWriter;

/**
 * A utility class for templates files.
 * 
 * @author Christophe Lauret
 * @version 0.6.8 - 7 June 2011
 * @since 0.6.0
 */
public final class TemplateFile {

  /**
   * Logger to use for this file
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(TemplateFile.class); 

  /**
   * The template configuration.
   */
  private static volatile Properties properties = null;

  /**
   * The config file.
   */
  private static volatile File conf = null;

  /**
   * Utility class - no public constructor needed.
   */
  private TemplateFile() {
  }

  /**
   * Write the specified file on the given XML writer.
   * 
   * @param xml  The XML writer the file should be written to.
   * @param file The file to write.
   * 
   * @throws IOException If Berlioz was unable to write on the XML writer.
   */
  public static void write(XMLWriter xml, File file) throws IOException {
    xml.openElement("template-file");
    xml.attribute("name", file.getName());

    // All good, print to the XML stream
    if (file.exists()) {
      xml.attribute("status", "ok");
      boolean cache = GlobalSettings.get("berlioz.cache.xml", true);
      XMLCopy.copyTo(file, xml);
      LOGGER.debug("loaded {}", file.getAbsolutePath());

    // The requested could not be found 
    } else {
      xml.attribute("status", "not-found");
      xml.writeText("Unable to find file: "+file.getName());
      LOGGER.debug("{} does not exist", file.getAbsolutePath());
    }
    xml.closeElement();
  }

  /**
   * Returns the template file.
   * 
   * @param name   The name of the template file property ("header", "footer", etc...).
   * @param reload Whether to reload the configuration. 
   * 
   * @return the corresponding file.
   */
  public static File getFile(String name, boolean reload) {
    // load if needed
    if (properties == null || reload) {
      properties = loadConf();
    }
    // Get the XML file to return
    String filename = properties.getProperty(name, name+".xml");
    File folder = new File(GlobalSettings.getRepository(), "xml");
    return new File(folder, filename);
  }

  /**
   * Returns the Etag for the given template file name.
   * 
   * <p>The ETag is a weak etag based on the length and last modified date of both the config file
   * and the actual file to load.
   *
   * @param name the name of the file property ("header", "footer", etc...)
   * @param reload Whether to reload the configuration. 
   * 
   * @return the Etag for the given template file name.
   */
  public static String getETag(String name, boolean reload) {
    File f = getFile(name, reload);
    return MD5.hash(conf.length() + "x" + conf.lastModified() + "|" + f.length() + "x" + f.lastModified());
  }

  // Private helpers
  // ----------------------------------------------------------------------------------------------

  /**
   * Loads the properties.
   * 
   * @return Properties. Always.
   */
  private static Properties loadConf() {
    File file = new File(GlobalSettings.getRepository(), "conf/template-config.prp");
    if (conf == null) conf = file;
    Properties p = new Properties();
    try {
      LOGGER.info("Loading conf properties for template from {}", file.getAbsolutePath());
      FileInputStream in = new FileInputStream(file); 
      p.load(in);
      in.close();
    } catch (IOException ex) {
      LOGGER.warn("Unable to read conf properties for template: {}", ex.getLocalizedMessage());
    }
    return p;
  }

}
