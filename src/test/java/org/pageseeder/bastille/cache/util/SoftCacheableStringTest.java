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

class SoftCacheableStringTest {

  @Test
  void get_returnsOriginalString() {
    SoftCacheableString cached = new SoftCacheableString("hello");
    assertEquals("hello", cached.get());
  }

  @Test
  void get_empty() {
    SoftCacheableString cached = new SoftCacheableString("");
    assertEquals("", cached.get());
  }

  @Test
  void get_unicode() {
    String value = "Héllo Wörld — 日本語";
    SoftCacheableString cached = new SoftCacheableString(value);
    assertEquals(value, cached.get());
  }

  @Test
  void get_consistent() {
    SoftCacheableString cached = new SoftCacheableString("repeated");
    assertEquals(cached.get(), cached.get());
  }

  @Test
  void toString_equalsGet() {
    SoftCacheableString cached = new SoftCacheableString("test string");
    assertEquals(cached.get(), cached.toString());
  }

  @Test
  void get_largeString() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 100_000; i++) sb.append("abcdefghij");
    String large = sb.toString();
    SoftCacheableString cached = new SoftCacheableString(large);
    assertEquals(large, cached.get());
  }

  @Test
  void get_afterSoftRefCleared() throws Exception {
    SoftCacheableString cached = new SoftCacheableString("recover me");

    // Clear the soft reference via reflection to simulate GC pressure
    java.lang.reflect.Field f = SoftCacheableString.class.getDeclaredField("softRef");
    f.setAccessible(true);
    f.set(cached, null);

    // get() must reconstruct the string from the compressed bytes
    assertEquals("recover me", cached.get());
  }
}
