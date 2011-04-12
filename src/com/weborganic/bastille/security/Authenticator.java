package com.weborganic.bastille.security;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.weborganic.berlioz.Beta;

/**
 * Must provide the login and logout mechanisms.
 * 
 * @author Christophe Lauret
 * @version 7 April 2011
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
  public AuthenticationResult login(HttpServletRequest req) throws IOException;

  /**
   * Logs the specified user out.
   * 
   * @return The result of this authentication process..
   */
  public AuthenticationResult logout(HttpServletRequest req) throws IOException;

  /**
   * Logs the specified user out.
   * 
   * @return <code>true</code> if the logout request succeeded, <code>false</code> otherwise.
   */
  public boolean logout(User user) throws IOException;

}