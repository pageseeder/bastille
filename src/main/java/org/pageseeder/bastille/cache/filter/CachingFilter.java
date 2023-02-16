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

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pageseeder.bastille.cache.util.CachedResource;
import org.pageseeder.berlioz.Beta;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;

/**
 * Defines methods common to all caching HTTP filters.
 *
 * @author Christophe Lauret
 *
 * @version Bastille 0.8.3 - 27 January 2013
 */
@Beta
public interface CachingFilter {

  /**
   * Returns the default name for the cache managed by the filter.
   *
   * <p>This method must not return <code>null</code>.
   *
   * @return the default name for the cache manage by the filter.
   */
  String getDefaultCacheName();

  /**
   * Returns the CacheManager for this caching filter.
   *
   * @return the CacheManager to be used
   */
  CacheManager getCacheManager();

  /**
   * Indicates whether the resource from the specified request can return a cached copy.
   *
   * <p>This method may be used to determine whether of not this filter should be used for the
   * resource. Implementations may use this method to decide whether to bypass the filter and
   * directly invoke the rest of the filter chain.
   *
   * @param req The HTTP servlet request.
   *
   * @return <code>true</code> if the caching filter can use a cache;
   *         <code>false</code> to invoke the rest of the filter chain.
   */
  boolean isCacheable(HttpServletRequest req);

  /**
   * CachingFilter works off a key.
   * <p>
   * The key should be unique. Factors to consider in generating a key are:
   * <ul>
   *   <li>The various hostnames that a request could come through
   *   <li>Whether additional parameters used for referral tracking e.g. google should be excluded to
   *   maximise cache hits
   *   <li>Additional parameters can be added to any page. The page will still work but will miss the
   *   cache. Consider coding defensively around this issue.
   * </ul>
   *
   * <p>Implementers should differentiate between GET and HEAD requests otherwise blank pages can result.
   *
   * @param req The HTTP servlet request
   * @return the key, generally the URL plus request parameters
   */
  String calculateKey(HttpServletRequest req);

  /**
   * Returns a cached resource from the specified request and response.
   *
   * <p>This method may invoke the rest of the filter chain.
   *
   * @param req The HTTP servlet request.
   * @param res The HTTP servlet response.
   * @param chain The filter chain.
   *
   * @return the corresponding cached resource
   *
   * @throws ServletException For general errors or errors while invoking the filter chain.
   * @throws CacheException   For caching-specific error.
   */
  CachedResource getResource(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
    throws ServletException, CacheException;

  /**
   * Writes the response from a PageInfo object.
   * <p>
   * Headers are set last so that there is an opportunity to override
   *
   * @param req The HTTP servlet request.
   * @param res the HTTP servlet response.
   * @param resource The cached resource build previously.
   *
   * @throws IOException      Should an IO error occur
   * @throws ServletException For general errors or errors while invoking the filter chain.
   * @throws CacheException   For caching-specific error.
   */
  void writeResponse(HttpServletRequest req, HttpServletResponse res, CachedResource resource)
      throws IOException, ServletException, CacheException;

}
