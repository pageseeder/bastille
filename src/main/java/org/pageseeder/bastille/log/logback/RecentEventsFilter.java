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
package org.pageseeder.bastille.log.logback;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Marker;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;

/**
 * A neutral logback Turbo filter which keep the last 256 recorded events in memory for quick look up.
 *
 * @author Christophe Lauret
 *
 * @version Bastille 0.8.6
 * @since Bastille 0.8.5
 */
public final class RecentEventsFilter extends TurboFilter {

  /**
   * The maximum size of the queue.
   */
  private static final int MAX_HOLD_SIZE = 256;

  /**
   * Where the events are stored temporarily.
   */
  private static final Queue<RecentEvent> RECENT = new ConcurrentLinkedQueue<>();

  /**
   * The threshold for the recent events.
   */
  private static Level threshold = Level.INFO;

  /**
   * Singleton instance.
   */
  private static final RecentEventsFilter SINGLETON = new RecentEventsFilter();

  /**
   * Use singleton method.
   */
  private RecentEventsFilter() {
  }

  /**
   * {@inheritDoc}
   *
   * @return Always neutral.
   */
  @Override
  public FilterReply decide(Marker marker, Logger logger, Level level, String m, Object[] a, Throwable t) {
    if (level.isGreaterOrEqual(threshold) && (m != null || a != null || t != null)) {
      RecentEvent e = new RecentEvent(marker, logger, level, m, a, t);
      RECENT.add(e);
      if (RECENT.size() > MAX_HOLD_SIZE) {
        RECENT.remove();
      }
    }
    return FilterReply.NEUTRAL;
  }

  /**
   * Clear the list of recent events.
   */
  public static void clearAll() {
    RECENT.clear();
  }

  /**
   * @param level the threshold to set
   */
  public static void setThreshold(Level level) {
    RecentEventsFilter.threshold = level;
  }

  /**
   * @return the threshold
   */
  public static Level getThreshold() {
    return threshold;
  }

  /**
   * @return a copy of all the events on hol.
   */
  static synchronized List<RecentEvent> getCopyOfEvents() {
    return new ArrayList<>(RECENT);
  }

  /**
   * @return a singleton instance.
   */
  public static RecentEventsFilter singleton() {
    return SINGLETON;
  }
}
