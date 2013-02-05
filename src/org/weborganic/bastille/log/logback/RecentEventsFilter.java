/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.weborganic.bastille.log.logback;

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
 * A neutral logback filter which keep the last 100 recorded events in memory for quick look up.
 *
 * @author Christophe Lauret
 * @version 5 February 2013
 */
public final class RecentEventsFilter extends TurboFilter {

  /**
   * The maximum size of the queue.
   */
  private static final int MAX_HOLD_SIZE = 256;

  /**
   * Where the events are stored temporarily.
   */
  private static final Queue<RecentEvent> RECENT = new ConcurrentLinkedQueue<RecentEvent>();

  /**
   * The threshold for the recent events.
   */
  private static Level threshold = Level.INFO;

  /**
   * Singleton instance.
   */
  private static RecentEventsFilter singleton = new RecentEventsFilter();

  /**
   * Use singleton method.
   */
  private RecentEventsFilter() {}

  /**
   * @param event the event to keep.
   * @return Always neutral.
   */
  @Override
  public FilterReply decide(Marker marker, Logger logger, Level level, String p, Object[] objects, Throwable throwable) {
    // TODO filter correctly before adding it...
    if (level.isGreaterOrEqual(threshold)) {
      RecentEvent e = new RecentEvent(marker, logger, level, p, objects, throwable);
      RECENT.add(e);
      if (RECENT.size() > MAX_HOLD_SIZE) RECENT.remove();
    }
    return FilterReply.NEUTRAL;
  };

  /**
   * Clear the list of recent events.
   */
  public static void clearAll() {
    RECENT.clear();
  }

  /**
   * @return a copy of all the events on hol.
   */
  static synchronized List<RecentEvent> getCopyOfEvents() {
    return new ArrayList<RecentEvent>(RECENT);
  }

  /**
   * @return a singleton instance.
   */
  public static RecentEventsFilter singleton() {
    return singleton;
  }
}
