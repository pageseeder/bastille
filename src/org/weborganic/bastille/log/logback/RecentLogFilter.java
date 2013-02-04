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

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

/**
 * A neutral logback filter which keep the last 100 recorded events in memory for quick look up.
 *
 * @author Christophe Lauret
 * @version 20 September 2012
 */
public final class RecentLogFilter extends Filter<ILoggingEvent> {

  /**
   * The maximum size of the queue.
   */
  private static final int MAX_HOLD_SIZE = 1000;

  /**
   * Where the events are stored temporarily.
   */
  private static final Queue<ILoggingEvent> RECENT = new ConcurrentLinkedQueue<ILoggingEvent>();

  /**
   * @param event the event to keep.
   * @return Always neutral.
   */
  @Override
  public FilterReply decide(ILoggingEvent event) {
    RECENT.add(event);
    if (RECENT.size() > MAX_HOLD_SIZE) RECENT.remove();
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
  static synchronized List<ILoggingEvent> getCopyOfEvents() {
    return new ArrayList<ILoggingEvent>(RECENT);
  }

}
