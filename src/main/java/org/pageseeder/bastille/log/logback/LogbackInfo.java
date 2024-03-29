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

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.pageseeder.bastille.log.LogInfo;
import org.pageseeder.bastille.log.LogLevel;
import org.pageseeder.bastille.log.UnexpectedFrameworkException;
import org.pageseeder.xmlwriter.XMLWritable;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.TurboFilterList;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;

/**
 * An implementation of the log info interface for the Logback framework.
 *
 * @author Christophe Lauret
 *
 * @version Bastille 0.8.6
 * @since Bastille 0.8.5
 */
public final class LogbackInfo implements LogInfo {

  /**
   * Sets up the recent events turbo filter.
   */
  @Override
  public void init() {
    LoggerContext context = (LoggerContext)LoggerFactory.getILoggerFactory();
    // Ensure that it's not already there
    TurboFilterList list = context.getTurboFilterList();
    list.addIfAbsent(RecentEventsFilter.singleton());
  }

  /**
   * @return always <code>true</code>.
   */
  @Override
  public boolean supportsListLogDirectories() {
    return true;
  }

  /**
   * Returns the list of log directories by looking at all the {@link FileAppender} instances.
   *
   * @return the list of log files generated by Logback
   */
  @Override
  public List<File> listLogDirectories() {
    List<File> files = new ArrayList<>();
    try {
      LoggerContext context = (LoggerContext)LoggerFactory.getILoggerFactory();
      Set<FileAppender<ILoggingEvent>> fileappenders = getFileAppenders(context);
      for (FileAppender<ILoggingEvent> appender : fileappenders) {
        String pattern = appender.getFile();
        File f = new File(pattern);
        files.add(f.getParentFile());
      }

    } catch (ClassCastException ex) {
      throw new UnexpectedFrameworkException("Expected LogBack", ex);
    }
    return files;
  }

  /**
   * @return always <code>true</code>.
   */
  @Override
  public boolean supportsRecentEvents() {
    return true;
  }

  @Override
  public void setRecentEventThreshold(LogLevel level) {
    RecentEventsFilter.setThreshold(toLogbackLevel(level));
  }

  @Override
  public LogLevel getRecentEventThreshold() {
    return toBastilleLevel(RecentEventsFilter.getThreshold());
  }

  @Override
  public List<? extends XMLWritable> listRecentEvents() {
    return RecentEventsFilter.getCopyOfEvents();
  }

  // private helpers
  // ----------------------------------------------------------------------------------------------

  /**
   * Returns the list of all appenders used in the Logback context.
   *
   * @param context The logger context.
   *
   * @return The list of appenders in use.
   */
  private static Set<FileAppender<ILoggingEvent>> getFileAppenders(LoggerContext context) {
    Set<FileAppender<ILoggingEvent>> fileAppenders = new HashSet<>();
    List<Logger> loggers = context.getLoggerList();
    for (Logger logger : loggers) {
      for (Iterator<Appender<ILoggingEvent>> i = logger.iteratorForAppenders(); i.hasNext();) {
        Appender<ILoggingEvent> appender = i.next();
        if (appender instanceof FileAppender) {
          fileAppenders.add((FileAppender<ILoggingEvent>)appender);
        }
      }
    }
    return fileAppenders;
  }

  /**
   * Translate the level from Logback to Bastille.
   *
   * @param level The Logback level
   * @return the corresponding Bastille level
   */
  private static LogLevel toBastilleLevel(final Level level) {
    if (level == Level.DEBUG) return LogLevel.DEBUG;
    if (level == Level.INFO) return LogLevel.INFO;
    if (level == Level.WARN) return LogLevel.WARN;
    if (level == Level.ERROR) return LogLevel.ERROR;
    return LogLevel.OFF;
  }

  /**
   * Translate the level from Bastille to Logback.
   *
   * @param level The Bastille level
   * @return the corresponding Logback level
   */
  private static Level toLogbackLevel(final LogLevel level) {
    switch (level) {
      case DEBUG: return Level.DEBUG;
      case INFO: return Level.INFO;
      case WARN: return Level.WARN;
      case ERROR: return Level.ERROR;
      default: return Level.OFF;
    }
  }
}
