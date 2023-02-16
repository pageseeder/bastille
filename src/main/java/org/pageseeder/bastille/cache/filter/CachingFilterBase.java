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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pageseeder.bastille.cache.util.CachedResource;
import org.pageseeder.bastille.cache.util.GZIPUtils;
import org.pageseeder.berlioz.http.HttpHeaderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.constructs.blocking.BlockingCache;

/**
 * A base class for caching filters.
 *
 * <p>This class should be sub-classed for each page to be cached.
 *
 * <p>The following initialization parameters are supported:
 * <ul>
 *   <li><code>cache-name</code> - the name of the cache in the EH cache configuration.
 *   <li>blockingTimeoutMillis - the time, in milliseconds, to wait for the filter chain to return
 * with a response on a cache miss. This is useful to fail fast in the event of an infrastructure
 * failure.
 * </ul>
 *
 * @author Christophe Lauret
 *
 * @version Bastille 0.8.3 - 27 January 2013
 */
public abstract class CachingFilterBase implements Filter, CachingFilter {

  /** Logger will report caching problems */
  private static final Logger LOGGER = LoggerFactory.getLogger(CachingFilterBase.class);

  /**
   * The cache name can be set through init parameters. If it is set it is stored here.
   */
  private String cacheName;

  /**
   * The actual cache holding the web pages.
   *
   * <p>We use a blocking cache as ensure that all threads use the same instance of when they
   * share the same cache name
   */
  private BlockingCache _cache;

  /**
   * A thread local flag to check whether the filter has been invoked multiple times by the same
   * thread.
   */
  private final VisitedFlag _visits = new VisitedFlag();

  /**
   * Initialises blockingCache to use.
   *
   * <p>The BlockingCache created by this method does not have a lock timeout set.
   *
   * <p>A timeout can be appled using <code>blockingCache.setTimeoutMillis(int timeout)</code> and
   * takes effect immediately for all new requests
   *
   * @throws CacheException
   *           The most likely cause is that a cache has not been configured in ehcache's
   *           configuration file ehcache.xml for the filter name
   * @param config this filter's configuration.
   */
  @Override
  public void init(FilterConfig config) throws CacheException {
    synchronized (this.getClass()) {
      if (this._cache == null) {
        this.cacheName = config.getInitParameter("cache-name");
        if (this.cacheName != null && this.cacheName.length() > 0) {
          LOGGER.debug("Using configured cacheName of {}.", this.cacheName);
        } else {
          LOGGER.debug("No cacheName configured - using default of {}.", getDefaultCacheName());
          this.cacheName = getDefaultCacheName();
        }

        String localCacheName = getCacheName();

        // Initialise the cache
        Ehcache cache = getCacheManager().getEhcache(localCacheName);
        if (cache == null) throw new CacheException("cache '" + localCacheName + "' not found in configuration");
        if (!(cache instanceof BlockingCache)) {
          // decorate and substitute
          BlockingCache newBlockingCache = new BlockingCache(cache);
          getCacheManager().replaceCacheWithDecoratedCache(cache, newBlockingCache);
        }
        this._cache = (BlockingCache) getCacheManager().getEhcache(localCacheName);
        this._cache.setTimeoutMillis(5000);
      }
    }
  }

  /**
   * Performs the filtering.
   *
   * @param req The HTTP servlet request
   * @param res The HTTP servlet response
   * @param chain THe servlet chain
   *
   * @throws IOException      for I/O errors only
   * @throws ServletException Any other error including unhandled caching exceptions
   */
  @Override
  public final void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
          throws ServletException, IOException {
    HttpServletRequest httpRequest = (HttpServletRequest) req;
    HttpServletResponse httpResponse = (HttpServletResponse) res;
    doFilter(httpRequest, httpResponse, chain);
  }

