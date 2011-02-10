package com.weborganic.bastille.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.GlobalSettings;
import org.weborganic.berlioz.xml.XMLCopy;

import com.topologi.diffx.xml.XMLWriter;

/**
 * A utility class for templates files.
 * 
 * @author Christophe Lauret
 * 
 * @version 31 May 2010
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
   */
  public static void write(XMLWriter xml, File file) throws BerliozException, IOException {
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
      xml.writeText("Unable to read file: "+file.getName());
      LOGGER.debug("{} does not exist", file.getAbsolutePath());
    }
    xml.closeElement();    
  }

  /**
   * Returns the template file.
   * 
   * @param name   The name of the template file property.
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
   * @return the Etag for the given template file name.
   */
  public static String getETag(String name, boolean reload) {
    File f = getFile(name, reload);
    return conf.length() + "x" + conf.lastModified() + "|" + f.length() + "x" + f.lastModified();
  }

  // Private helpers
  // ----------------------------------------------------------------------------------------------

  /**
   * Loads the properties.
   * 
   * @return Properties. Always.
   */
  private static Properties loadConf() {
    // TODO use properties or prp extension instead
    File file = new File(GlobalSettings.getRepository(), "conf/template-config.prp");
    if (conf == null) conf = file;
    Properties p = new Properties();
    try {
      LOGGER.info("Loading conf properties for template from {}", file.getAbsolutePath());
      FileInputStream in = new FileInputStream(file); 
      p.load(in);
      in.close();
    } catch (Exception ex) {
      LOGGER.warn("Unable to read conf properties for template", ex);
    }
    return p;
  }

}
