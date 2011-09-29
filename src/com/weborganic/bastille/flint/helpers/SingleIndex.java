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
 * 
 * @version 0.6.20 - 26 September 2011
 * @since 0.6.0
 */
public final class SingleIndex {

  /**
   * Utility class.
   */
  private SingleIndex() {
  }

  /**
   * Return the master (or null if it doesn't exist).
   * 
   * @return the master or null if it doesn't exist.
   */
  public static IndexMaster master() {
    return MultipleIndex.getMaster(FlintConfig.directory());
  }

  /**
   * Return the master (will create it if it doesn't exist and XSLT is not null).
   * 
   * @param xslt The location of the XSLT generating the IXML.
   * 
   * @return the master
   */
  public static IndexMaster setupMaster(File xslt) {
    return MultipleIndex.setupMaster(FlintConfig.directory(), xslt);
  }

}
