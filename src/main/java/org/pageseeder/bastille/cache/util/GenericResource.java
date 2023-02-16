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

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import javax.servlet.http.HttpServletResponse;

import org.pageseeder.bastille.cache.util.HttpHeader.Type;
import org.pageseeder.berlioz.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A <code>Serializable</code> representation of a cached resource.
 *
 * @author Christophe Lauret
 *
 * @version Bastille 0.8.3 - 26 January 2013
 */
public final class GenericResource implements Serializable, CachedResource {

  /** As per requirement for <code>Serializable</code> */
  private static final long serialVersionUID = 2909918731971135622L;

  /** Where useful debug info goes. */
  private static final Logger LOGGER = LoggerFactory.getLogger(GenericResource.class);

  /**
   * List of response HTTP headers to include in response.
   */
  private final List<HttpHeader<? extends Serializable>> _headers = new ArrayList<>();

  /**
   * Indicates whether we store the compressed version of the content.
   */
  private final boolean _storeGzipped;

  /**
   * The content of the page.
   */
  private final byte[] _content;

  /**
   * The content type (MIME) of the content.
   */
  private final String _contentType;

  /**
   * The status code of the response.
   */
  private final int _status;

  /**
   * Creates a PageInfo object representing the "page".
   *
   * @param status       The HTTP status code of the response
   * @param contentType  The content type
   * @param body         The body in bytes to store
   * @param storeGzipped <code>true</code> to store the content as compressed (for text);
   *                     <code>false</code> otherwise (for image)
   * @param headers      The headers for this cached resource.
   *
   */
  public GenericResource(int status, String contentType, byte[] body,
      boolean storeGzipped, Collection<HttpHeader<? extends Serializable>> headers) {
    if (headers != null) {
      this._headers.addAll(headers);
    }
    this._contentType = contentType;
    this._storeGzipped = storeGzipped;
    this._status = status;
    this._content = toStorableContent(body, storeGzipped, headers);
  }

  /**
   * @return the content type of the response.
   */
  @Override
  public String getContentType() {
    return this._contentType;
  }

  /**
   * @return the HTTP status code of the response.
   */
  @Override
  public int getStatusCode() {
    return this._status;
  }

  @Override
  public List<HttpHeader<? extends Serializable>> getHeaders(boolean gzipped) {
    // TODO Adjust the etag?
    return this._headers;
  }

  @Override
  public boolean hasContent() {
    if (this._content == null) return false;
    return this._storeGzipped? GZIPUtils.shouldGzippedBodyBeZero(this._content) : this._content.length != 0;
  }

  /**
   * Returns the gzip content if stored as such.
   *
   * @return the gzipped version of the body if the content is stores gzipped or <code>null</code>
   */
  public byte[] getGzippedBody() {
    if (this._storeGzipped) return this._content;
    else return null;
  }

  /**
   * Returns the ungzipped content.
   *
   * <p>If the content is stored gzipped, this method will unzip the content on demand.
   *
   * @return the ungzipped version of the body.
   *
   * @throws IOException if thrown whil ungzippind the content.
   */
  public byte[] getUngzippedBody() throws IOException {
    if (this._storeGzipped) return GZIPUtils.ungzip(this._content);
    else return this._content;
  }

  /**
   * @return <code>true</code> if there is a non <code>null</code> gzipped body
   */
  @Override
  public boolean hasGzippedBody() {
    return this._storeGzipped && this._content != null;
  }

  /**
   * @return <code>true</code> if there is a non-null ungzipped body
   */
  public boolean hasUngzippedBody() {
    return !this._storeGzipped && this._content != null;
  }

  @Override
  public byte[] getBody(boolean gzipped) throws IOException {
    if (gzipped) return getGzippedBody();
    else return getUngzippedBody();
  }

  /**
   * Returns <code>true</code> if the response is OK (200).
   *
   * @return <code>true</code> if the status code is 200;
   *         <code>false</code> for any other code.
   */
  @Override
  public boolean isOK() {
    return this._status == HttpServletResponse.SC_OK;
  }

  /**
   * Returns the last modified date for this resource.
   *
   * @return the last modified date if there is a "Last-Modified" header defined; -1 otherwise.
   */
  @Override
  public long getLastModified() {
    for (HttpHeader<? extends Serializable> h : this._headers) {
      if (HttpHeaders.LAST_MODIFIED.equalsIgnoreCase(h.name())) {
        final Type type = h.type();
        switch (type) {
          case STRING:
            return new HttpDateFormat().parse((String) h.value()).getTime();
          case DATE:
            return (Long)h.value();
          default:
            throw new IllegalArgumentException("Header " + h + " is not supported as type: " + h.type());
        }
      }
    }
    return -1;
  }

