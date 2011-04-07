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
   * @param req The user name
   * 
   * @return The corresponding user or <code>null</code>.
   * 
   * @throws IOException if any error occurs while trying to login.
   */
  public boolean login(HttpServletRequest req) throws IOException;

  /**
   * Logs the specified user out.
   * 
   * @return <code>true</code> if the logout request succeeded, <code>false</code> otherwise.
   */
  public boolean logout(HttpServletRequest req) throws IOException;

  /**
   * @param username The user name
   * @param password the user password
   * 
   * @return The corresponding user or <code>null</code>.
   * 
   * @throws IOException if any error occurs while trying to login.
   */
  public User login(String username, String password) throws IOException;

  /**
   * Logs the specified user out.
   * 
   * @return <code>true</code> if the logout request succeeded, <code>false</code> otherwise.
   */
  public boolean logout(User user) throws IOException;

}
