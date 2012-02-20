/*
 * This file is part of the Bastille library.
 *
 * Available under a commercial licence, contact Weborganic.
 *
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package com.weborganic.bastille.security;

/**
 * Defines whether is allowed to access specific resources.
 *
 * @author Christophe Lauret
 * @version 0.6.2 - 8 April 2011
 * @since 0.6.2
 */
public interface Authorizer {

  /**
   * Indicates whether a user is allowed to access a given resource.
   *
   * @param user A user.
   * @param uri  The URI the user is trying to access.
   *
   * @return <code>true</code> is the user can access the resource;
   *         <code>false</code> otherwise.
   */
  AuthorizationResult isUserAuthorized(User user, String uri);

}
