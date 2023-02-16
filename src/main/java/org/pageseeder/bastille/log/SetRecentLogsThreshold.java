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

import java.io.IOException;

import org.pageseeder.bastille.util.Errors;
import org.pageseeder.berlioz.content.ContentGenerator;
import org.pageseeder.berlioz.content.ContentRequest;
import org.pageseeder.berlioz.content.ContentStatus;
import org.pageseeder.xmlwriter.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Returns a list of the most recent logs entries.
 *
 * <p>This generators only work with the <i>LogBack</i> logging framework and if the filter has
 * been configured in the logs.
 * <p>
 *
 * @author Christophe Lauret
 * @version Bastille 0.8.6
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
  public void process(ContentRequest req, XMLWriter xml) throws IOException {

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
                     + "Switch to the LogBack library https://logback.qos.ch";
      xml.writeComment(message);
      req.setStatus(ContentStatus.SERVICE_UNAVAILABLE);

    }

  }

}
