package com.weborganic.bastille.flint.helpers;

import java.io.File;

import org.weborganic.berlioz.GlobalSettings;

public class FlintConfig {

  protected static final String DEFAULT_INDEX_CONFIG = "index";

  protected static final String DEFAULT_ITEMPLATES_CONFIG = "ixml";

  protected static final String DEFAULT_INDEX_LOCATION = "index";

  protected static final String DEFAULT_ITEMPLATES_LOCATION = "ixml/default.xsl";

  /** Where the index is located */
  private static volatile File directory = null;

  /** Where the itemplates are */
  private static volatile File itemplates = null;

  protected static synchronized final void init() {
    // Location of the index
    if (directory == null) {
      File dir = GlobalSettings.getDirProperty(DEFAULT_INDEX_CONFIG);
      if (dir == null) {
        dir = new File(GlobalSettings.getRepository(), DEFAULT_INDEX_LOCATION);
      }
      directory = dir;
    }
    // Location of the itemplates
    if (itemplates == null) {
      File ixml = GlobalSettings.getDirProperty(DEFAULT_ITEMPLATES_CONFIG);
      if (ixml == null) {
        ixml = new File(GlobalSettings.getRepository(), DEFAULT_ITEMPLATES_LOCATION);
      }
      itemplates = ixml;
    }
  }

  public static File directory() {
    if (directory == null) init();
    return directory;
  }
  
  public static File itemplates() {
    if (itemplates == null) init();
    return itemplates;
  }
}
