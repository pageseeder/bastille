/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.pageseeder.bastille.security;

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
