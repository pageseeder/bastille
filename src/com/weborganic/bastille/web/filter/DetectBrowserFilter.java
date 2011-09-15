/*
 *  Copyright (c) 2011 Allette Systems pty. ltd.
 */
package com.weborganic.bastille.web.filter;

import java.io.IOException;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>A Simpler filter class for redirect URL based on User-Agent to different URL.</p>
 * 
 * <h3>Configuration </h3>
 * 
 * 
 * <p>Sample config in Web Configuration (web.xml)</p>
 * <pre>
 * {@code
 * <filter>
 *    <filter-name>MobileFilter</filter-name>
 *    <filter-class>com.weborganic.bastille.web.filter.DetectBrowserFilter</filter-class>
 *     <init-param>
 *       <param-name>mobile</param-name>
 *       <param-value>http://m.pbs.gov.au</param-value>
 *     </init-param>
 *     <init-param>
 *       <param-name>normal</param-name>
 *       <param-value>http://www.pbs.gov.au</param-value>
 *    </init-param>
 *  </filter>
 * }
 * </pre>
 * 
 * @author Christophe Lauret
 * @author Ciber Cai
 * 
 * @version 0.6.13 - 15 September 2011
 * @since 0.6.13
 */
public final class DetectBrowserFilter implements Filter {

  /**
   * Mobile user agents match this pattern.
   */
  private static final Pattern MOBILE_UA = Pattern.compile(".*(android|avantgo|blackberry|blazer|compal|elaine|fennec|hiptop|iemobile|ip(hone|od)|iris|kindle|lge |maemo|midp|mmp|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)\\/|plucker|pocket|psp|symbian|treo|up\\.(browser|link)|vodafone|wap|windows (ce|phone)|xda|xiino).*");

  /**
   * The mobile site address
   */
  private String mobile="";

  /**
   * The normal site address
   */
  private String normal= "";

  /**
   * Initialises the filter.
   * 
   * <p>This servlet accepts the following initialisation parameters:
   * <ul>
   *   <li><code>mobile</code> The mobile site address (eg. 'http://m.acme.gov.au')
   *   <li><code>normal</code> The normal site address (eg. 'http://www.acme.gov.au')
   * </ul>
   * 
   * <p>Both parameters MUST be valid URLs.
   * 
   * @param config The filter configuration.
   */
  @Override
  public void init(FilterConfig config) throws ServletException {
    this.mobile = config.getInitParameter("mobile");
    this.normal = config.getInitParameter("normal");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void destroy() {
    this.mobile = null;
    this.normal = null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
    if (req instanceof HttpServletRequest && res instanceof HttpServletResponse) {
      doHTTPFilter((HttpServletRequest)req, (HttpServletResponse)res, chain);
    }
  }

  /**
   * Do the filtering for a HTTP request.
   * 
   * @param req   The HTTP servlet request.
   * @param res   The HTTP servlet response.
   * @param chain The filter chain.
   * 
   * @throws IOException      Should an error occurs while writing the response.
   * @throws ServletException If thrown by the filter chain.
   */
  public void doHTTPFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
  throws ServletException, IOException {
    String userAgent = req.getHeader("User-Agent").toLowerCase();
    String requrl = req.getRequestURL().toString();

    // A Mobile and there is a mobile Website
    if (isMobile(userAgent) && this.mobile != null && !this.mobile.isEmpty()){
      // protect die loop
      if (requrl.toLowerCase().contains(this.mobile.toLowerCase())){
        chain.doFilter(req, res);
      } else {
        res.sendRedirect(this.mobile);
      }

    // A Desktop and there is a desktop Website
    } else if (!isMobile(userAgent) && this.normal != null && !this.normal.isEmpty()){
      // protect die loop
      if (requrl.toLowerCase().contains(this.normal.toLowerCase())){
        chain.doFilter(req, res);
      } else {
        res.sendRedirect(this.normal);
      }

    } else {
      chain.doFilter(req, res);
    }
  }

  /**
   * 
   * Indicates whether the specified user agent is a mobile device or not.
   * 
   * @see <a href="http://detectmobilebrowser.com/">Detect Mobile Browser | Open source mobile phone detection</a>
   * 
   * @param userAgent The <code>User-Agent</code> HTTP header string.
   * @return <code>true</code> if the user agent string matches that of a mobile device;
   *         <code>false</code> otherwise.
   */
  public static boolean isMobile(String userAgent) {
    if (userAgent == null) return false;
    String ua = userAgent.toLowerCase();
    return MOBILE_UA.matcher(ua).matches();
  }

}
