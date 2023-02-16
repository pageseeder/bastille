/*
 * Copyright 2015 Allette Systems (Australia)
 * http://www.allette.com.au
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pageseeder.bastille.cache.filter;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pageseeder.bastille.cache.util.CachedResource;
import org.pageseeder.bastille.cache.util.CachedResponseWrapper;
import org.pageseeder.bastille.cache.util.GenericResource;
import org.pageseeder.bastille.cache.util.HttpDateFormat;
import org.pageseeder.bastille.cache.util.StaticRequestWrapper;
import org.pageseeder.bastille.cache.util.StaticResource;
import org.pageseeder.berlioz.http.HttpHeaderUtils;
import org.pageseeder.berlioz.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.constructs.blocking.LockTimeoutException;

/**
 * A caching filter for static resources such as scripts, images, styles, etc..
 *
 * <p>This filter is designed to be used for URIs which map directly to a file on the server as
 * it will check whether the file has been changed and update the cache accordingly.
 * These resources are typically served by the default Servlet on the Web container.
 *
 * <h3>GZip compression</h3>
 * <p>This filter will automatically compress resources such as styles and scripts while leaving
 * images uncompressed. This is done based on the media type of the resource.
 * <p>If this use agent does not support GZip encoding, the resource is served uncompressed.
 *
 * <h3>Cache Key</h3>
 * <p>The key for each resource is the path component of the URI. The scheme, host port, query
 * string and fragment are ignored. The HTTP method is also ignored, so that <code>GET</code>
 * and <code>POST</code> methods are equivalent.
 *
 * @author Christophe Lauret
 *
 * @version Bastille 0.11.0
 */
public final class StaticCachingFilter extends CachingFilterBase implements CachingFilter {

  /**
   * The default of the cache to use.
   */
  public static final String DEFAULT_CACHE_NAME = "StaticCachingFilter";

  /**
   * The default cache control to use.
   */
  public static final String DEFAULT_CACHE_CONTROL = "max-age=%TTL, must-revalidate";

  /**
   * The default file size threshold.
   *
   * <p>Beyond this files are not cached but served directly.
   */
  public static final long DEFAULT_FILESIZE_THRESHOLD = 1024*1024L;

  /**
   * The name of the attribute on the request to store the file corresponding to the resource
   */
  private static final String FILE_REQUEST_ATTRIBUTE = StaticCachingFilter.class.getName()+".File";

  /** Logger for this class. */
  private static final Logger LOGGER = LoggerFactory.getLogger(StaticCachingFilter.class);

  /** Useful constant */
  private static final long ONE_YEAR_IN_MILLISECONDS = 60 * 60 * 24 * 365 * 1000L;

  /** Useful constant */
  private static final int MILLISECONDS_PER_SECOND = 1000;

  /** Formatter for HTTP dates. */
  private HttpDateFormat httpDateFormatter;

  /** The cache control pattern */
  private String cacheControlPattern;

  /** The cache control pattern */
  private long sizeThreshold;

  /**
   * The servlet context.
   */
  private ServletContext _context = null;

  @Override
  public CacheManager getCacheManager() {
    return CacheManager.getInstance();
  }

  @Override
  public String getDefaultCacheName() {
    return DEFAULT_CACHE_NAME;
  }

  @Override
  public void init(FilterConfig config) throws CacheException {
    super.init(config);
    this._context = config.getServletContext();
    // Setting the Cache-Control pattern
    String cc = config.getInitParameter("cache-control");
    if (cc != null && cc.length() > 0) {
      this.cacheControlPattern = cc;
    } else {
      this.cacheControlPattern = DEFAULT_CACHE_CONTROL;
    }
    LOGGER.debug("Using Cache-Control: {}", this.cacheControlPattern);
    // Setting the threshold for the file size
    String st = config.getInitParameter("filesize-threshold");
    if (st != null && st.length() > 0) {
      // TODO handle parsing errors
      this.sizeThreshold = Long.parseLong(st);
    } else {
      this.sizeThreshold = DEFAULT_FILESIZE_THRESHOLD;
    }
    this.sizeThreshold = 1024*1024L;
  }

  @Override
  public void destroy() {
  }

