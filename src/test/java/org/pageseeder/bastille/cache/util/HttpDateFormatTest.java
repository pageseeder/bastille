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

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class HttpDateFormatTest {

  private final HttpDateFormat formatter = new HttpDateFormat();

  @Test
  void format_knownDate() {
    // RFC 2616 Section 3.3.1 example: Sun, 06 Nov 1994 08:49:37 GMT
    Instant date = Instant.parse("1994-11-06T08:49:37Z");
    assertEquals("Sun, 06 Nov 1994 08:49:37 GMT", formatter.format(date));
  }

  @Test
  void parse_validDate() {
    Instant expected = Instant.parse("1994-11-06T08:49:37Z");
    Instant parsed = formatter.parse("Sun, 06 Nov 1994 08:49:37 GMT");
    assertEquals(expected, parsed);
  }

  @Test
  void parse_invalidDate_returnsEpoch() {
    Instant result = formatter.parse("not-a-valid-date");
    assertEquals(Instant.EPOCH, result);
  }

  @Test
  void parse_emptyString_returnsEpoch() {
    Instant result = formatter.parse("");
    assertEquals(Instant.EPOCH, result);
  }

  @Test
  void roundTrip_secondPrecision() {
    // HTTP dates have second precision — trim sub-second
    Instant original = Instant.ofEpochSecond(System.currentTimeMillis() / 1000);
    String formatted = formatter.format(original);
    Instant parsed = formatter.parse(formatted);
    assertEquals(original, parsed);
  }

  @Test
  void format_containsGmt() {
    String result = formatter.format(Instant.EPOCH);
    assertTrue(result.endsWith("GMT"));
  }

  @Test
  void format_epoch() {
    String result = formatter.format(Instant.EPOCH);
    assertTrue(result.contains("1970"));
  }

  @Test
  void threadSafety_concurrentFormatAndParse() throws InterruptedException {
    Instant date = Instant.ofEpochMilli(1000000000000L);
    Thread[] threads = new Thread[10];
    boolean[] failed = {false};

    for (int i = 0; i < threads.length; i++) {
      threads[i] = new Thread(() -> {
        String formatted = formatter.format(date);
        Instant parsed = formatter.parse(formatted);
        if (parsed.getEpochSecond() != date.getEpochSecond()) {
          failed[0] = true;
        }
      });
    }
    for (Thread t : threads) t.start();
    for (Thread t : threads) t.join();
    assertFalse(failed[0], "Concurrent format/parse produced inconsistent results");
  }
}
