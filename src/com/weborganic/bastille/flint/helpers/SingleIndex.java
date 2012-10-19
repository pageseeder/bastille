/*
 * This file is part of the Bastille library.
 *
 * Available under a commercial licence, contact Weborganic.
 *
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package com.weborganic.bastille.flint.helpers;

import com.weborganic.bastille.flint.config.FlintConfig;



/**
 * Centralises all the indexing and searching function using Flint for one index.
 *
 * <p>This class defines a singleton which can be access using the {@link #getInstance()} method.
 *
 * @deprecated Use methods in {@link FlintConfig} instead.
 *
 * @author Christophe Lauret
 * @author Jean-Baptiste Reure
 *
 * @version 0.7.4 - 19 October 2012
 * @since 0.6.0
 */
@Deprecated
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