  /**
   * Get the requested resource either from the cache or by invoking the page directly.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public CachedResource getResource(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws ServletException, CacheException {
    // Look up the cached page
    String key = calculateKey(req);
    CachedResource resource = null;
    boolean doBuild = true;
    try {
      resource = getResourceFromCache(key);
      // We've got a cached resource, let's check for freshness
      if (resource != null) {

        // Get last modified date of resource (rounded to the second)
        long modified = resource.getLastModified() / MILLISECONDS_PER_SECOND;

        // Get last modified from file (also rounded to the second)
        File f = getResourceFile(this._context, req);
        long fmodified = f == null? 0 : f.lastModified() / MILLISECONDS_PER_SECOND;

        // Check for freshness
        if (fmodified > modified || fmodified == 0) {
          LOGGER.debug("Resource {} updated since last cached", key);
        } else {
          doBuild = false;
        }
      }

      // Let's invoke the underlying page
      if (doBuild) {
        Ehcache cache = getCache();
        try {
          // Page is not cached - build the response, cache it, and send to client
          resource = buildResource(req, res, chain);
          if (resource.isOK()) {
            LOGGER.debug("Resource OK (200) - adding to cache {} with key {}", cache.getName(), key);
            cache.put(new Element(key, resource));
          } else {
            LOGGER.debug("Resource was not OK(200) - putting null into cache {} with key {}", cache.getName(), key);
            // Must unlock the cache by inserting null element
            cache.put(new Element(key, null));
          }
        } catch (Throwable throwable) {
          // Must unlock the cache by inserting null element
          cache.put(new Element(key, null));
          throw new ServletException(throwable);
        }
      }
    } catch (LockTimeoutException ex) {
      // Do not release the lock since we never acquired it
      throw ex;
    }
    return resource;
  }

  /**
   * Generate the cached resource
   *
   * <p>
   * The following headers are set:
   * <ul>
   * <li>Last-Modified
   * <li>Expires
   * <li>Cache-Control
   * <li>ETag
   * </ul>
   * Any of these headers already set in the response are ignored, and new ones generated. To control
   * your own caching headers, use {@link StaticCachingFilter}.
   * <p>
   *
   * @param req   The HTTP Servlet request
   * @param res   The HTTP Servlet response
   * @param chain THe Servlet chain
   *
   * @return the cache resource
   *
   * @throws IOException      For I/O errors only
   * @throws ServletException For all other errors.
   */
  private CachedResource buildResource(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws IOException, ServletException {

    // Invoke the next entity in the chain
    StaticRequestWrapper q = new StaticRequestWrapper(req);
    CachedResponseWrapper r = new CachedResponseWrapper(res);
    chain.doFilter(q, r);
    r.flush();

    // Is it worth compressing?
    CachedResource resource;

    // OK we can build a static resource
    if (r.getStatus() == HttpServletResponse.SC_OK && !r.isCommitted()) {

      // Return a static cached resource
      LOGGER.debug("Building static cached resource for {}", req.getRequestURI());
      long lastModified = r.getDateHeader(HttpHeaders.LAST_MODIFIED);
      long ttlMilliseconds = computeTimeToLiveMilliseconds(getCache());
      String cacheControl = this.cacheControlPattern.replaceAll("%TTL", Long.toString(ttlMilliseconds / MILLISECONDS_PER_SECOND));
      long expires = System.currentTimeMillis() + ttlMilliseconds;
      resource = new StaticResource(r.getStatus(), r.getContentType(), r.toByteArray(), lastModified, cacheControl, expires);

    } else {

      LOGGER.debug("Building generic cached resource {}", req.getRequestURI());
      boolean gzip = HttpHeaderUtils.isCompressible(r.getContentType());
      resource = new GenericResource(r.getStatus(), r.getContentType(), r.toByteArray(), gzip, r.getAllHeaders());

    }
    return resource;

  }

  /**
   * Always return <code>true</code> unless the "berlioz-cache" parameter is set to "false"
   * or the file is too large or does not exist.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public boolean isCacheable(HttpServletRequest req) {
    // Cache disabled by parameter
    if ("false".equals(req.getParameter("berlioz-cache"))) return false;
    // Check the file
    File f = getResourceFile(this._context, req);
    return f != null && (f == null || f.length() <= this.sizeThreshold);
  }

  /**
   * @return A lazily created HttpDateFormatter instance scoped to this filter.
   */
  protected HttpDateFormat getHttpDateFormatter() {
    if (this.httpDateFormatter == null) {
      // Delay init since SimpleDateFormat is expensive to create
      this.httpDateFormatter = new HttpDateFormat();
    }
    return this.httpDateFormatter;
  }

  /**
   * Writes the response from a PageInfo object.
   *
   * <p>This method actually performs the conditional GET and returns 304 if not modified,
   * short-circuiting the normal response.
   *
   * @param req The HTTP servlet request.
   * @param res the HTTP servlet response.
   * @param resource The cached resource build previously.
   *
   * @throws IOException      For I/O errors only
   * @throws ServletException For all other errors.
   */
  @Override
  public void writeResponse(HttpServletRequest req, HttpServletResponse res, CachedResource resource)
      throws IOException, ServletException {

    boolean sendGzip = resource.hasContent() && resource.hasGzippedBody() && HttpHeaderUtils.acceptsGZipCompression(req);

    if (resource instanceof StaticResource) {

      // Reset the headers
      res.reset();

      // Check "If-None-Match" header
      String ifNoneMatch = req.getHeader(HttpHeaders.IF_NONE_MATCH);
      if (ifNoneMatch != null) {
        String etag = resource.getETag(ifNoneMatch.contains("-gzip"));
        if (etag.equals(ifNoneMatch)) {
          LOGGER.debug("Returning Not Modified (304) for {} from {}", req.getRequestURI(), HttpHeaders.IF_NONE_MATCH);
          resource.copyHeadersTo(res, sendGzip);
          res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
          res.flushBuffer();
          return;
        }
      }

      // Check "If-Modified-Since" header
      long ifModifiedSince = req.getDateHeader(HttpHeaders.IF_MODIFIED_SINCE);
      if (ifModifiedSince != -1) {
        Date requestDate = new Date(ifModifiedSince);
        Date resourceDate = new Date(resource.getLastModified());
        if (!requestDate.before(resourceDate)) {
          LOGGER.debug("Returning Not Modified (304) for {} from ", req.getRequestURI(), HttpHeaders.IF_MODIFIED_SINCE);
          resource.copyHeadersTo(res, sendGzip);
          res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
          res.flushBuffer();
          return;
        }
      }

      // Normal process
      LOGGER.debug("Writing response OK (200) for {}", req.getRequestURI());
      res.setStatus(resource.getStatusCode());
      String contentType = resource.getContentType();
      if (contentType != null && contentType.length() > 0) {
        res.setContentType(contentType);
      }
      resource.copyHeadersTo(res, sendGzip);
      res.setCharacterEncoding("utf-8");
      writeContent(req, res, resource);
    }

  }

  /**
   * Returns the key for this cache.
   *
   * @param req the HTTP Servlet request.
   * @return the request URI.
   */
  @Override
  public String calculateKey(HttpServletRequest req) {
    StringBuilder key = new StringBuilder();
    key.append(req.getMethod()).append('_').append(req.getRequestURI());
    return key.toString();
  }

  // Private helpers
  // ----------------------------------------------------------------------------------------------

  /**
   * Returns the file corresponding to the resource specified in the request.
   *
   * @param context the servlet context
   * @param req     the HTTP servlet request.
   *
   * @return The corresponding file or <code>null</code> if the file path could not be guessed
   */
  private static File getResourceFile(ServletContext context, HttpServletRequest req) {
    File f = (File)req.getAttribute(FILE_REQUEST_ATTRIBUTE);
    if (f == null) {
      String filepath = context.getRealPath(decode(req.getRequestURI()));
      // filepath may be null on Windows due to case sensitivity, pfff...
      if (filepath != null) {
  //      LOGGER.debug("Resource path: {}", filepath);
        f = new File(filepath);
        req.setAttribute(FILE_REQUEST_ATTRIBUTE, f);
      }
    }
    return f;
  }

  /**
   * Get the time to live for a page, in milliseconds
   *
   * @param cache The cache instance
   * @return time to live in milliseconds
   */
  private static long computeTimeToLiveMilliseconds(Ehcache cache) {
    if (cache.isDisabled()) return -1;
    else {
      CacheConfiguration config = cache.getCacheConfiguration();
      if (config.isEternal()) return ONE_YEAR_IN_MILLISECONDS;
      else return config.getTimeToLiveSeconds() * MILLISECONDS_PER_SECOND;
    }
  }

  /**
   * Try to decoded a URL encoded path.
   *
   * @param encoded the encoded path
   * @return the decoded path
   */
  private static String decode(String encoded) {
    try {
      return URLDecoder.decode(encoded, "utf-8");
    } catch (IllegalArgumentException ex) {
      // Might simply be a badly written path
      return encoded;
    } catch (UnsupportedEncodingException ex) {
      // XXX: maybe we should throw an exception instead
      return encoded;
    }
  }
}
