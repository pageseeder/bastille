/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.weborganic.bastille.log;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weborganic.bastille.log.logback.LogbackInfo;

import com.topologi.diffx.xml.XMLWritable;

/**
 * A utility class for logs.
 *
 * @author Christophe Lauret
 *
 * @version Bastille 0.8.6 - 6 February 2013
 * @since Bastille 0.8.5
 */
public final class Logs {

  /**
   * A simple enum for the log implementation in use in the system.
   */
  public enum LoggingFramework {

    /**
     * Logback framework.
     * @see http://logback.qos.ch/
     */
    LOGBACK,

    /**
     * SLF4J in No operation mode.
     */
    NOP,

    /**
     * Apache Log4j.
     * @see http://logging.apache.org/log4j/1.2/
     */
    LOG4J,

    /**
     * The Java logging from the util package.
     */
    JAVA,

    /**
     * Any other unrecognised logging framework.
     */
    OTHER
  }

  /**
   * The framework in use.
   */
  private static volatile LoggingFramework framework = null;

  /**
   * The framework in use.
   */
  private static volatile LogInfo info = null;

  /**
   * Utility class.
   */
  private Logs() {
  }

  /**
   * Returns the logging framework in use by the system.
   *
   * @return the logging framework in use by the system.
   */
  public static LoggingFramework getLoggingFramework() {
    if (framework == null) initFramework();
    return framework;
  }

  /**
   * Returns the logging framework in use by the system.
   *
   * @return the logging framework in use by the system.
   */
  public static LogInfo getLogInfo() {
    if (info == null) initLogInfo();
    return info;
  }

  // private helpers
  // ----------------------------------------------------------------------------------------------

  /**
   * Initialise the framework by guessing from the implementation.
   */
  private static synchronized void initFramework() {
    // If we don't know yet, let's find out
    Logger root = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    LoggingFramework fm = LoggingFramework.OTHER;
    if (root != null) {
      String implementation = root.getClass().getName();
      if (implementation.startsWith("ch.qos.logback")) {
        fm = LoggingFramework.LOGBACK;
      } else if ("org.slf4j.helpers.NOPLogger".equals(implementation)) {
        fm = LoggingFramework.NOP;
      }
      // TODO org.apache.log4j
      // TODO java.util.logging
    }
    framework = fm;
  }

  /**
   * Initialise the log info by guessing from the implementation.
   */
  private static synchronized void initLogInfo() {
    if (framework == null) initFramework();
    switch (framework) {
      case LOGBACK:
        info = new LogbackInfo();
        break;
      default:
        info = new NoLogInfo();
        break;
    }
  }

  /**
   * A simple log info for when there is no log info for the detected framework.
   *
   * <p>None of the feature are supported and all list returned are empty.
   */
  private static class NoLogInfo implements LogInfo {

    @Override
    public void init() {
    }

    @Override
    public boolean supportsListLogDirectories() {
      return false;
    }

    @Override
    public List<File> listLogDirectories() {
      return Collections.emptyList();
    }

    @Override
    public void setRecentEventThreshold(LogLevel level) {
    }

    @Override
    public LogLevel getRecentEventThreshold() {
      return LogLevel.OFF;
    }

    @Override
    public boolean supportsRecentEvents() {
      return false;
    }

    @Override
    public List<XMLWritable> listRecentEvents() {
      return Collections.emptyList();
    }
  }
}
