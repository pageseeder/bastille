package com.weborganic.bastille.security;

/**
 * A collection of constants related to security.
 * 
 * 
 * @author Christophe Lauret
 * @version 7 April 2011
 */
public final class Constants {

  /**
   * The name of the attribute that contains the User currently logged in.
   */
  public final static String SESSION_USER_ATTRIBUTE = "com.weborganic.bastille.security.User";

  /**
   * The name of the attribute that contains the request to a protected resource.
   */
  public final static String SESSION_REQUEST_ATTRIBUTE = "com.weborganic.bastille.security.HttpRequest";

}
