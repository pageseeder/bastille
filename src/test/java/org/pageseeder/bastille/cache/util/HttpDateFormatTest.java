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

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;

class HttpDateFormatTest {

  private final HttpDateFormat formatter = new HttpDateFormat();

  @Test
  void format_knownDate() {
    // RFC 2616 Section 3.3.1 example: Sun, 06 Nov 1994 08:49:37 GMT
    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.US);
    cal.set(1994, Calendar.NOVEMBER, 6, 8, 49, 37);
    cal.set(Calendar.MILLISECOND, 0);
    assertEquals("Sun, 06 Nov 1994 08:49:37 GMT", formatter.format(cal.getTime()));
  }

  @Test
  void parse_validDate() {
    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.US);
    cal.set(1994, Calendar.NOVEMBER, 6, 8, 49, 37);
    cal.set(Calendar.MILLISECOND, 0);
    Date expected = cal.getTime();

    Date parsed = formatter.parse("Sun, 06 Nov 1994 08:49:37 GMT");
    assertEquals(expected.getTime(), parsed.getTime());
  }

  @Test
  void parse_invalidDate_returnsEpoch() {
    Date result = formatter.parse("not-a-valid-date");
    assertEquals(0L, result.getTime());
  }

  @Test
  void parse_emptyString_returnsEpoch() {
    Date result = formatter.parse("");
    assertEquals(0L, result.getTime());
  }

  @Test
  void roundTrip_secondPrecision() {
    // HTTP dates have second precision — trim sub-second
    Date original = new Date((System.currentTimeMillis() / 1000) * 1000);
    String formatted = formatter.format(original);
    Date parsed = formatter.parse(formatted);
    assertEquals(original.getTime(), parsed.getTime());
  }

  @Test
  void format_containsGmt() {
    String result = formatter.format(new Date(0));
    assertTrue(result.endsWith("GMT"));
  }

  @Test
  void format_epoch() {
    String result = formatter.format(new Date(0));
    assertTrue(result.contains("1970"));
  }

  @Test
  void threadSafety_concurrentFormatAndParse() throws InterruptedException {
    Date date = new Date(1000000000000L);
    Thread[] threads = new Thread[10];
    boolean[] failed = {false};

    for (int i = 0; i < threads.length; i++) {
      threads[i] = new Thread(() -> {
        String formatted = formatter.format(date);
        Date parsed = formatter.parse(formatted);
        if (parsed.getTime() / 1000 != date.getTime() / 1000) {
          failed[0] = true;
        }
      });
    }
    for (Thread t : threads) t.start();
    for (Thread t : threads) t.join();
    assertFalse(failed[0], "Concurrent format/parse produced inconsistent results");
  }
}
