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
package org.pageseeder.bastille.cache.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * A Servlet request wrapping the original request for use by the static caching filters.
 *
 * <p>The purpose of this request wrapper is to prune out the HTTP headers in order to
 * force the underlying servlet to fetch the raw data:
 * <ul>
 *   <li><code>If-None-Match</code></li>
 *   <li><code>If-Modified-Since</code></li>
 *   <li><code>If-Match</code></li>
 *   <li><code>If-Unmodified-Since</code></li>
 * </ul>
 *
 * @author Christophe Lauret
 *
 * @version Bastille 0.8.3 - 27 January 2013
 */
public final class StaticRequestWrapper extends HttpServletRequestWrapper {

  /**
   * The HTTP headers to exclude stored as lower case since headers are case insensitive.
   */
  private static final List<String> EXCLUDE = Arrays.asList(new String[]{
    "if-mone-match",
    "if-modified-since",
    "if-match",
    "if-unmodified-since"
  });

  /**
   * Wraps the HTTP servlet request.
   *
   * @param req the request to wrap
   */
  public StaticRequestWrapper(HttpServletRequest req) {
    super(req);
  }

  // HttpServletRequest methods
  // ----------------------------------------------------------------------------------------------

  @Override
  @SuppressWarnings("unchecked")
  public Enumeration<String> getHeaderNames() {
    List<String> headers = Collections.list(super.getHeaderNames());
    for (Iterator<String> i = headers.iterator(); i.hasNext();) {
      if (EXCLUDE.contains(i.next().toLowerCase())) {
        i.remove();
      }
    }
    return Collections.enumeration(headers);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Enumeration<String> getHeaders(String name) {
    if (EXCLUDE.contains(name.toLowerCase())) return new EmptyEnumeration<String>();
    else return super.getHeaders(name);
  }

  @Override
  public long getDateHeader(String name) {
    if (EXCLUDE.contains(name.toLowerCase())) return -1;
    else return super.getDateHeader(name);
  }

  @Override
  public String getHeader(String name) {
    if (EXCLUDE.contains(name.toLowerCase())) return null;
    else return super.getHeader(name);
  }

  // An Empty enumeration
  // ----------------------------------------------------------------------------------------------

  /**
   * A enumeration over an empty collection.
   *
   * <p>The {@link #hasMoreElements()} method always returns <code>false</code> and the
   * {@link #nextElement()} method always throws a {@link NoSuchElementException}.
   *
   * @author Christophe Lauret
   *
   * @param <T> Type of object to enumerate.
   */
  private static class EmptyEnumeration<T> implements Enumeration<T> {

    @Override
    public boolean hasMoreElements() {
      return false;
    }

    @Override
    public T nextElement() {
      throw new NoSuchElementException();
    }
  }
}
