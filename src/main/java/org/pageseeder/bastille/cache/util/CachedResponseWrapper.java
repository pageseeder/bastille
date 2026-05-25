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

import org.jspecify.annotations.Nullable;
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

  private static final String ACCEPT_ENCODING = "Accept-Encoding";

  /** HTTP status code, OK (200) by default. */
  private int status = SC_OK;

  /** The length of the content in bytes */
  private int contentLength;

  /** The content type (MIME) */
  private @Nullable String contentType;

  /**
   * List of response headers.
   */
  private final Map<String, List<Serializable>> headers =
      new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

  /**
   * Cookies.
   */
  private final List<Cookie> cookies = new ArrayList<>();

  /**
   * A servlet output stream backed by a byte array.
   */
  private final FilterOutputStream out;

  /**
   * Only used if the writer is requested; not serialized since it can be reconstructed.
   */
  private transient @Nullable PrintWriter writer;

  /**
   * Creates a cached response wrapper.
   *
   * @param res The HTTP response
   */
  public CachedResponseWrapper(HttpServletResponse res) {
    super(res);
    this.out = new FilterOutputStream();
  }

  @Override
  public ServletOutputStream getOutputStream() {
    return this.out;
  }

  /**
   * @return The print writer wrapping the underlying stream.
   *
   * @throws IOException If thrown while creating a new <code>PrintWriter</code> instance.
   */
  @Override
  public PrintWriter getWriter() throws IOException {
    if (this.writer == null) {
      this.writer = new PrintWriter(new OutputStreamWriter(this.out, getCharacterEncoding()), true);
    }
    return this.writer;
  }

  @Override
  public void setStatus(int status) {
    this.status = status;
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
    this.status = code;
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
    this.status = code;
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
    this.status = HttpServletResponse.SC_MOVED_TEMPORARILY;
    super.sendRedirect(url);
  }

  @Override
  public void setStatus(int code, String msg) {
    this.status = code;
    super.setStatus(code);
  }

  @Override
  public void setContentLength(int length) {
    this.contentLength = length;
    super.setContentLength(length);
  }

  @Override
  public void setContentType(String type) {
    this.contentType = type;
    super.setContentType(type);
  }

  @Override
  public @Nullable String getContentType() {
    return this.contentType;
  }

  @Override
  public void addHeader(String name, String value) {
    this.headers.computeIfAbsent(name, k -> new LinkedList<>()).add(value);
    super.addHeader(name, value);
  }

  @Override
  public void setHeader(String name, String value) {
    List<Serializable> values = new LinkedList<>();
    values.add(value);
    this.headers.put(name, values);
    super.setHeader(name, value);
  }

  @Override
  public void addDateHeader(String name, long date) {
    this.headers.computeIfAbsent(name, k -> new LinkedList<>()).add(date);
    super.addDateHeader(name, date);
  }

  @Override
  public void setDateHeader(String name, long date) {
    List<Serializable> values = new LinkedList<>();
    values.add(date);
    this.headers.put(name, values);
    super.setDateHeader(name, date);
  }

  @Override
  public void addIntHeader(String name, int value) {
    this.headers.computeIfAbsent(name, k -> new LinkedList<>()).add(value);
    super.addIntHeader(name, value);
  }

  @Override
  public void setIntHeader(String name, int value) {
    List<Serializable> values = new LinkedList<>();
    values.add(value);
    this.headers.put(name, values);
    super.setIntHeader(name, value);
  }

  @Override
  public void addCookie(Cookie cookie) {
    this.cookies.add(cookie);
    super.addCookie(cookie);
  }

  /**
   * Flushes buffer and commits response to client.
   *
   * <p>This method does not flush the buffer of the underlying streams to avoid the
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
    this.cookies.clear();
    this.headers.clear();
    this.status = SC_OK;
    this.contentType = null;
    this.contentLength = 0;
  }

  // Class specific methods
  // ---------------------------------------------------------------------------------------------

  /**
   * @return the status code for this response.
   */
  @Override
  public int getStatus() {
    return this.status;
  }

  /**
   * @return the content length.
   */
  public int getContentLength() {
    return this.contentLength;
  }

  /**
   * @return all the cookies.
   */
  public Collection<Cookie> getCookies() {
    return this.cookies;
  }

  /**
   * @return All the headersMap set/added on the response
   */
  public List<HttpHeader<? extends Serializable>> getAllHeaders() {
    List<HttpHeader<? extends Serializable>> result = new LinkedList<>();
    for (Map.Entry<String, List<Serializable>> headerEntry : this.headers.entrySet()) {
      String name = headerEntry.getKey();
      for (Serializable value : headerEntry.getValue()) {
        final Type type = HttpHeader.Type.determineType(value.getClass());
        switch (type) {
          case STRING:
            result.add(new HttpHeader<>(name, (String) value));
            break;
          case DATE:
            result.add(new HttpHeader<>(name, (Long) value));
            break;
          case INT:
            result.add(new HttpHeader<>(name, (Integer) value));
            break;
          default:
            throw new IllegalArgumentException("No mapping for Header.Type: " + type);
        }
      }
    }
    return result;
  }

  /**
   * @param name the name of the header
   * @return All the headersMap set/added on the response
   */
  public @Nullable List<Serializable> getHeaderValues(String name) {
    return this.headers.get(name);
  }


  /**
   * @param name the name of the header
   * @return All the headersMap set/added on the response
   */
  @Override
  public @Nullable String getHeader(String name) {
    List<Serializable> values = this.headers.get(name);
    if (values != null && !values.isEmpty()) return values.get(0).toString();
    return null;
  }

  /**
   *
   * @param name the name of the header
   * @return the value of the header as a date.
   */
  public long getDateHeader(String name) {
    List<Serializable> values = this.headers.get(name);
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
    if (this.writer != null) {
      this.writer.flush();
    }
    this.out.flush();
  }

  /**
   * @return the content of the underlying stream as a byte array.
   */
  public byte[] toByteArray() {
    return this.out.toByteArray();
  }

  /**
   * Adds the "Vary: Accept-Encoding" header to this response.
   *
   * @param add <code>true</code> to add the "Accept-Encoding" to the Vary header;
   *            <code>false</code> to remove it.
   */
  public void adjustVaryAcceptEncoding(boolean add) {
    String value = findVaryHeaderValue();
    if (value == null) {
      if (add) {
        setHeader(HttpHeaders.VARY, ACCEPT_ENCODING);
      }
    } else if (!"*".equals(value)) {
      if (value.contains(ACCEPT_ENCODING)) {
        if (!add && ACCEPT_ENCODING.equals(value)) {
          this.headers.remove(HttpHeaders.VARY);
        }
      } else {
        if (add) {
          setHeader(HttpHeaders.VARY, value + "," + ACCEPT_ENCODING);
        }
      }
    }
  }

  private @Nullable String findVaryHeaderValue() {
    for (Entry<String, List<Serializable>> e : this.headers.entrySet()) {
      if (HttpHeaders.VARY.equalsIgnoreCase(e.getKey())) {
        List<Serializable> v = e.getValue();
        return v.isEmpty() ? null : v.get(0).toString();
      }
    }
    return null;
  }

  // inner classes
  // ---------------------------------------------------------------------------------------------

  /**
   * A custom {@link javax.servlet.ServletOutputStream} for this wrapper.
   */
  public static final class FilterOutputStream extends ServletOutputStream implements Serializable {

    private static final long serialVersionUID = 1L;

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
