/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.weborganic.bastille.log.logback;

import java.io.IOException;
import java.util.List;

import org.slf4j.Marker;
import org.weborganic.berlioz.util.ISO8601;

import ch.qos.logback.classic.spi.ClassPackagingData;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;

import com.topologi.diffx.xml.XMLWritable;
import com.topologi.diffx.xml.XMLWriter;

/**
 * Holds the most recent logging events.
 *
 * @author Christophe Lauret
 * @version 20 September 2012
 */
public final class RecentLogs implements XMLWritable {

  /**
   * The most recent logging events.
   */
  private final List<ILoggingEvent> _events;

  /**
   * Constructor.
   * @param events the list of recent event.
   */
  private RecentLogs(List<ILoggingEvent> events) {
    this._events = events;
  }

  /**
   * Returns a new instance with the a copy of the most recent events.
   *
   * @return the new instance with the a copy of the most recent events..
   */
  public static RecentLogs newInstance() {
//    org.slf4j.Logger root = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
//    if (root instanceof Logger) {
//      Logger classicRoot = (Logger)root;
//    }
    return new RecentLogs(RecentLogFilter.getCopyOfEvents());
  }

  @Override
  public void toXML(XMLWriter xml) throws IOException {
    xml.openElement("events");
    xml.attribute("datetime", ISO8601.CALENDAR_DATE.format(System.currentTimeMillis()));
    for (ILoggingEvent event : this._events) {
      toXML(xml, event);
    }
    xml.closeElement();
  }

  /**
   * Write a logging event as XML.
   *
   * @param xml   The XML writer.
   * @param event The event to serialise
   * @throws IOException Should an error occur while writing the XML.
   */
  private static void toXML(XMLWriter xml, ILoggingEvent event) throws IOException {
    xml.openElement("event");
    xml.attribute("level", event.getLevel().toString());
    xml.attribute("logger", event.getLoggerName());
    xml.attribute("timestamp", Long.toString(event.getTimeStamp()));
    xml.attribute("datetime", ISO8601.DATETIME.format(event.getTimeStamp()));
    xml.attribute("message", event.getFormattedMessage());

    // Any marker?
    Marker marker = event.getMarker();
    if (marker != null) xml.attribute("marker", marker.getName());

    // If caller info is available
    StackTraceElement[] caller = event.getCallerData();
    if ((caller != null) && (caller.length > 0)) {
      toXML(xml, caller[0]);
    }

    // If a throwable has been submitted
    IThrowableProxy proxy = event.getThrowableProxy();
    if (proxy != null) {
      toXML(xml, proxy, true);
    }

    xml.closeElement();
  }

  /**
   * Write a stack trace as XML.
   *
   * @param xml        The XML writer.
   * @param proxy      The stack trace to serialise
   * @param isOriginal If it is the original exception.
   *
   * @throws IOException Should an error occur while writing the XML.
   */
  private static void toXML(XMLWriter xml, IThrowableProxy proxy, boolean isOriginal) throws IOException {
    StackTraceElementProxy[] steps = proxy.getStackTraceElementProxyArray();
    xml.openElement(isOriginal? "throwable" : "cause", true);
    String message = proxy.getMessage();
    xml.attribute("message", message);
    for (StackTraceElementProxy step : steps) {
      xml.openElement("step", false);
      ClassPackagingData packaging = step.getClassPackagingData();
      if (packaging != null) {
        String location = packaging.getCodeLocation();
        if (location != null) xml.attribute("location", location);
        String version = packaging.getVersion();
        if (version != null) xml.attribute("version", version);
      }
      String s = step.toString();
      xml.writeText(s);
      // do not go past the servlet API
      if (s.indexOf("javax.servlet") != -1) {
        xml.writeXML("...");
        xml.closeElement();
        break;
      }
      xml.closeElement();
    }
    IThrowableProxy cause = proxy.getCause();
    if (!isOriginal && cause != null) {
      toXML(xml, cause, false);
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
