/*
 * Copyright (c) 2011 weborganic systems pty. ltd.
 */
package com.weborganic.bastille.security;

import java.io.IOException;
import java.net.ConnectException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.weborganic.bastille.security.ps.PageSeederAuthenticator;

/**
 * A servlet to login.
 * 
 * <p>This servlet actually performs the login using an authenticator. 
 * 
 * <h3>Initialisation parameters</h3>
 * <p>See {@link #init(ServletConfig)}.
 * 
 * @author Christophe Lauret
 * @version 0.6.15 - 16 September 2011 
 * @since 0.6.2
 */
public final class LoginServlet extends HttpServlet {

  /**
   * As per requirement for the Serializable interface.
   */
  private static final long serialVersionUID = -5279152811865484362L;

  /**
   * The logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(LoginServlet.class);

  /**
   * The URI of the default target page.
   */
  protected static final String DEFAULT_TARGET = "/";

  /**
   * The URI of the login page.
   */
  private String loginPage = null;

  /**
   * The URI of the default target page.
   */
  private String defaultTarget = DEFAULT_TARGET;

  /**
   * This Servlet accepts two initialisation parameters.
   * 
   * <p><code>login-page</code> is required and should point to the login page.
   * <p><code>default-target</code> is optional and should point to the default target after login,
   * defaults to "/".
   * 
   * {@inheritDoc}
   */
  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    this.loginPage = config.getInitParameter("login-page");
    this.defaultTarget = config.getInitParameter("default-target");
    if (this.defaultTarget == null)
      this.defaultTarget = DEFAULT_TARGET;
  }

  @Override
  public void destroy() {
    super.destroy();
    this.loginPage = null;
    this.defaultTarget = DEFAULT_TARGET;
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    // Get the authenticator
    HttpSession session = req.getSession();
    res.setStatus(HttpServletResponse.SC_NO_CONTENT);
    if (session != null) {
      User user = (User)session.getAttribute(Constants.SESSION_USER_ATTRIBUTE);
      if (user != null) {
        res.setHeader("X-Bastille-User", user.getName());
        return;
      }
    }
    res.setHeader("X-Bastille-User", "Anonymous");
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

    // Get the authenticator
    HttpSession session = req.getSession(true);
    Object target = session.getAttribute(Constants.SESSION_REQUEST_ATTRIBUTE);

    // Perform login
    try {
      PageSeederAuthenticator authenticator = new PageSeederAuthenticator();
      AuthenticationResult result = authenticator.login(req);
      LOGGER.debug("Login User: {}", result);

      // Logged in successfully
      if (result == AuthenticationResult.LOGGED_IN || result == AuthenticationResult.ALREADY_LOGGED_IN) {

        // Forward the original request
        if (target != null) {
          LOGGER.debug("Redirecting to {}", target.toString());
          res.sendRedirect(target.toString());

        } else {
          LOGGER.debug("Redirecting to {}", this.defaultTarget);
          res.sendRedirect(this.defaultTarget);
        }

      // Login failed
      } else {
        if (target != null) {
          session = req.getSession(true);
          session.setAttribute(Constants.SESSION_REQUEST_ATTRIBUTE, target);
        }
        if (this.loginPage != null) {
          LOGGER.debug("Redirecting to "+this.loginPage+"?message=Login failed");
          res.sendRedirect(this.loginPage+"?message=Login failed");
        } else {
          res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Login failed");
        }
      }

    } catch (ConnectException ex) {

      // Unable to connect to PageSeeder
      final int badGateway = 502; 
      res.sendError(badGateway, ex.getMessage());
    }

  }

}