  @Override
  public String getETag(boolean gzipped) {
    for (HttpHeader<? extends Serializable> h : this._headers) {
      if (HttpHeaders.ETAG.equals(h.name())) return adjustEtag(h.value().toString(), gzipped);
    }
    return null;
  }

  /**
   * Copy the headers to the HTTP servlet response.
   *
   * @param res     The HTTP servlet response where the headers should be copied.
   * @param gzipped Whether the content was sent gzipped
   */
  @Override
  public void copyHeadersTo(HttpServletResponse res, boolean gzipped) {
    // Track which headers have been set so all headers of the same name after the first are added
    Collection<String> setHeaders = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    for (HttpHeader<? extends Serializable> header : this._headers) {
      String name = header.name();
      switch (header.type()) {
        case STRING:
          if (setHeaders.contains(name)) {
            String value = (String) header.value();
            // Value adjust Etag for negociated content
            if ("etag".equalsIgnoreCase(name)) {
              if (!gzipped && value.endsWith("-gzip\"")) {
                value = value.replace("-gzip\"", "");
              } else if (gzipped && !value.endsWith("-gzip\"")) {
                value = value.substring(0, value.length()-1) + "-gzip\"";
              }
            }
            res.addHeader(name, value);
          } else {
            setHeaders.add(name);
            res.setHeader(name, (String) header.value());
          }
          break;
        case DATE:
          if (setHeaders.contains(name)) {
            res.addDateHeader(name, (Long) header.value());
          } else {
            setHeaders.add(name);
            res.setDateHeader(name, (Long) header.value());
          }
          break;
        case INT:
          if (setHeaders.contains(name)) {
            res.addIntHeader(name, (Integer) header.value());
          } else {
            setHeaders.add(name);
            res.setIntHeader(name, (Integer) header.value());
          }
          break;
        default:
          throw new IllegalArgumentException("No mapping for Header: " + header);
      }
    }
  }

  // private helpers
  // ---------------------------------------------------------------------------------------------

  /**
   * Returns the content to store in this class.
   *
   * @param body         the content content
   * @param storeGzipped whether to store the content as gzipped
   * @param headers      the HTTP header to check whether the content was gzipped.
   *
   * @return the corresponding content.
   */
  private static byte[] toStorableContent(byte[] body, boolean storeGzipped,
      Collection<HttpHeader<? extends Serializable>> headers) {
    byte[] content = null;
    try {
      if (storeGzipped) {
        // gunzip on demand
        if (isBodyParameterGzipped(headers)) {
          content = body;
        } else {
          content = GZIPUtils.gzip(body);
        }
      } else {
        if (isBodyParameterGzipped(headers)) {
          content = null;
          throw new IllegalArgumentException("Non gzip content has been gzipped.");
        } else {
          content = body;
        }
      }
    } catch (IOException ex) {
      LOGGER.error("Error ungzipping gzipped body", ex);
    }
    return content;
  }

  /**
   * The response body will be assumed to be gzipped if the GZIP header has been set.
   *
   * @param headers The HTTP headers.
   *
   * @return <code>true</code> if the body is gzipped
   */
  private static boolean isBodyParameterGzipped(Collection<HttpHeader<? extends Serializable>> headers) {
    for (HttpHeader<? extends Serializable> header : headers) {
      if ("gzip".equals(header.value())) return true;
    }
    return false;
  }

  /**
   * Adjust an existing etag to include the "-gzip" part so that it changes for negociated representations.
   *
   * <p>This method does nothing if the etag is already correct.
   *
   * @param etag    The current etag
   * @param gzipped <code>true</code> if the etag is needed for gzipped content;
   *                <code>false</code> otherwise.
   *
   * @return the adjusted etag
   */
  private static String adjustEtag(String etag, boolean gzipped) {
    String value = etag;
    if (!gzipped && value.endsWith("-gzip\"")) {
      value = value.replace("-gzip", "");
    } else if (gzipped && !value.endsWith("-gzip\"")) {
      value = value.substring(0, value.length()-1) + "-gzip\"";
    }
    return value;
  }

}

