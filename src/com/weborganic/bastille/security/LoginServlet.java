package com.weborganic.bastille.security;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
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
 * @author Christophe Lauret
 * @version 7 April 2011
 */
public class LoginServlet extends HttpServlet {

  /**
   * The logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(LoginServlet.class);

  /**
   * Servlet context to get the dispatcher.
   */
  private ServletContext context = null;

  /**
   * The URI of the login page.
   */
  private String loginPage = null;

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    this.context = config.getServletContext();
    this.loginPage = config.getInitParameter("login-page");
  }

  @Override
  public void destroy() {
    // TODO Auto-generated method stub
    super.destroy();
    this.context = null;
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
        LOGGER.debug("Redirecting to /");
        res.sendRedirect("/");
      }

    // Login failed
    } else {
      if (target != null) {
        session = req.getSession(true);
        session.setAttribute(Constants.SESSION_REQUEST_ATTRIBUTE, target);
      }
      LOGGER.debug("Redirecting to "+this.loginPage+"?message=Login failed");
      res.sendRedirect(this.loginPage+"?message=Login failed");
    }

  }

}
