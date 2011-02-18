package com.weborganic.bastille.xml;

import java.io.File;

import org.weborganic.berlioz.content.ContentRequest;
import org.weborganic.berlioz.content.Environment;


/**
 * Centralises the configuration options for this package
 * 
 * 
 * @since 0.6.1
 * 
 * @author Christophe Lauret
 * @version 19 February 2011
 */
final class XMLConfiguration {

  /** Utility class */
  private XMLConfiguration(){}

  public final static String BASTILLE_XML_ROOT = "bastille.xml.root";
  
  /**
   * Returns the XML Root folder used by the generator in this class.
   */
  public static File getXMLRootFolder(ContentRequest req) {
    Environment env = req.getEnvironment();
    String root = env.getProperty(BASTILLE_XML_ROOT, "xml");
    return env.getPrivateFile(root);
  }

}
