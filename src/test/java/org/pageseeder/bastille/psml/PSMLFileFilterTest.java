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
package org.pageseeder.bastille.psml;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class PSMLFileFilterTest {

  private final PSMLFileFilter filter = new PSMLFileFilter();

  @Test
  void accept_psmlExtension_returnsTrue() {
    assertTrue(filter.accept(new File("document.psml")));
  }

  @Test
  void accept_xmlExtension_returnsFalse() {
    assertFalse(filter.accept(new File("document.xml")));
  }

  @Test
  void accept_noExtension_returnsFalse() {
    assertFalse(filter.accept(new File("document")));
  }

  @Test
  void accept_dotOnly_returnsFalse() {
    assertFalse(filter.accept(new File(".")));
  }

  @Test
  void accept_htmlExtension_returnsFalse() {
    assertFalse(filter.accept(new File("index.html")));
  }

  @Test
  void accept_psmlUpperCase_returnsFalse() {
    assertFalse(filter.accept(new File("document.PSML")));
  }

  @Test
  void accept_psmlMixedCase_returnsFalse() {
    assertFalse(filter.accept(new File("document.Psml")));
  }

  @Test
  void accept_deepPathWithPsmlExtension_returnsTrue() {
    assertTrue(filter.accept(new File("/a/b/c/document.psml")));
  }

  @Test
  void accept_null_throwsNullPointerException() {
    assertThrows(NullPointerException.class, () -> filter.accept(null));
  }
}
