/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.weborganic.bastille.cache.filter;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.constructs.blocking.LockTimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weborganic.bastille.cache.util.CachedResource;
import org.weborganic.bastille.cache.util.CachedResponseWrapper;
import org.weborganic.bastille.cache.util.HttpDateFormat;
import org.weborganic.bastille.cache.util.HttpHeader;
import org.weborganic.bastille.cache.util.StaticRequestWrapper;
import org.weborganic.berlioz.http.HttpHeaderUtils;
import org.weborganic.berlioz.http.HttpHeaders;

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
 * @version Bastille 0.8.3 - 27 January 2013
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
   *
   * {@inheritDoc}
   */
  @Override
  public CachedResource getResource(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws IOException, ServletException, CacheException {
    // Look up the cached page
    String key = calculateKey(req);
    CachedResource resource = null;
    boolean doBuild = true;
    try {
      resource = getResourceFromCache(key);
      // We've got a cached resource, let's check for freshness
      if (resource != null) {

        // Get last modified date from "Etag"
        String etag = resource.getETag();
        long modified = toLastModified(etag);
        if (modified == -1) {
          // Fallback on "Last-Modified"
          modified = resource.getLastModified();
          if (modified != -1)
            modified = modified / MILLISECONDS_PER_SECOND;
        }

        // Get last modified from file
        // TODO: check URL encoding as well
        String filepath = req.getContextPath() + req.getRequestURI();
//        String filepath = req.getServletContext().getRealPath(req.getRequestURI());
        File f = new File(filepath);
        long fmodified = f.lastModified()  / MILLISECONDS_PER_SECOND;

        // Check for freshness
        if (fmodified > modified) {
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
            LOGGER.debug("Resource was not OK(200). Putting null into cache {} with key {}", cache.getName(), key);
            cache.put(new Element(key, null));
          }
        } catch (Throwable throwable) {
          // Must unlock the cache if the above fails. Will be logged at Filter
          cache.put(new Element(key, null));
          throw new ServletException(throwable);
        }
      }
    } catch (LockTimeoutException ex) {
      // do not release the lock, because you never acquired it
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
   * Any of these headers aleady set in the response are ignored, and new ones generated. To control
   * your own caching headers, use {@link StaticCachingFilter}.
   *
   *
   * @param req   The HTTP Servlet request
   * @param res   The HTTP Servlet response
   * @param chain THe Servlet chain
   *
   * @return a Serializable value object for the page or page fragment
   * @throws Exception
   */
  private CachedResource buildResource(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws IOException, ServletException {

    // Invoke the next entity in the chain
    StaticRequestWrapper q = new StaticRequestWrapper(req);
    CachedResponseWrapper r = new CachedResponseWrapper(res);
    chain.doFilter(q, r);
    r.flush();

    // Is it worth compressing?
    boolean gzip = HttpHeaderUtils.isCompressible(r.getContentType());
    r.adjustVaryAcceptEncoding(gzip);

    // Return the page info
    CachedResource resource = new CachedResource(r.getStatus(), r.getContentType(), r.toByteArray(), gzip, r.getAllHeaders());

    List<HttpHeader<? extends Serializable>> headers = resource.getHeaders();
    LOGGER.debug("=== Response headers ===");
    for (HttpHeader<? extends Serializable> header : headers) {
      LOGGER.debug("R {}: {}", header.name(), header.value());
    }

    // Get values required for header values
    long lastModified = resource.getLastModified();
    long ttlMilliseconds = computeTimeToLiveMilliseconds(getCache());
    boolean isGzipped = HttpHeaderUtils.isCompressible(r.getContentType());
    String cacheControl = this.cacheControlPattern.replaceAll("%TTL", Long.toString(ttlMilliseconds / MILLISECONDS_PER_SECOND));

    // Remove any conflicting headers
    for (Iterator<HttpHeader<? extends Serializable>> i = headers.iterator(); i.hasNext();) {
      HttpHeader<? extends Serializable> header = i.next();
      String name = header.name();
      if ("Last-Modified".equalsIgnoreCase(name)
       || "Expires".equalsIgnoreCase(name)
       || "Cache-Control".equalsIgnoreCase(name)
       || "ETag".equalsIgnoreCase(name)
       || "Accept-Ranges".equalsIgnoreCase(name)) {
        i.remove();
      }
    }

    // Set headers
    headers.add(new HttpHeader<Long>("Last-Modified", (lastModified / MILLISECONDS_PER_SECOND) * MILLISECONDS_PER_SECOND));
    headers.add(new HttpHeader<Long>("Expires", System.currentTimeMillis() + ttlMilliseconds));
    headers.add(new HttpHeader<String>("Cache-Control", cacheControl));
    headers.add(new HttpHeader<String>("ETag", toEtag(lastModified / MILLISECONDS_PER_SECOND, isGzipped)));

    return resource;
  }

  /**
   * Always return <code>true</code> unless the "berlioz-cache" parameter is set to false.
   *
   * {@inheritDoc}
   */
  @Override
  public boolean isCacheable(HttpServletRequest req) {
    boolean doCache = !"false".equals(req.getParameter("berlioz-cache"));
    return doCache;
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
   */
  @Override
  public void writeResponse(HttpServletRequest req, HttpServletResponse res, CachedResource resource)
      throws IOException, ServletException {

    final List<HttpHeader<? extends Serializable>> headers = resource.getHeaders();
    for (final HttpHeader<? extends Serializable> header : headers) {
      if ("ETag".equals(header.name())) {
        String requestIfNoneMatch = req.getHeader("If-None-Match");
        if (header.value().equals(requestIfNoneMatch)) {
          setHeaderNotModified(req, res, resource);
          res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
          res.flushBuffer();
          return;
        }
        break;
      }
      if ("Last-Modified".equals(header.name())) {
        long requestIfModifiedSince = req.getDateHeader("If-Modified-Since");
        if (requestIfModifiedSince != -1) {
          Date requestDate = new Date(requestIfModifiedSince);
          Date pageInfoDate = new Date(resource.getLastModified());
          if (!requestDate.before(pageInfoDate)) {
            setHeaderNotModified(req, res, resource);
            res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            res.flushBuffer();
            return;
          }
        }
      }
    }

    // Normal process
    res.setStatus(resource.getStatusCode());
    String contentType = resource.getContentType();
    if (contentType != null && contentType.length() > 0) {
      res.setContentType(contentType);
    }
    resource.copyHeadersTo(res, HttpHeaderUtils.isCompressible(resource.getContentType()) && HttpHeaderUtils.acceptsGZipCompression(req));
    writeContent(req, res, resource);
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
   * Set the appropriate headers for the HTTP not modified response.
   *
   * @param req The HTTP servlet request.
   * @param res the HTTP servlet response.
   * @param resource The cached resource build previously.
   */
  private void setHeaderNotModified(HttpServletRequest req, HttpServletResponse res, CachedResource resource) {
    long lastModified = resource.getLastModified();
    long ttlMilliseconds = computeTimeToLiveMilliseconds(getCache());
    boolean isGzippable = HttpHeaderUtils.isCompressible(resource.getContentType());
    boolean isGzipped = isGzippable && HttpHeaderUtils.acceptsGZipCompression(req);
    String cacheControl = this.cacheControlPattern.replaceAll("%TTL", Long.toString(ttlMilliseconds / MILLISECONDS_PER_SECOND));

    // Set the headers of the HTTP response
    res.setHeader(HttpHeaders.CACHE_CONTROL, cacheControl);
    res.setHeader(HttpHeaders.ETAG, toEtag(lastModified / MILLISECONDS_PER_SECOND, isGzipped));
    res.setDateHeader(HttpHeaders.LAST_MODIFIED, (lastModified / MILLISECONDS_PER_SECOND) * MILLISECONDS_PER_SECOND);
    res.setDateHeader(HttpHeaders.EXPIRES, System.currentTimeMillis() + ttlMilliseconds);
    if (isGzippable)
      res.setHeader(HttpHeaders.VARY, "Accept-Encoding");
  }

  /**
   * Get the time to live for a page, in milliseconds
   *
   * @param cache The cache instance
   * @return time to live in milliseconds
   */
  private static long computeTimeToLiveMilliseconds(Ehcache cache) {
    if (cache.isDisabled()) {
      return -1;
    } else {
      CacheConfiguration config = cache.getCacheConfiguration();
      if (config.isEternal()) {
        return ONE_YEAR_IN_MILLISECONDS;
      } else {
        return config.getTimeToLiveSeconds() * MILLISECONDS_PER_SECOND;
      }
    }
  }

  /**
   * Returns the last modified date form the etag
   *
   * @param etag the etag used
   *
   * @return the last modified date (in seconds)
   */
  private static long toLastModified(String etag) {
    if (etag == null || etag.length() < 2) return -1;
    String raw = etag;
    // Remove quotes
    if (raw.charAt(0) == '"' && raw.charAt(etag.length()-1) == '"') {
      raw = raw.substring(1, raw.length()-1);
    }
    // Remove "-gzip" suffix if any
    if (raw.endsWith("-gzip")) raw = raw.substring(0, raw.length()-5);
    try {
      return Long.parseLong(raw, 16);
    } catch (NumberFormatException ex) {
      LOGGER.warn("Incorrect etag {}", etag);
      return -1;
    }
  }

  /**
   * Returns an etag for the specified resource based on the last modified date.
   *
   * @param modified  The last modified date (in seconds)
   * @param isGzipped <code>true</code> if the resource is sent gzipped;
   *                  <code>false</code> if sent raw
   *
   * @return The corresponding etag.
   */
  private static String toEtag(long modified, boolean isGzipped) {
    StringBuilder etag = new StringBuilder();
    etag.append('"').append(Long.toHexString(modified));
    if (isGzipped) {
      etag.append("-gzip");
    }
    etag.append('"');
    return etag.toString();
  }

}
