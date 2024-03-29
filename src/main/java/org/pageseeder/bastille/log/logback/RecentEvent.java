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

import java.io.IOException;
import java.io.Serializable;

import org.pageseeder.berlioz.util.ISO8601;
import org.pageseeder.xmlwriter.XML;
import org.pageseeder.xmlwriter.XMLStringWriter;
import org.pageseeder.xmlwriter.XMLWritable;
import org.pageseeder.xmlwriter.XMLWriter;
import org.slf4j.Marker;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.CallerData;
import ch.qos.logback.classic.spi.ClassPackagingData;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;

/**
 * Holds the information about a log event.
 *
 * <p>Each object only keeps references to the objects passed by the TurboFilter.
 *
 * <p>It will not serialise the objects until requested by the generator.
 *
 * @author Christophe Lauret
 *
 * @version Bastille 0.12.1
 * @since Bastille 0.8.5
 */
public final class RecentEvent implements XMLWritable, Serializable {

  /** As per requirement */
  private static final long serialVersionUID = 8445287374800936982L;

  /** Timestamp created when this object is instantiated */
  private final long _timestamp;

  /** Marker as passed by the filter */
  private final Marker _marker;

  /** Logger as passed by the filter */
  private final Logger _logger;

  /** Level as passed by the filter */
  private final Level _level;

  /** Message as passed by the filter */
  private final String _message;

  /** Arguments as passed by the filter */
  private final Object[] _args;

  /** Throwable as passed by the filter */
  private final Throwable _throwable;

  /**
   * The Logback API has changed, so we may have to fall back on previous version
   */
  private static volatile boolean _callerDataExtractFailed = false;

  /**
   * If the XML has been computed, we store it here, it won't be serialized though...
   */
  private transient volatile String _xml = null;

  /**
   * Creates a recent event using the objects send from the TurboFilter.
   *
   * @param marker     The marker
   * @param logger     The original logger
   * @param level      The logging level
   * @param message    The message
   * @param args       The arguments
   * @param throwable  Any error (may be <code>null</code>).
   */
  RecentEvent(Marker marker, Logger logger, Level level, String message, Object[] args, Throwable throwable) {
    this._marker = marker;
    this._logger = logger;
    this._level = level;
    this._message = message;
    this._args = args;
    this._throwable = throwable;
    this._timestamp = System.currentTimeMillis();
  }

  @Override
  public void toXML(XMLWriter xml) throws IOException {
    if (this._xml == null) {
      this._xml = toXML();
    }
    xml.writeXML(this._xml);
  }

  // Private helpers
  // ----------------------------------------------------------------------------------------------

  /**
   * Writes this event as XML.
   *
   * @return this event serialized as XML.
   *
   * @throws IOException Should an error occur while writing the XML.
   */
  private String toXML() throws IOException {
    XMLStringWriter xml = new XMLStringWriter(XML.NamespaceAware.No, false);
    xml.openElement("event");
    xml.attribute("level", this._level.toString());
    xml.attribute("logger", this._logger.getName());
    xml.attribute("timestamp", Long.toString(this._timestamp));
    xml.attribute("datetime", ISO8601.DATETIME.format(this._timestamp));

    // Format the message
    FormattingTuple ft = MessageFormatter.arrayFormat(this._message, this._args);
    String message = ft.getMessage();
    xml.attribute("message", message);

    // Any marker?
    if (this._marker != null) {
      xml.attribute("marker", this._marker.getName());
    }

    Throwable throwable = this._throwable;
    if (throwable == null) {
      // Check if throwable included in array of arguments
      throwable = ft.getThrowable();
    }

    // Let's try to return some throwable information...
    if (throwable != null) {

      // If caller info is available
      StackTraceElement[] caller = toCallerData(this._logger);
      if (caller != null && caller.length > 0) {
        toXML(xml, caller[0]);
      }

      ThrowableProxy proxy = new ThrowableProxy(throwable);
      LoggerContext lc = this._logger.getLoggerContext();
      if (lc.isPackagingDataEnabled()) {
        proxy.calculatePackagingData();
      }

      // If a throwable has been submitted
      toXML(xml, proxy, 0);
    }

    xml.closeElement();
    return xml.toString();
  }

  /**
   * Generate the stack trace element for an event.
   *
   * <p>The actual work is delegated to the {@link CallerData#extract} method.
   *
   * @param logger the logger for the event.
   * @return the stack trace element
   */
  private static StackTraceElement[] toCallerData(Logger logger) {
    StackTraceElement[] ste = null;
    if (!_callerDataExtractFailed) {
      try {
        final String name = Logger.class.getName();
        final LoggerContext context = logger.getLoggerContext();
        ste = CallerData.extract(new Throwable(), name, context.getMaxCallerDataDepth(), null);
      } catch (Error error) {
        System.err.println("Unable to extract caller data - this message will only be shown once");
        error.printStackTrace();
        _callerDataExtractFailed = true;
      }
    }
    return ste;
  }

  /**
   * Write a stack trace as XML.
   *
   * @param xml    The XML writer.
   * @param proxy  The stack trace to serialise
   * @param level  0 if it is the original exception.
   *
   * @throws IOException Should an error occur while writing the XML.
   */
  private static void toXML(XMLWriter xml, IThrowableProxy proxy, int level) throws IOException {
    StackTraceElementProxy[] steps = proxy.getStackTraceElementProxyArray();
    xml.openElement(level == 0? "throwable" : "cause", true);
    String message = proxy.getMessage();
    xml.attribute("message", message);
    for (StackTraceElementProxy step : steps) {
      xml.openElement("step", false);
      ClassPackagingData packaging = step.getClassPackagingData();
      if (packaging != null) {
        String location = packaging.getCodeLocation();
        if (location != null) {
          xml.attribute("location", location);
        }
        String version = packaging.getVersion();
        if (version != null) {
          xml.attribute("version", version);
        }
      }
      String s = step.toString();
      xml.writeText(s);
      // do not go past the servlet API
      if (s.contains("javax.servlet")) {
        xml.writeXML("...");
        xml.closeElement();
        break;
      }
      xml.closeElement();
    }
    IThrowableProxy cause = proxy.getCause();
    if (cause != null && level < 8) {
      toXML(xml, cause, level+1);
    }
    xml.closeElement();
  }

  /**
   * Write a caller as XML.
   *
   * @param xml    The XML writer.
   * @param caller The caller to serialise
   * @throws IOException Should an error occur while writing the XML.
   */
  private static void toXML(XMLWriter xml, StackTraceElement caller) throws IOException {
    xml.openElement("caller");
    xml.attribute("class", caller.getClassName());
    xml.attribute("method", caller.getMethodName());
    xml.attribute("file", caller.getFileName());
    xml.attribute("line", caller.getLineNumber());
    xml.closeElement();
  }

}
