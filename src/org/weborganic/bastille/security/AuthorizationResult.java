/*
 * This file is part of the Bastille library.
 *
 * Available under a commercial licence, contact Weborganic.
 *
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package org.weborganic.bastille.security;

/**
 * An enumeration for the results of authorization functions.
 *
 * @author Christophe Lauret
 * @version 0.6.2 - 8 April 2011
 * @since 0.6.2
 */
public enum AuthorizationResult {

  /**
   * The user is authorised to access the resource.
   */
  AUTHORIZED,

  /**
   * The user is not authorised to access the resource due to lack of credentials.
   * (for example, if the user is not logged in).
   */
  UNAUTHORIZED,

  /**
   * The user is not authorised to access the resource due to lack of credentials.
   * (The user is logged in, but he is not allowed to access the resource)
   */
  FORBIDDEN;

}
