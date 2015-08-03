/*
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package org.pageseeder.bastille.util;

import java.io.File;
import java.io.FileFilter;

/**
 * A utility class providing some common file filters.
 *
 * @author Christophe Lauret
 * @version 27/02/2013
 */
public final class FileFilters {

  /**
   * Only accepts folders (<code>isDirectory</code> returns <code>true</code>)
   */
  private static final FileFilter FOLDERS = new FileFilter() {
    @Override
    public boolean accept(File d) {
      return d.isDirectory();
    }
  };

  /**
   * Only accepts XML files (ending with ".xml")
   */
  private static final FileFilter XML = new FileFilter() {
    @Override
    public boolean accept(File f) {
      return f.isFile() && f.getName().endsWith(".xml");
    }
  };

  /**
   * Only accepts XSLT files (ending with ".xsl")
   */
  private static final FileFilter XSLT = new FileFilter() {
    @Override
    public boolean accept(File f) {
      return f.isFile() && f.getName().endsWith(".xsl");
    }
  };

  /**
   * Utility class.
   */
  private FileFilters() {
  }

  /**
   * @return A file filter which only accepts folders (<code>isDirectory</code> returns <code>true</code>)
   */
  public static FileFilter getFolders() {
    return FOLDERS;
  }

  /**
   * @return A file filter which only accepts XML files (ending with ".xml")
   */
  public static FileFilter getXMLFiles() {
    return XML;
  }

  /**
   * @return A file filter which only accepts XSLT files (ending with ".xsl")
   */
  public static FileFilter getXSLTFiles() {
    return XSLT;
  }

}