  /**
   * Performs the filtering for a request. This method caches based responses keyed by
   * {@link #calculateKey(javax.servlet.http.HttpServletRequest)}
   *
   * <p>By default this method will queue requests requesting the page response for a given key until
   * the first thread in the queue has completed. The request which occurs when the page expires
   * incurs the cost of waiting for the downstream processing to return the response.
   *
   * <p>The maximum time to wait can be configured by setting <code>setTimeoutMillis</code> on the
   * underlying <code>BlockingCache</code>.
   *
   * @param req The HTTP servlet request
   * @param res The HTTP servlet response
   * @param chain THe servlet chain
   *
   * @throws IOException      For I/O errors only
   * @throws ServletException For all other errors.
   */
  public final void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws ServletException, IOException {

    // Response already committed (maybe by another filter)
    if (res.isCommitted()) {
      LOGGER.warn("Response already committed before building cached resource.");

    // Request indicates underlying resource cannot be cached
    } else if (!isCacheable(req)) {
      LOGGER.info("Request not cacheable, invoking underlying servlet");
      chain.doFilter(req, res);

    // This filter already been used (probably a configuration error)
    } else if (this._visits.hasVisited()) {
      LOGGER.warn("This filter was invoked twice for {} and will be ignored - check your config",
          req.getRequestURI());
      chain.doFilter(req, res);

    // Let's go
    } else {
      try {
        this._visits.markAsVisited();
        CachedResource resource = getResource(req, res, chain);
        if (resource.isOK()) {
          if (res.isCommitted()) {
            LOGGER.warn("Response already committed after fetching resource but before writing response.");
          } else {
            writeResponse(req, res, resource);
          }
        } else {
          LOGGER.warn("Status cached resource was for {} was {}", req.getRequestURI(), resource.getStatusCode());
        }
      } catch (CacheException ex) {
        LOGGER.error("Unable to construct cache entry", ex);
      } finally {
        this._visits.remove();
      }
    }
  }

  // Useful methods for extending classes
  // ----------------------------------------------------------------------------------------------

  /**
   * @param key The key for the cached resource
   * @return The cached resource for the specified key.
   */
  protected final CachedResource getResourceFromCache(String key) {
    Element element = this._cache.get(key);
    if (element == null || element.getObjectValue() == null) return null;
    return (CachedResource)element.getObjectValue();
  }

  /**
   * Returns the cache used.
   *
   * @return the used by this filter.
   */
  protected final Ehcache getCache() {
    return this._cache;
  }

  /**
   * A meaningful name representative of the page being cached.
   *
   * <p>The <code>cacheName</code> field is be set by the <code>doInit</code> method.
   *
   * <p>Override to with key control the name used.
   *
   * <p>The significance is that the name is used to find a cache configuration in <code>ehcache.xml</code>
   *
   * @return the name of the cache to use for this filter.
   */
  protected final String getCacheName() {
    return this.cacheName;
  }

  // private helpers
  // ----------------------------------------------------------------------------------------------

  /**
   * Writes the response content.
   *
   * <p>This will be gzipped or non gzipped depending on whether the User Agent accepts GZIP encoding.
   *
   * <p>If the body is written gzipped a gzip header is added.
   *
   * @param req      The HTTP Servlet request
   * @param res      The HTTP Servlet response
   * @param resource The content of the cached resource.
   *
   * @throws IOException      For I/O errors only
   * @throws ServletException For all other errors.
   */
  public static void writeContent(HttpServletRequest req, HttpServletResponse res, CachedResource resource)
      throws IOException, ServletException {
    byte[] body;

    // Check whether the response should have any content
    boolean hasContent = hasContent(req, resource.getStatusCode());
    if (!hasContent || !resource.hasContent()) {
      // Discarding returned body and returning a 0-length body content
      body = new byte[0];

    } else if (resource.hasGzippedBody() && HttpHeaderUtils.acceptsGZipCompression(req)) {
      // Client accepts GZIP, let's send it compressed
      body = resource.getBody(true);
      GZIPUtils.addGzipHeader(res);

    } else {
      // No HTTP compression
      body = resource.getBody(false);
    }

    // Writing out content
    res.setContentLength(body.length);
    OutputStream out = new BufferedOutputStream(res.getOutputStream());
    out.write(body);
    out.flush();
  }

  /**
   * Check whether the response should have any content according to RFC 2616.
   *
   * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.2.5">HTTP/1.1 - 204 No Content</a>
   * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.3.5">HTTP/1.1 - 304 Not Modified</a>
   *
   * @param req    the client HTTP request
   * @param status the HTTP status code of the response.
   * @return <code>true</code> if the response should be 0, even if it is isn't.
   */
  private static boolean hasContent(HttpServletRequest req, int status) {
    if (status == HttpServletResponse.SC_NO_CONTENT
     || status == HttpServletResponse.SC_NOT_MODIFIED) {
      LOGGER.debug("Removing message body for {} (status code={})", req.getRequestURL(), status);
      return false;
    }
    return true;
  }

  // Inner classes
  // ----------------------------------------------------------------------------------------------

  /**
   * A <code>ThreadLocal</code> class to check if the filter was invoked twice for the same
   * request.
   */
  private static final  class VisitedFlag extends ThreadLocal<Boolean> {

    @Override
    protected Boolean initialValue() {
      return false;
    }

    /**
     * @return <code>true</code> if this thread has already set the flag to <code>true</code>;
     *         <code>false</code> the first time.
     */
    public boolean hasVisited() {
      return get();
    }

    /**
     * set the flag to <code>true</code> for this thread.
     */
    public void markAsVisited() {
      set(true);
    }

  }

}
