/*
 * This file is part of the Bastille library.
 *
 * Available under a commercial licence, contact Weborganic.
 *
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package com.weborganic.bastille.security;

/**
 * An authorizer which only requires the user to be logged in.
 *
 * @author Christophe Lauret
 * @version 0.6.2 - 8 April 2011
 * @since 0.6.2
 */
public final class LoggedInAuthorizer implements Authorizer {

  /**
   * The singleton instance.
   */
  private static final LoggedInAuthorizer SINGLETON = new LoggedInAuthorizer();

  /**
   * No need to allow creation of this class.
   */
  private LoggedInAuthorizer() {
  }

  /**
   * Always returns <code>AUTHORIZED</code> if the specified user is not <code>null</code>.
   *
   * @param user A user.
   * @param uri  The URI the user is trying to access.
   *
   * @return <code>AUTHORIZED</code> is the user is not <code>null</code>;
   *         <code>UNAUTHORIZED</code> otherwise.
   */
  @Override
  public AuthorizationResult isUserAuthorized(User user, String uri) {
    return user != null? AuthorizationResult.AUTHORIZED : AuthorizationResult.UNAUTHORIZED;
  }

  /**
   * @return a singleton instance.
   */
  public static LoggedInAuthorizer getInstance() {
    return SINGLETON;
  }

}
