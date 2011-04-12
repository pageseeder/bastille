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
 * @version 8 April 2011
 */
public final class SecurityFilter implements Filter {

  /**
   * Do nothing.
   * 
   * {@inheritDoc}
   */
  public void init(FilterConfig config) throws ServletException {
  }

  /**
   * Do nothing.
   * 
   * {@inheritDoc}
   */
  public void destroy() {
  }

  /**
   * Does the filtering.
   * 
   * {@inheritDoc}
   */
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
    // Use HTTP specific requests.
    doHttpFilter((HttpServletRequest)req, (HttpServletResponse)res, chain);
  }

  /**
   * Does the filtering.
   * 
   * @param req   the HTTP servlet request
   * @param res   the HTTP servlet response
   * @param chain The filter chain
   */
  private void doHttpFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {

    // Retrieve the user from the session
    HttpSession session = req.getSession(true);
    Object o = session.getAttribute(Constants.SESSION_USER_ATTRIBUTE);

    // The user is authenticated
    if (o instanceof User) {

      // Get relevant URI.
      String uri = req.getRequestURI();

      // Invoke Authorizer method to see if user can access resource.
      Authorizer authorizer = LoggedInAuthorizer.getInstance();
      AuthorizationResult result = authorizer.isUserAuthorized((User)o, uri);
      if (result == AuthorizationResult.AUTHORIZED) {
        chain.doFilter(req, res);
      } else {
        res.sendError(HttpServletResponse.SC_FORBIDDEN);
      }

    // The user has not been authenticated yet
    } else {
      session = req.getSession(true);
      ProtectedRequest target = new ProtectedRequest(req.getRequestURL().toString());
      session.setAttribute(Constants.SESSION_REQUEST_ATTRIBUTE, target);
      res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

  }
}