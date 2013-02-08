/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.weborganic.bastille.log;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weborganic.bastille.util.Errors;
import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentRequest;
import org.weborganic.berlioz.content.ContentStatus;

import com.topologi.diffx.xml.XMLWriter;

/**
 * Returns a list of the most recent logs entries.
 *
 * <p>This generators only work with the <i>LogBack</i> logging framework and if the filter has
 * been configured in the logs.
 *
 *
 * @author Christophe Lauret
 * @version Bastille 0.8.6 - 6 February 2013
 * @since Bastille 0.8.5
 */
public final class SetRecentLogsThreshold implements ContentGenerator {

  /** A logger. */
  private final static Logger LOGGER = LoggerFactory.getLogger(SetRecentLogsThreshold.class);

  /**
   * When this generator is instantiated, the logging framework information is loaded and initialized.
   *
   * <p>This assumes that the logging have been loaded before this constructor is called.
   */
  public SetRecentLogsThreshold() {
    LogInfo info = Logs.getLogInfo();
    info.init();
  }

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {

    LogInfo info = Logs.getLogInfo();
    if (info.supportsRecentEvents()) {

      //
      String threshold = req.getParameter("threshold");
      if (threshold == null) {
        Errors.noParameter(req, xml, "threshold");
        return;
      }

      try {
        LogLevel was = info.getRecentEventThreshold();
        LogLevel level = LogLevel.valueOf(threshold);
        info.setRecentEventThreshold(level);
        LOGGER.info("Switching recent log levels from {} to {}", was, level);

        // Write out the new threshold
        xml.openElement("recent-logs-threshold");
        xml.attribute("level", info.getRecentEventThreshold().toString());
        xml.attribute("was", was.toString());
        xml.closeElement();

      } catch (IllegalArgumentException ex) {
        Errors.invalidParameter(req, xml, "threshold");
      }

    } else {

      // No recent logs
      xml.openElement("no-recent-logs");
      String message = "The logging framework in use '"+Logs.getLoggingFramework()+"' does not support recent logs.\n"
                     + "Switch to the LogBack library http://logback.qos.ch";
      xml.writeComment(message);
      req.setStatus(ContentStatus.SERVICE_UNAVAILABLE);

    }

  }

}
