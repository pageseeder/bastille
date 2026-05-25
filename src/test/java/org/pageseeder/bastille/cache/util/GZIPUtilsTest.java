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

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class GZIPUtilsTest {

  // --- isGzipped ---

  @Test
  void isGzipped_null_returnsFalse() {
    assertFalse(GZIPUtils.isGzipped(null));
  }

  @Test
  void isGzipped_empty_returnsFalse() {
    assertFalse(GZIPUtils.isGzipped(new byte[0]));
  }

  @Test
  void isGzipped_oneByte_returnsFalse() {
    assertFalse(GZIPUtils.isGzipped(new byte[]{31}));
  }

  @Test
  void isGzipped_validMagicBytes_returnsTrue() throws IOException {
    byte[] compressed = GZIPUtils.gzip("hello".getBytes(StandardCharsets.UTF_8));
    assertTrue(GZIPUtils.isGzipped(compressed));
  }

  @Test
  void isGzipped_plainText_returnsFalse() {
    byte[] plain = "hello world".getBytes(StandardCharsets.UTF_8);
    assertFalse(GZIPUtils.isGzipped(plain));
  }

  // --- shouldGzippedBodyBeZero ---

  @Test
  void shouldGzippedBodyBeZero_exactlyEmptySize_returnsTrue() throws IOException {
    // An empty gzip stream is 20 bytes
    byte[] emptyGzip = GZIPUtils.gzip(new byte[0]);
    assertTrue(GZIPUtils.shouldGzippedBodyBeZero(emptyGzip));
  }

  @Test
  void shouldGzippedBodyBeZero_nonEmptyContent_returnsFalse() throws IOException {
    byte[] compressed = GZIPUtils.gzip("hello world".getBytes(StandardCharsets.UTF_8));
    assertFalse(GZIPUtils.shouldGzippedBodyBeZero(compressed));
  }

  // --- gzip ---

  @Test
  void gzip_producesGzippedOutput() throws IOException {
    byte[] result = GZIPUtils.gzip("hello".getBytes(StandardCharsets.UTF_8));
    assertTrue(GZIPUtils.isGzipped(result));
  }

  @Test
  void gzip_alreadyGzipped_throws() throws IOException {
    byte[] compressed = GZIPUtils.gzip("hello".getBytes(StandardCharsets.UTF_8));
    assertThrows(IOException.class, () -> GZIPUtils.gzip(compressed));
  }

  @Test
  void gzip_emptyArray_producesEmptyGzipFrame() throws IOException {
    byte[] result = GZIPUtils.gzip(new byte[0]);
    assertTrue(GZIPUtils.isGzipped(result));
    assertEquals(20, result.length);
  }

  // --- ungzip ---

  @Test
  void ungzip_roundTrip_ascii() throws IOException {
    byte[] original = "Hello, World!".getBytes(StandardCharsets.UTF_8);
    byte[] compressed = GZIPUtils.gzip(original);
    assertArrayEquals(original, GZIPUtils.ungzip(compressed));
  }

  @Test
  void ungzip_roundTrip_largerPayload() throws IOException {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 5000; i++) sb.append("abcdefghij");
    byte[] original = sb.toString().getBytes(StandardCharsets.UTF_8);
    assertArrayEquals(original, GZIPUtils.ungzip(GZIPUtils.gzip(original)));
  }

  @Test
  void ungzip_emptyFrame_returnsEmptyArray() throws IOException {
    byte[] emptyGzip = GZIPUtils.gzip(new byte[0]);
    assertArrayEquals(new byte[0], GZIPUtils.ungzip(emptyGzip));
  }

  @Test
  void gzip_compressionReducesSize() throws IOException {
    // Highly compressible content
    byte[] original = new byte[10_000];
    java.util.Arrays.fill(original, (byte) 'a');
    byte[] compressed = GZIPUtils.gzip(original);
    assertTrue(compressed.length < original.length,
        "Compressed size should be smaller than original for repetitive data");
  }
}
