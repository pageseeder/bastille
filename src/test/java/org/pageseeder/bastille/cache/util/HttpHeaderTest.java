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

import static org.junit.jupiter.api.Assertions.*;

class HttpHeaderTest {

  // --- Construction and accessors ---

  @Test
  void constructor_stringHeader() {
    HttpHeader<String> h = new HttpHeader<>("Content-Type", "text/html");
    assertEquals("Content-Type", h.name());
    assertEquals("text/html", h.value());
    assertEquals(HttpHeader.Type.STRING, h.type());
  }

  @Test
  void constructor_intHeader() {
    HttpHeader<Integer> h = new HttpHeader<>("Content-Length", 1024);
    assertEquals("Content-Length", h.name());
    assertEquals(Integer.valueOf(1024), h.value());
    assertEquals(HttpHeader.Type.INT, h.type());
  }

  @Test
  void constructor_longHeader_mapsToDate() {
    HttpHeader<Long> h = new HttpHeader<>("Last-Modified", 1_000_000L);
    assertEquals("Last-Modified", h.name());
    assertEquals(Long.valueOf(1_000_000L), h.value());
    assertEquals(HttpHeader.Type.DATE, h.type());
  }

  @Test
  void constructor_nullName_throws() {
    assertThrows(NullPointerException.class, () -> new HttpHeader<>(null, "value"));
  }

  @Test
  void constructor_nullValue_throws() {
    assertThrows(NullPointerException.class, () -> new HttpHeader<String>("name", null));
  }

  // --- equals / hashCode ---

  @Test
  void equals_sameInstance() {
    HttpHeader<String> h = new HttpHeader<>("X-Header", "v");
    assertEquals(h, h);
  }

  @Test
  void equals_equalHeaders() {
    HttpHeader<String> h1 = new HttpHeader<>("X-Header", "v");
    HttpHeader<String> h2 = new HttpHeader<>("X-Header", "v");
    assertEquals(h1, h2);
    assertEquals(h2, h1);
  }

  @Test
  void equals_differentName() {
    HttpHeader<String> h1 = new HttpHeader<>("X-A", "v");
    HttpHeader<String> h2 = new HttpHeader<>("X-B", "v");
    assertNotEquals(h1, h2);
  }

  @Test
  void equals_differentValue() {
    HttpHeader<String> h1 = new HttpHeader<>("X-Header", "v1");
    HttpHeader<String> h2 = new HttpHeader<>("X-Header", "v2");
    assertNotEquals(h1, h2);
  }

  @Test
  void equals_differentType() {
    HttpHeader<String>  hs = new HttpHeader<>("X", "42");
    HttpHeader<Integer> hi = new HttpHeader<>("X", 42);
    assertNotEquals(hs, hi);
  }

  @Test
  void equals_null() {
    assertNotEquals(new HttpHeader<>("X-Header", "v"), null);
  }

  @Test
  void equals_differentClass() {
    assertNotEquals(new HttpHeader<>("X-Header", "v"), "X-Header");
  }

  @Test
  void hashCode_equalHeaders() {
    HttpHeader<String> h1 = new HttpHeader<>("X-Header", "v");
    HttpHeader<String> h2 = new HttpHeader<>("X-Header", "v");
    assertEquals(h1.hashCode(), h2.hashCode());
  }

  // --- toString ---

  @Test
  void toString_containsName() {
    assertTrue(new HttpHeader<>("Content-Type", "text/html").toString().contains("Content-Type"));
  }

  @Test
  void toString_containsValue() {
    assertTrue(new HttpHeader<>("Content-Type", "text/html").toString().contains("text/html"));
  }

  // --- Type.determineType ---

  @Test
  void determineType_string() {
    assertEquals(HttpHeader.Type.STRING, HttpHeader.Type.determineType(String.class));
  }

  @Test
  void determineType_integer() {
    assertEquals(HttpHeader.Type.INT, HttpHeader.Type.determineType(Integer.class));
  }

  @Test
  void determineType_long() {
    assertEquals(HttpHeader.Type.DATE, HttpHeader.Type.determineType(Long.class));
  }

  @Test
  void determineType_cachedSecondCall() {
    // Second call uses the lookup cache — should return the same result
    assertEquals(HttpHeader.Type.STRING, HttpHeader.Type.determineType(String.class));
  }

  @Test
  void determineType_unknownClass_throws() {
    assertThrows(IllegalArgumentException.class, () -> HttpHeader.Type.determineType(Double.class));
  }

  // --- Type.getTypeClass ---

  @Test
  void typeClass_string() {
    assertEquals(String.class, HttpHeader.Type.STRING.getTypeClass());
  }

  @Test
  void typeClass_integer() {
    assertEquals(Integer.class, HttpHeader.Type.INT.getTypeClass());
  }

  @Test
  void typeClass_long() {
    assertEquals(Long.class, HttpHeader.Type.DATE.getTypeClass());
  }
}
