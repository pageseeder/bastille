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
import javax.servlet.WriteListener;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.pageseeder.bastille.cache.util.HttpHeader.Type;
import org.pageseeder.berlioz.http.HttpHeaders;

/**
 * Provides a wrapper for use with the caching filters.
 *
 * <p>This is the response that is supplied to the filters and servlets in the filter chain.
 *
 * @author Christophe Lauret
 *
 * @version Bastille 11.0
 */
public final class CachedResponseWrapper extends HttpServletResponseWrapper implements Serializable {

  /** As per requirement by <code>Serializable</code> */
  private static final long serialVersionUID = -5976708169031065497L;

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
      new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

  /**
   * Cookies.
   */
  private final List<Cookie> _cookies = new ArrayList<>();

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
   * <p>
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
   * <p>
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
   * <p>
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
      values = new LinkedList<>();
      this._headers.put(name, values);
    }
    values.add(value);
    super.addHeader(name, value);
  }

  @Override
  public void setHeader(String name, String value) {
    List<Serializable> values = new LinkedList<>();
    values.add(value);
    this._headers.put(name, values);
    super.setHeader(name, value);
  }

  @Override
  public void addDateHeader(String name, long date) {
    List<Serializable> values = this._headers.get(name);
    if (values == null) {
      values = new LinkedList<>();
      this._headers.put(name, values);
    }
    values.add(date);
    super.addDateHeader(name, date);
  }

  @Override
  public void setDateHeader(String name, long date) {
    List<Serializable> values = new LinkedList<>();
    values.add(date);
    this._headers.put(name, values);
    super.setDateHeader(name, date);
  }

  @Override
  public void addIntHeader(String name, int value) {
    List<Serializable> values = this._headers.get(name);
    if (values == null) {
      values = new LinkedList<>();
      this._headers.put(name, values);
    }
    values.add(value);
    super.addIntHeader(name, value);
  }

  @Override
  public void setIntHeader(String name, int value) {
    List<Serializable> values = new LinkedList<>();
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

  // Class specific methods
  // ---------------------------------------------------------------------------------------------

  /**
   * @return the status code for this response.
   */
  @Override
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
    List<HttpHeader<? extends Serializable>> headers = new LinkedList<>();
    for (Map.Entry<String, List<Serializable>> headerEntry : this._headers.entrySet()) {
      String name = headerEntry.getKey();
      for (Serializable value : headerEntry.getValue()) {
        final Type type = HttpHeader.Type.determineType(value.getClass());
        switch (type) {
          case STRING:
            headers.add(new HttpHeader<>(name, (String) value));
            break;
          case DATE:
            headers.add(new HttpHeader<>(name, (Long) value));
            break;
          case INT:
            headers.add(new HttpHeader<>(name, (Integer) value));
            break;
          default:
            throw new IllegalArgumentException("No mapping for Header.Type: " + type);
        }
      }
    }
    return headers;
  }

  /**
   * @param name the name of the header
   * @return All of the headersMap set/added on the response
   */
  public List<Serializable> getHeaderValues(String name) {
    return this._headers.get(name);
  }


  /**
   * @param name the name of the header
   * @return All of the headersMap set/added on the response
   */
  @Override
  public String getHeader(String name) {
    List<Serializable> values = this._headers.get(name);
    if (values != null && values.size() > 0) return values.get(0).toString();
    return null;
  }

  /**
   *
   * @param name the name of the header
   * @return the value of the header as a date.
   */
  public long getDateHeader(String name) {
    List<Serializable> values = this._headers.get(name);
    if (values != null && values.size() > 0) {
      Serializable value = values.get(0);
      if (value instanceof Long) return (Long)value;
      else if (value instanceof String) {
        // XXX: Not great, we should look into making it
        new HttpDateFormat().parse((String)value);
      }
    }
    return -1;
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
      if (add) {
        setHeader(HttpHeaders.VARY, "Accept-Encoding");
      }
    } else if (!"*".equals(value)) {
      if (value.contains("Accept-Encoding")) {
        if (!add && "Accept-Encoding".equals(value)) {
          this._headers.remove(HttpHeaders.VARY);
        }
      } else {
        if (add) {
          setHeader(HttpHeaders.VARY, value + ",Accept-Encoding");
        }
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
    public boolean isReady() {
      return true;
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {
      // Ignore
    }

    @Override
    public void write(int b) {
      this.stream.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
      this.stream.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) {
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
