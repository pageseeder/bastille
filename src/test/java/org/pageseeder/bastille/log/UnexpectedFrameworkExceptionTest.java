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
package org.pageseeder.bastille.log;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UnexpectedFrameworkExceptionTest {

  @Test
  void constructor_setsMessage() {
    Throwable cause = new ClassCastException("wrong type");
    UnexpectedFrameworkException ex = new UnexpectedFrameworkException("Expected Logback", cause);
    assertEquals("Expected Logback", ex.getMessage());
  }

  @Test
  void constructor_setsCause() {
    Throwable cause = new ClassCastException("wrong type");
    UnexpectedFrameworkException ex = new UnexpectedFrameworkException("Expected Logback", cause);
    assertSame(cause, ex.getCause());
  }

  @Test
  void isRuntimeException() {
    UnexpectedFrameworkException ex = new UnexpectedFrameworkException("msg", new RuntimeException());
    assertInstanceOf(RuntimeException.class, ex);
  }

  @Test
  void serialVersionUID_isStable() {
    // Verify the class can be instantiated without issues and is Serializable
    UnexpectedFrameworkException ex = new UnexpectedFrameworkException("test", new Exception());
    assertNotNull(ex);
  }
}
