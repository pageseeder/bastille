/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.weborganic.bastille.cache.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weborganic.bastille.cache.util.HttpHeader.Type;
import org.weborganic.berlioz.http.HttpHeaders;

/**
 * Provides a wrapper for use with the caching filters.
 *
 * <p>This is the response that is supplied to the filters and servlets in the filter chain.
 *
 * @author Christophe Lauret
 *
 * @version Bastille 0.8.3 - 27 January 2013
 */
public final class CachedResponseWrapper extends HttpServletResponseWrapper implements Serializable {

  /** As per requirement by <code>Serializable</code> */
  private static final long serialVersionUID = -5976708169031065497L;

  /** For our logging needs */
  private static final Logger LOGGER = LoggerFactory.getLogger(CachedResponseWrapper.class);

  /** HTTP status code, OK (200) by default. */
  private int _status = SC_OK;

  /** The length of the content in bytes */
  private int _contentLength;

  /** The content type (MIME) */
  private String _contentType;

  /**
   * List of response headers.
   */
  private final Map<String, List<Serializable>> _headers =
      new TreeMap<String, List<Serializable>>(String.CASE_INSENSITIVE_ORDER);

  /**
   * Cookies.
   */
  private final List<Cookie> _cookies = new ArrayList<Cookie>();

  /**
   * A servlet output stream backed by a byte array.
   */
  private final FilterOutputStream _out;

  /**
   * Only used if the writer is requested.
   */
  private PrintWriter _writer;

  /**
   * Creates a cached response wrapper.
   *
   * @param res The HTTP response
   */
  public CachedResponseWrapper(HttpServletResponse res) {
    super(res);
    this._out = new FilterOutputStream();
  }

  @Override
  public ServletOutputStream getOutputStream() {
    return this._out;
  }

  /**
   * @return The print writer wrapping the underlying stream.
   *
   * @throws IOException If thrown while creating a new <code>PrintWriter</code> instance.
   */
  @Override
  public PrintWriter getWriter() throws IOException {
    if (this._writer == null) {
      this._writer = new PrintWriter(new OutputStreamWriter(this._out, getCharacterEncoding()), true);
    }
    return this._writer;
  }

  @Override
  public void setStatus(int status) {
    this._status = status;
    super.setStatus(status);
  }

  /**
   * Send the error.
   *
   * <p>If the response is not OK, most of the logic is bypassed and the error is sent raw and
   * the content is not cached.
   *
   * {@inheritDoc}
   */
  @Override
  public void sendError(int code, String string) throws IOException {
    this._status = code;
    super.sendError(code, string);
  }

  /**
   * Send the error.
   *
   * <p>If the response is not OK, most of the logic is bypassed and the error is sent raw and
   * the content is not cached.
   *
   * {@inheritDoc}
   */
  @Override
  public void sendError(int code) throws IOException {
    this._status = code;
    super.sendError(code);
  }

  /**
   * Send the redirect.
   *
   * <p>If the response is not OK, most of the logic is bypassed and the error is sent raw and
   * the content is not cached.
   *
   * {@inheritDoc}
   */
  @Override
  public void sendRedirect(String url) throws IOException {
    this._status = HttpServletResponse.SC_MOVED_TEMPORARILY;
    super.sendRedirect(url);
  }

  @Override
  public void setStatus(int code, String msg) {
    this._status = code;
    LOGGER.warn("Discarding message because this method is deprecated.");
    super.setStatus(code);
  }

  @Override
  public void setContentLength(int length) {
    this._contentLength = length;
    super.setContentLength(length);
  }

  @Override
  public void setContentType(String type) {
    this._contentType = type;
    super.setContentType(type);
  }

  @Override
  public String getContentType() {
    return this._contentType;
  }

  @Override
  public void addHeader(String name, String value) {
    List<Serializable> values = this._headers.get(name);
    if (values == null) {
      values = new LinkedList<Serializable>();
      this._headers.put(name, values);
    }
    values.add(value);
    super.addHeader(name, value);
  }

  @Override
  public void setHeader(String name, String value) {
    List<Serializable> values = new LinkedList<Serializable>();
    values.add(value);
    this._headers.put(name, values);
    super.setHeader(name, value);
  }

  @Override
  public void addDateHeader(String name, long date) {
    List<Serializable> values = this._headers.get(name);
    if (values == null) {
      values = new LinkedList<Serializable>();
      this._headers.put(name, values);
    }
    values.add(date);
    super.addDateHeader(name, date);
  }

  @Override
  public void setDateHeader(String name, long date) {
    List<Serializable> values = new LinkedList<Serializable>();
    values.add(date);
    this._headers.put(name, values);
    super.setDateHeader(name, date);
  }

  @Override
  public void addIntHeader(String name, int value) {
    List<Serializable> values = this._headers.get(name);
    if (values == null) {
      values = new LinkedList<Serializable>();
      this._headers.put(name, values);
    }
    values.add(value);
    super.addIntHeader(name, value);
  }

