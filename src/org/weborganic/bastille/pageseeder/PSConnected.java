/*
 * This file is part of the Bastille library.
 *
 * Available under a commercial licence, contact Weborganic.
 *
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package org.weborganic.bastille.pageseeder;

/**
 * Use this annotation to indicate whether a generator will connect to PageSeeder.
 *
 * <p>This annotation has no effect on the annotated class.
 *
 * @author Christophe Lauret
 * @version 0.6.8 - 8 June 2011
 * @since 0.6.8
 */
public @interface PSConnected {

  /**
   * Indicates whether the connection requires the user to be connected.
   *
   * <p>Only set to <code>true</code> if a PageSeeder User is required in the session. If the
   * connection requires a PageSeeder user but is set by the generator, that is does not require
   * to be stored in the session, this flag should be set to <code>false</code>.
   *
   * <p>Defaults to <code>false</code>.
   */
  boolean login() default false;

}
