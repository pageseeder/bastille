/*
 * Copyright 2015 Allette Systems (Australia)
 * http://www.allette.com.au
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pageseeder.bastille.security;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.pageseeder.berlioz.Beta;

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
