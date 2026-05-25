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

class LogLevelTest {

  @Test
  void values_count() {
    assertEquals(5, LogLevel.values().length);
  }

  @Test
  void values_order() {
    LogLevel[] levels = LogLevel.values();
    assertEquals(LogLevel.DEBUG, levels[0]);
    assertEquals(LogLevel.INFO,  levels[1]);
    assertEquals(LogLevel.WARN,  levels[2]);
    assertEquals(LogLevel.ERROR, levels[3]);
    assertEquals(LogLevel.OFF,   levels[4]);
  }

  @Test
  void valueOf_debug() {
    assertEquals(LogLevel.DEBUG, LogLevel.valueOf("DEBUG"));
  }

  @Test
  void valueOf_info() {
    assertEquals(LogLevel.INFO, LogLevel.valueOf("INFO"));
  }

  @Test
  void valueOf_warn() {
    assertEquals(LogLevel.WARN, LogLevel.valueOf("WARN"));
  }

  @Test
  void valueOf_error() {
    assertEquals(LogLevel.ERROR, LogLevel.valueOf("ERROR"));
  }

  @Test
  void valueOf_off() {
    assertEquals(LogLevel.OFF, LogLevel.valueOf("OFF"));
  }

  @Test
  void valueOf_unknown_throws() {
    assertThrows(IllegalArgumentException.class, () -> LogLevel.valueOf("TRACE"));
  }

  @Test
  void name_roundTrip() {
    for (LogLevel level : LogLevel.values()) {
      assertEquals(level, LogLevel.valueOf(level.name()));
    }
  }
}
