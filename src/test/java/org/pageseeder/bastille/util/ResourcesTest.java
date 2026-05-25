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
package org.pageseeder.bastille.util;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class ResourcesTest {

  @Test
  void getResource_existingResource_returnsBytes() {
    byte[] data = Resources.getResource("test.txt");
    assertNotNull(data);
    assertEquals("Hello, Bastille!", new String(data, StandardCharsets.UTF_8));
  }

  @Test
  void getResource_missingResource_returnsNull() {
    assertNull(Resources.getResource("does-not-exist.txt"));
  }

  @Test
  void getResource_existingResource_nonEmpty() {
    byte[] data = Resources.getResource("test.txt");
    assertNotNull(data);
    assertTrue(data.length > 0);
  }
}
