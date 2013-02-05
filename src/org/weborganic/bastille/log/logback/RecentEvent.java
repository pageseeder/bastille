package org.weborganic.bastille.log.logback;

import java.io.IOException;
import java.io.Serializable;

import org.slf4j.Marker;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;
import org.weborganic.berlioz.util.ISO8601;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.CallerData;
import ch.qos.logback.classic.spi.ClassPackagingData;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;

import com.topologi.diffx.xml.XMLStringWriter;
import com.topologi.diffx.xml.XMLWritable;
import com.topologi.diffx.xml.XMLWriter;

/**
 * Holds the information about a log event.
 *
 * <p>By design, this class only keeps references to the objects passed by the logger.
 *
 * <p>It will not serialise the objects until requested by the generator.
 *
 * @author Christophe Lauret
 * @version Bastille 0.8.5 - 5 February 2013
 */
public final class RecentEvent implements XMLWritable, Serializable {

  /** As per requirement */
  private static final long serialVersionUID = 8445287374800936982L;

  private final long _timestamp;
  private final Marker _marker;
  private final Logger _logger;
  private final Level _level;
  private final String _message;
  private final Object[] _args;
  private final Throwable _throwable;

  /**
   * If the XML has been computed, we store it here, it won't be serialized though...
   */
  private volatile transient String _xml = null;

  /**
   * Creates a recent event using the
   *
   * @param marker     The marker
   * @param logger     The original logger
   * @param level      The logging level
   * @param p          The message
   * @param objects    The arguments
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
    XMLStringWriter xml = new XMLStringWriter(false, false);
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
    if (this._marker != null) xml.attribute("marker", this._marker.getName());

    Throwable throwable = this._throwable;
    if (throwable == null) {
      // Check if throwable included in array of arguments
      throwable = ft.getThrowable();
    }

    // Let's try to return some throwable information...
    if (throwable != null) {

      // If caller info is available
      StackTraceElement[] caller = toCallerData(this._logger);
      if ((caller != null) && (caller.length > 0)) {
        toXML(xml, caller[0]);
      }

      ThrowableProxy proxy = new ThrowableProxy(throwable);
      LoggerContext lc = this._logger.getLoggerContext();
      if (lc.isPackagingDataEnabled()) {
        proxy.calculatePackagingData();
      }

      // If a throwable has been submitted
      if (proxy != null) {
        toXML(xml, proxy, true);
      }
    }

    xml.closeElement();
    return xml.toString();
  }

  /**
   * Generate the stack trace element for an event.
   *
   * @param logger the logger for the event.
   * @return the stack trace element
   */
  private static StackTraceElement[] toCallerData(Logger logger) {
    return CallerData.extract(new Throwable(), Logger.class.getName(), logger.getLoggerContext().getMaxCallerDataDepth());
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
