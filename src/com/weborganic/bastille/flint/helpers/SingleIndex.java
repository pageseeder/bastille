/*
 * Copyright (c) 2011 weborganic systems pty. ltd.
 */
package com.weborganic.bastille.flint.helpers;

import java.io.File;

import org.weborganic.berlioz.GlobalSettings;


/**
 * Centralises all the indexing and searching function using Flint for one index.
 * 
 * <p>This class defines a singleton which can be access using the {@link #getInstance()} method.
 * 
 * @author Christophe Lauret
 * @author Jean-Baptiste Reure
 * @version 0.6.0 - 21 July 2010
 * @since 0.6.0
 */
public final class SingleIndex {

  private static File INDEX_DIRECTORY = null;
  
  /**
   * Return the master (or null if it doesn't exist).
   * 
   * @return the master or null if it doesn't exist.
   */
  public static IndexMaster master() {
    if (INDEX_DIRECTORY == null) INDEX_DIRECTORY = new File(GlobalSettings.getRepository(), "index");
    return MultipleIndex.getMaster(INDEX_DIRECTORY);
  }
  
  /**
   * Return the master (will create it if it doesn't exist and XSLT is not null).
   * 
   * @param indexDir the index directory
   * @param xslt     the location of the XSLT generating the IXML.
   * 
   * @return the master
   */
  public static IndexMaster setupMaster(File xslt) {
    if (INDEX_DIRECTORY == null) INDEX_DIRECTORY = new File(GlobalSettings.getRepository(), "index");
    return MultipleIndex.setupMaster(INDEX_DIRECTORY, xslt);
  }
}
