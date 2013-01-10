/*
 * This file is part of the Bastille library.
 *
 * Available under a commercial licence, contact Weborganic.
 *
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package org.weborganic.bastille.security;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weborganic.bastille.security.ps.PageSeederAuthenticator;


/**
 * A servlet to login.
 *
 * <p>This servlet actually performs the login using an authenticator.
 *
 * <h3>Initialisation parameters</h3>
 * <p>See {@link #init(ServletConfig)}.
 *
 * @author Christophe Lauret
 * @author Jean-Baptiste Reure
 *
 * @version 0.8.1 - 19 December 2012
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
    HttpSession session = req.getSession();
    String target = getTarget(req);

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
          if (session != null) session.removeAttribute(Constants.SESSION_REQUEST_ATTRIBUTE);

        } else {
          LOGGER.debug("Redirecting to {}", this.defaultTarget);
          String context = req.getContextPath() == null ? "" : req.getContextPath();
          res.sendRedirect(context+this.defaultTarget);
        }

      // Login failed
      } else {
        if (target != null) {
          session = req.getSession(true);
          session.setAttribute(Constants.SESSION_REQUEST_ATTRIBUTE, target);
        }
        if (this.loginPage != null) {
          String ctxt = req.getContextPath() == null ? "" : req.getContextPath();
          LOGGER.debug("Redirecting to "+ctxt+this.loginPage+"?message=Login failed");
          res.sendRedirect(ctxt+this.loginPage+"?message=Login failed");
        } else {
          res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Login failed");
        }
      }

    } catch (ConnectException ex) {

      // Unable to connect to PageSeeder
      res.sendError(HttpServletResponse.SC_BAD_GATEWAY, ex.getMessage());

    }

  }

  /**
   * Filter the target for the login.
   *
   * @param t the target specified in the
   *
   * @return the filtered target.
   */
  private static String getTarget(HttpServletRequest req) {
    HttpSession session = req.getSession();
    String target = null;

    // Check if target in session already
    if (session != null) {
      Object t = session.getAttribute(Constants.SESSION_REQUEST_ATTRIBUTE);
      if (t != null) target = t.toString();
    }

    // No target, let's look for it somewhere else
    if (target == null) {

      // Check if the target was specified in the request
      String t = req.getParameter("target");

      // We've got something, let's see if it's valid
      if (t != null) {
        try {
          // Base URL from servlet container
          URI base = new URI(req.getScheme(), null,req.getServerName(), req.getLocalPort(), "/", null, null);
          URI uri = base.resolve(t);

          // The specified target must match the scheme, host and port of server
          if (base.getScheme().equals(uri.getScheme())
           && base.getHost().equals(uri.getHost())
           && base.getPort() == uri.getPort()) {
            // Write target
            target = uri.getPath();
            if (uri.getQuery() != null) {
              target = target +"?"+uri.getQuery();
            }
            if (uri.getFragment() != null) {
              target = target +"#"+uri.getFragment();
            }
          }
        } catch (IllegalArgumentException ex) {
          LOGGER.warn("Illegal target URL {}", t, ex);
        } catch (URISyntaxException ex) {
          LOGGER.error("Illegal base URL", ex);
        }
      }
    }

    // Hopefully, we've got a target by now...
    return target;
  }

}
