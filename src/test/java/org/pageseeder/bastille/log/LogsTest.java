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

class LogsTest {

  @Test
  void getLoggingFramework_returnsNonNull() {
    assertNotNull(Logs.getLoggingFramework());
  }

  @Test
  void getLoggingFramework_consistent() {
    Logs.LoggingFramework first  = Logs.getLoggingFramework();
    Logs.LoggingFramework second = Logs.getLoggingFramework();
    assertSame(first, second);
  }

  @Test
  void loggingFramework_knownValues() {
    // All enum constants are reachable by name
    assertNotNull(Logs.LoggingFramework.valueOf("LOGBACK"));
    assertNotNull(Logs.LoggingFramework.valueOf("NOP"));
    assertNotNull(Logs.LoggingFramework.valueOf("LOG4J"));
    assertNotNull(Logs.LoggingFramework.valueOf("JAVA"));
    assertNotNull(Logs.LoggingFramework.valueOf("OTHER"));
  }

  @Test
  void loggingFramework_fiveValues() {
    assertEquals(5, Logs.LoggingFramework.values().length);
  }

  @Test
  void getLogInfo_returnsNonNull() {
    assertNotNull(Logs.getLogInfo());
  }

  @Test
  void getLogInfo_consistent() {
    LogInfo first  = Logs.getLogInfo();
    LogInfo second = Logs.getLogInfo();
    assertSame(first, second);
  }

  @Test
  void getLogInfo_listLogDirectories_neverNull() {
    assertNotNull(Logs.getLogInfo().listLogDirectories());
  }

  @Test
  void getLogInfo_listRecentEvents_neverNull() {
    assertNotNull(Logs.getLogInfo().listRecentEvents());
  }

  @Test
  void getLogInfo_recentEventThreshold_neverNull() {
    assertNotNull(Logs.getLogInfo().getRecentEventThreshold());
  }

  @Test
  void getLogInfo_setRecentEventThreshold_noThrow() {
    // Should not throw even when the underlying framework does not support it
    assertDoesNotThrow(() -> Logs.getLogInfo().setRecentEventThreshold(LogLevel.WARN));
  }

  @Test
  void getLogInfo_init_noThrow() {
    assertDoesNotThrow(() -> Logs.getLogInfo().init());
  }
}
