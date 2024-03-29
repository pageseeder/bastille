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

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.pageseeder.bastille.log.logback.LogbackInfo;
import org.pageseeder.xmlwriter.XMLWritable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class for logs.
 *
 * @author Christophe Lauret
 *
 * @version Bastille 0.8.6
 * @since Bastille 0.8.5
 */
public final class Logs {

  /**
   * A simple enum for the log implementation in use in the system.
   */
  public enum LoggingFramework {

    /**
     * Logback framework.
     * @see <a href="https://logback.qos.ch">logback.qos.ch</a>
     */
    LOGBACK,

    /**
     * SLF4J in No operation mode.
     */
    NOP,

    /**
     * Apache Log4j.
     * @see <a href="https://logging.apache.org/log4j/1.2/">logging.apache.org/log4j/1.2/</a>
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
    if (framework == null) {
      initFramework();
    }
    return framework;
  }

  /**
   * Returns the logging framework in use by the system.
   *
   * @return the logging framework in use by the system.
   */
  public static LogInfo getLogInfo() {
    if (info == null) {
      initLogInfo();
    }
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
    }
    framework = fm;
  }

  /**
   * Initialise the log info by guessing from the implementation.
   */
  private static synchronized void initLogInfo() {
    if (framework == null) {
      initFramework();
    }
    if (Objects.requireNonNull(framework) == LoggingFramework.LOGBACK) {
      info = new LogbackInfo();
    } else {
      info = new NoLogInfo();
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
