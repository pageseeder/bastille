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

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

/**
 * A date format to produce and parse dates compliant with the RFC 2616 - HTTP/1.1 protocol.
 *
 * <p>Section 3.3.1 defines the preferred full date and time as:
 * <pre>
 * HTTP-date    = rfc1123-date
 * rfc1123-date = wkday "," SP date1 SP time SP "GMT"
 * date1        = 2DIGIT SP month SP 4DIGIT
 * ; day month year (e.g., 02 Jun 1982)
 * time         = 2DIGIT ":" 2DIGIT ":" 2DIGIT
 * ; 00:00:00 - 23:59:59
 * wkday        = "Mon" | "Tue" | "Wed"
 * | "Thu" | "Fri" | "Sat" | "Sun"
 * month        = "Jan" | "Feb" | "Mar" | "Apr"
 * | "May" | "Jun" | "Jul" | "Aug"
 * | "Sep" | "Oct" | "Nov" | "Dec"
 * </pre>
 * <p/>
 * For example: <code>Sun, 07 Jan 2013 04:17:56 GMT</code>
 *
 * @author Christophe Lauret
 * @version 0.13.0
 */
public final class HttpDateFormat {

  /**
   * The HTTP date formatter. {@link DateTimeFormatter} is immutable and thread-safe, so a
   * single shared instance can be reused without synchronization.
   */
  private static final DateTimeFormatter FORMAT =
      DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US).withZone(ZoneOffset.UTC);

  /**
   * Formats the specified date.
   *
   * @param date The date to format
   *
   * @return A date formatted in accordance with Section 3.3.1 of RFC 2616
   */
  public String format(Instant date) {
    return FORMAT.format(date);
  }

  /**
   * Parses dates supplied in accordance with Section 3.3.1 of RFC 2616
   *
   * <p>If the date cannot be parsed, the start of POSIX time, 1/1/1970 is returned, which will
   * have the effect of expiring the content.
   *
   * @param date a date formatted in accordance with Section 3.3.1 of RFC 2616
   *
   * @return the parsed date or the epoch
   */
  public Instant parse(String date) {
    try {
      return Instant.from(FORMAT.parse(date));
    } catch (DateTimeParseException ex) {
      return Instant.EPOCH;
    }
  }
}