  @Override
  public void setIntHeader(String name, int value) {
    List<Serializable> values = new LinkedList<Serializable>();
    values.add(value);
    this._headers.put(name, values);
    super.setIntHeader(name, value);
  }

  @Override
  public void addCookie(Cookie cookie) {
    this._cookies.add(cookie);
    super.addCookie(cookie);
  }

  /**
   * Flushes buffer and commits response to client.
   *
   * <p>This method does not flush the buffer of the underlying stream so as to avoid the
   * response to commit the response prematurely.
   *
   * @throws IOException if thrown while flushing the underlying buffer.
   */
  @Override
  public void flushBuffer() throws IOException {
    flush();
  }

  /**
   * Resets the response.
   */
  @Override
  public void reset() {
    super.reset();
    this._cookies.clear();
    this._headers.clear();
    this._status = SC_OK;
    this._contentType = null;
    this._contentLength = 0;
  }

  @Override
  public void resetBuffer() {
    super.resetBuffer();
  }

  @Override
  public void setBufferSize(int size) {
    super.setBufferSize(size);
  }

  @Override
  public void setResponse(ServletResponse response) {
    super.setResponse(response);
  }

  @Override
  public boolean isCommitted() {
    return super.isCommitted();
  }

  @Override
  public ServletResponse getResponse() {
    // TODO potentially signal bypass
    LOGGER.debug("R.getResponse()");
    return super.getResponse();
  }

  // Class specific methods
  // ---------------------------------------------------------------------------------------------

  /**
   * @return the status code for this response.
   */
  public int getStatus() {
    return this._status;
  }

  /**
   * @return the content length.
   */
  public int getContentLength() {
    return this._contentLength;
  }

  /**
   * @return all the cookies.
   */
  public Collection<Cookie> getCookies() {
    return this._cookies;
  }

  /**
   * @return All of the headersMap set/added on the response
   */
  public List<HttpHeader<? extends Serializable>> getAllHeaders() {
    List<HttpHeader<? extends Serializable>> headers = new LinkedList<HttpHeader<? extends Serializable>>();
    for (Map.Entry<String, List<Serializable>> headerEntry : this._headers.entrySet()) {
      String name = headerEntry.getKey();
      for (Serializable value : headerEntry.getValue()) {
        final Type type = HttpHeader.Type.determineType(value.getClass());
        switch (type) {
          case STRING:
            headers.add(new HttpHeader<String>(name, (String) value));
            break;
          case DATE:
            headers.add(new HttpHeader<Long>(name, (Long) value));
            break;
          case INT:
            headers.add(new HttpHeader<Integer>(name, (Integer) value));
            break;
          default:
            throw new IllegalArgumentException("No mapping for Header.Type: " + type);
        }
      }
    }
    return headers;
  }

  /**
   * Flushes all the streams for this response.
   *
   * @throws IOException if thrown by the underlying output stream or writer
   */
  public void flush() throws IOException {
    if (this._writer != null) {
      this._writer.flush();
    }
    this._out.flush();
  }

  /**
   * @return the content of the underlying stream as a byte array.
   */
  public byte[] toByteArray() {
    return this._out.toByteArray();
  }

  /**
   * Adds the "Vary: Accept-Encoding" header to this response.
   *
   * @param add <code>true</code> to add the "Accept-Encoding" to the Vary header;
   *            <code>false</code> to remove it.
   */
  public void adjustVaryAcceptEncoding(boolean add) {
    String value = null;
    // Find the value of the existing vary header if any
    for (Entry<String, List<Serializable>> e : this._headers.entrySet()) {
      if (HttpHeaders.VARY.equalsIgnoreCase(e.getKey())) {
      List<Serializable> v = e.getValue();
      value = v.isEmpty()? null : e.getValue().get(0).toString();
        break;
      }
    }
    if (value == null) {
      // Set if not found
      if (add) setHeader(HttpHeaders.VARY, "Accept-Encoding");
    } else if (!"*".equals(value)) {
      if (value.contains("Accept-Encoding")) {
        if (!add && "Accept-Encoding".equals(value)) {
          this._headers.remove(HttpHeaders.VARY);
        }
      } else {
        if (add) setHeader(HttpHeaders.VARY, value + ",Accept-Encoding");
      }
    }
  }

  // inner classes
  // ---------------------------------------------------------------------------------------------

  /**
   * A custom {@link javax.servlet.ServletOutputStream} for this wrapper.
   */
  public static final class FilterOutputStream extends ServletOutputStream {

    /**
     * Underlying output stream
     */
    private final ByteArrayOutputStream stream;

    /**
     * Creates a FilterServletOutputStream backed by a byte array output stream.
     */
    public FilterOutputStream() {
      this.stream = new ByteArrayOutputStream();
    }

    @Override
    public void write(int b) throws IOException {
      this.stream.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
      this.stream.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
      this.stream.write(b, off, len);
    }

    /**
     * @return the content of this stream as a byte array.
     */
    public byte[] toByteArray() {
      return this.stream.toByteArray();
    }
  }

}
