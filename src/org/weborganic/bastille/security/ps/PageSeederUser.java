/*
 * This file is part of the Bastille library.
 *
 * Available under a commercial licence, contact Weborganic.
 *
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package org.weborganic.bastille.security.ps;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weborganic.bastille.pageseeder.PSUser;
import org.weborganic.bastille.security.Constants;
import org.weborganic.bastille.security.Obfuscator;
import org.weborganic.bastille.security.User;
import org.weborganic.berlioz.GlobalSettings;
import org.weborganic.berlioz.content.ContentRequest;

/**
 * Represents a PageSeeder User.
 *
 * @deprecated Use {@link PSUser} instead.
 *
 * @author Christophe Lauret
 *
 * @version 0.8.1 - 19 December 2012
 * @since 0.6.2
 */
@Deprecated
public final class PageSeederUser extends PSUser implements User, Serializable {

  /**
   *As per requirement for the {@link Serializable} interface.
   */
  private static final long serialVersionUID = 4229779545161938662L;

  /**
   * Create a new PageSeeder user.
   *
   * @param id The PageSeeder user ID.
   */
  public PageSeederUser(Long id) {
    super(id);
  }

  // Setters
  // ----------------------------------------------------------------------------------------------

  /**
   * @param username the PageSeeder username for this user.
   */
  @Override
  protected void setUsername(String username) {
    super.setUsername(username);
  }

  /**
   * @param jsessionid the PageSeeder username for this user.
   */
  @Override
  protected void setJSessionId(String jsessionid) {
    super.setJSessionId(jsessionid);
  }

  /**
   * @param firstname the PageSeeder username for this user.
   */
  @Override
  protected void setFirstname(String firstname) {
    super.setFirstname(firstname);
  }

  /**
   * @param surname the PageSeeder username for this user.
   */
  @Override
  protected void setSurname(String surname) {
    super.setSurname(surname);
  }

  /**
   * @param email the PageSeeder username for this user.
   */
  @Override
  protected void setEmail(String email) {
    super.setEmail(email);
  }

  /**
   * @param groups the PageSeeder groups the user is a member of.
   */
  @Override
  protected void setMemberOf(List<String> groups) {
    super.setMemberOf(groups);
  }

  /**
   * Returns the PageSeeder user that is currently logged in.
   *
   * @deprecated Use {@link org.weborganic.bastille.pageseeder.PSUsers#getUser(ContentRequest)} instead
   *
   * @param req the content request.
   * @return The PageSeeder user or <code>null</code> if it is not configured properly or could not login.
   */
  @Deprecated
  public static PageSeederUser getUser(ContentRequest req) {
    HttpSession session = req.getSession();
    Object o = null;
    // Try to get the user from the session (if logged in)
    if (session != null) {
      o = session.getAttribute(Constants.SESSION_USER_ATTRIBUTE);
    }
    // try to cast from PageSeeder user
    if (o instanceof PageSeederUser) {
      return (PageSeederUser)o;
    } else {
      return null;
    }
  }

  /**
   * Returns the user from the property stored in the global settings.
   *
   * <p>This class will log the user to PageSeeder to retrieve his info.
   *
   * @deprecated Use {@link org.weborganic.bastille.pageseeder.PSUsers#get(String)} instead.
   *
   * @param property The property of the PageSeeder user.
   *
   * @return The user or <code>null</code> if it is not configured properly or could not login.
   *
   * @throws IOException Should an error occur while attempting login
   */
  @Deprecated
  public static PageSeederUser get(String property) throws IOException {
    String username = GlobalSettings.get(property+".username");
    String password = GlobalSettings.get(property+".password");
    if (password.startsWith("OB1:")) {
      password = Obfuscator.clear(password.substring(4));
    } else {
      Logger logger = LoggerFactory.getLogger(PSUser.class);
      logger.warn("Config property \""+property+".password\" left in clear - consider obfuscating.");
    }
    PageSeederUser user = PageSeederAuthenticator.login(username, password);
    return user;
  }

}
