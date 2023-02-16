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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * A simple wrapper around a string optimised for caching.
 *
 * <p>Objects maintain a transient {@link SoftReference} to the string, ensuring that the string will only be removed
 * if there is a risk of an {@link OutOfMemoryError}.
 *
 * <p>The object keeps a strong reference to a gzipped byte array, ensuring that the memory footprint is minimal when
 * the soft reference is removed and the serialization process is optimal.
 *
 * <p>This class is useful for strings which are sufficiently large with a low entropy so that their gzipped
 * representation will be significantly smaller than their string value.
 *
 * @author Christophe Lauret
 * @version 21 March 2013
 */
public final class SoftCacheableString implements Serializable {

  /** Serializable */
  private static final long serialVersionUID = 3832762143004346490L;

  /** For use as default buffer size. */
  private static final int FOUR_KB = 4196;

  /**
   * Using utf8 as the default charset.
   */
  private static final Charset UTF8 = StandardCharsets.UTF_8;

  /**
   * Compressed version of the data.
   */
  private final byte[] _data;

  /**
   * A soft reference, which may be removed by the garbage collector and is transient, so it won't be serialized.
   */
  private transient SoftReference<String> _s;

  /**
   * Creates a new cached string.
   *
   * @param data the string to store
   *
   * @throws NullPointerException If the data is <code>null</code>
   */
  public SoftCacheableString(String data) {
    this._data = gzip(data);
    this._s = new SoftReference<>(data);
  }

  /**
   * @return the string value.
   */
  public String get() {
    String s = (this._s != null)? this._s.get() : null;
    if (s == null) {
      s = ungzip(this._data);
      this._s = new SoftReference<>(s);
    }
    return s;
  }

  @Override
  public String toString() {
    return get();
  }

  // Private helpers
  // ----------------------------------------------------------------------------------------------

  /**
   * Compress the String using gzip.
   *
   * @param s The string to compress
   * @return the compressed string using gzip.
   *
   * @throws NullPointerException If the string is <code>null</code>
   */
  private static byte[] gzip(String s) {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try {
      GZIPOutputStream zip = new GZIPOutputStream(bytes);
      byte[] raw = s.getBytes(UTF8);
      zip.write(raw, 0, raw.length);
      zip.close();
    } catch (IOException ex) {
      // Should never occur since we work directly with buffers in memory
      throw new IllegalStateException("Unable to gzip string", ex);
    }
    return bytes.toByteArray();
  }

  /**
   * Unzips the specified byte array as a string using UTF8.
   *
   * @param data the byte array to ungzip
   * @return the corresponding string
   *
   * @throws NullPointerException If the byte array is <code>null</code>
   */
  private static String ungzip(byte[] data) {
    ByteArrayOutputStream uncompressed = new ByteArrayOutputStream(data.length);
    byte[] ungzipped = null;
    try {
      GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(data));
      final byte[] buffer = new byte[FOUR_KB];
      int bytesRead = 0;
      while (bytesRead != -1) {
        bytesRead = gzip.read(buffer, 0, FOUR_KB);
        if (bytesRead != -1) {
          uncompressed.write(buffer, 0, bytesRead);
        }
      }
      ungzipped = uncompressed.toByteArray();
      gzip.close();
      uncompressed.close();
    } catch (IOException ex) {
      // Should never occur since we work directly with buffers in memory
      throw new IllegalStateException("Unable to ungzip bytes", ex);
    }
    return new String(ungzipped, UTF8);
  }

}
