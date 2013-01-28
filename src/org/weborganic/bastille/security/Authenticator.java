/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.weborganic.bastille.security;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.weborganic.berlioz.Beta;

/**
 * Must provide the login and logout mechanisms.
 *
 * @author Christophe Lauret
 *
 * @version 0.8.1 - 19 December 2012
 * @since 0.6.2
 */
@Beta public interface Authenticator {

  /**
   * Logs the specified user in.
   *
   * <p>The servlet request must contain the details sufficient to login (eg. parameters, headers).
   *
   * <p>Implementations should specify which details are required to login.
   *
   * @param req the HTTP Servlet Request that contains the details sufficient to login.
   *
   * @return The result of this authentication process.
   *
   * @throws IOException if any error occurs while trying to login.
   */
  AuthenticationResult login(HttpServletRequest req) throws IOException;

  /**
   * Logs the specified user out.
   *
   * @param req The HTTP srevlet request.
   *
   * @return The result of this authentication process..
   *
   * @throws IOException Should an error occur while logging out.
   */
  AuthenticationResult logout(HttpServletRequest req) throws IOException;

  /**
   * Logs the specified user out.
   *
   * @param user Logout the specified user.
   *
   * @return <code>true</code> if the logout request succeeded, <code>false</code> otherwise.
   *
   * @throws IOException Should an error occur while logging the user out.
   */
  boolean logout(User user) throws IOException;

}
