/*
 * This file is part of the Bastille library.
 *
 * Available under a commercial licence, contact Weborganic.
 *
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package com.weborganic.bastille.flint.helpers;



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
   * Return the master (or <code>null</code> if it doesn't exist).
   *
   * @return the master or <code>null</code> if it doesn't exist.
   */
  public static IndexMaster master() {
    return MultipleIndex.getMaster(FlintConfig.directory());
  }

}
