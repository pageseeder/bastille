package com.weborganic.bastille.security;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Filters request and check that the user has access to the underlying resource.
 * 
 * @author Christophe Lauret
 * @version 7 April 2011
 */
public class SecurityFilter implements Filter {

  /**
   * The URL to redirect to if the user is unauthorised.
   */
  private String unauthorized = null;

  /**
   * The URL to redirect to if the user is authorised but to .
   */
  private String forbidden = null;

  /**
   * Do nothing.
   * 
   * {@inheritDoc}
   */
  public void init(FilterConfig config) throws ServletException {
    if (config != null) {
      this.unauthorized = config.getInitParameter("unauthorized");
      this.forbidden = config.getInitParameter("forbidden");
    }
  }

  /**
   * Do nothing.
   * 
   * {@inheritDoc}
   */
  public void destroy() {
    this.unauthorized = null;
    this.forbidden = null;
  }

  /**
   * Does the filtering.
   * 
   * {@inheritDoc}
   */
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {

    // Retrieve the user from the session
    HttpSession session = ((HttpServletRequest)req).getSession(false);
    Object o = session !=null? session.getAttribute(Constants.SESSION_USER_ATTRIBUTE) : null;

    // The user is authenticated
    if (o instanceof User) {

      // Get relevant URI.              
      String URI = ((HttpServletRequest)req).getRequestURI();

      // Invoke AuthorizationManager method to see if user can access resource.
      boolean authorized = true;// authMgr.isUserAuthorized(currentUser, URI);
      if (authorized) {
        chain.doFilter(req, res);
      } else {
        ((HttpServletResponse)res).sendError(HttpServletResponse.SC_FORBIDDEN);
      }

    // The user has not been authenticated yet
    } else {

      session = ((HttpServletRequest)req).getSession(true);
      session.setAttribute(Constants.SESSION_REQUEST_ATTRIBUTE, req);
      ((HttpServletResponse)res).sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

  }

}
