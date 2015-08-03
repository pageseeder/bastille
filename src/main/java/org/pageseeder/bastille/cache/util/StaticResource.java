/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.pageseeder.bastille.cache.util;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.pageseeder.berlioz.http.HttpHeaderUtils;
import org.pageseeder.berlioz.http.HttpHeaders;

/**
 * A <code>Serializable</code> representation of a cached resource designed for
 * static file based content.
 *
 * <p>The etag is based on the last modified date.
 *
 * <p>If the resource is considered to be gzippable, the body content is stored compressed,
 * otherwise it is sent raw.
 *
 * @author Christophe Lauret
 *
 * @version Bastille 0.8.3 - 31 January 2013
 */
public final class StaticResource implements Serializable, CachedResource {

  /** As per requirement for <code>Serializable</code> */
  private static final long serialVersionUID = -7228525252854825521L;

  /** Where useful debug info goes. */
  private static final Logger LOGGER = LoggerFactory.getLogger(StaticResource.class);

  /** Useful constant */
  private static final int MILLISECONDS_PER_SECOND = 1000;

  /**
   * Indicates whether we store the compressed version of the content.
   */
  private final boolean _gzippable;

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
   * The last modified date.
   */
  private final long _lastModified;

  /**
   * The Cache control header.
   */
  private final String _cacheControl;

  /**
   * When the resource expires
   */
  private final long _expires;

  /**
   * Creates a PageInfo object representing the "page".
   *
   * @param status       The HTTP status code of the response
   * @param contentType  The content type
   * @param body         The body in bytes to store
   * @param modified     The last modified date of the resource.
   * @param cacheControl The cache control header for this static resource
   * @param expires      When this resource expires
   *
   * @throws IOException If the content was already gzipped
   */
  public StaticResource(int status, String contentType, byte[] body, long modified, String cacheControl, long expires)
      throws IOException {
    this._contentType = contentType;
    this._gzippable = HttpHeaderUtils.isCompressible(contentType);
    this._status = status;
    this._lastModified = (modified / MILLISECONDS_PER_SECOND) * MILLISECONDS_PER_SECOND;
    this._content = toStorableContent(body, this._gzippable);
    this._cacheControl = cacheControl;
    this._expires = expires;
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

  /**
   * Returns the gzip content if stored as such.
   *
   * @return the gzipped version of the body if the content is stores gzipped or <code>null</code>
   */
  public byte[] getGzippedBody() {
    if (this._gzippable) {
      return this._content;
    } else {
      return null;
    }
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
    if (this._gzippable) {
      return GZIPUtils.ungzip(this._content);
    } else {
      return this._content;
    }
  }

  @Override
  public byte[] getBody(boolean gzipped) throws IOException {
    if (gzipped) {
      return getGzippedBody();
    } else {
      return getUngzippedBody();
    }
  }

  @Override
  public boolean hasContent() {
    if (this._content == null) return false;
    return this._gzippable? !GZIPUtils.shouldGzippedBodyBeZero(this._content) : this._content.length != 0;
  }

  @Override
  public boolean hasGzippedBody() {
    return this._gzippable && this._content != null;
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
   * @return the last modified date if there is an "Last-Modified" header defined; -1 otherwise.
   */
  @Override
  public long getLastModified() {
    return this._lastModified;
  }

  @Override
  public String getETag(boolean gzipped) {
    return toEtag(this._lastModified / MILLISECONDS_PER_SECOND, gzipped);
  }

  @Override
  public List<HttpHeader<? extends Serializable>> getHeaders(boolean gzipped) {
    List<HttpHeader<? extends Serializable>> headers = new ArrayList<HttpHeader<? extends Serializable>>();
    // Set the headers of the HTTP response
    headers.add(new HttpHeader<Serializable>(HttpHeaders.CACHE_CONTROL, this._cacheControl));
    headers.add(new HttpHeader<Serializable>(HttpHeaders.ETAG, getETag(gzipped)));
    headers.add(new HttpHeader<Serializable>(HttpHeaders.LAST_MODIFIED, this._lastModified));
    headers.add(new HttpHeader<Serializable>(HttpHeaders.EXPIRES, this._expires));
    if (this._gzippable)
      headers.add(new HttpHeader<Serializable>(HttpHeaders.VARY, "Accept-Encoding"));
    return headers;
  }

  @Override
  public void copyHeadersTo(HttpServletResponse res, boolean gzipped) {
    res.setHeader(HttpHeaders.CACHE_CONTROL, this._cacheControl);
    res.setHeader(HttpHeaders.ETAG, getETag(gzipped));
    res.setDateHeader(HttpHeaders.LAST_MODIFIED, this._lastModified);
    res.setDateHeader(HttpHeaders.EXPIRES, this._expires);
    if (this._gzippable)
      res.setHeader(HttpHeaders.VARY, "Accept-Encoding");
  }

  // public utility class
  // ---------------------------------------------------------------------------------------------

  /**
   * Returns the last modified date form the etag.
   *
   * @param etag the etag used
   *
   * @return the last modified date (in seconds)
   */
  public static long toLastModified(String etag) {
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

  // private helpers
  // ---------------------------------------------------------------------------------------------

  /**
   * Returns the content to store in this class.
   *
   * @param body      the content content
   * @param gzippable whether to store the content as gzipped
   *
   * @return the corresponding content.
   */
  private static byte[] toStorableContent(byte[] body, boolean gzippable) {
    byte[] content = null;
    try {
      if (gzippable) {
        if (GZIPUtils.isGzipped(body)) {
          content = body;
        } else {
          content = GZIPUtils.gzip(body);
        }
      } else {
        if (GZIPUtils.isGzipped(body)) {
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
