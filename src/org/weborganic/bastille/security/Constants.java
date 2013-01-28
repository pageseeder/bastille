/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.weborganic.bastille.security;

/**
 * A collection of constants related to security.
 *
 * @author Christophe Lauret
 * @version 0.6.2 - 7 April 2011
 * @since 0.6.2
 */
public final class Constants {

  /** Utility class */
  private Constants() {
  }

  /**
   * The name of the attribute that contains the User currently logged in.
   */
  public static final String SESSION_USER_ATTRIBUTE = "org.weborganic.bastille.security.User";

  /**
   * The name of the attribute that contains the request to a protected resource.
   */
  public static final String SESSION_REQUEST_ATTRIBUTE = "org.weborganic.bastille.security.HttpRequest";

}
