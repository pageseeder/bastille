/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.weborganic.bastille.log;

import java.io.IOException;
import java.util.List;

import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentRequest;
import org.weborganic.berlioz.content.ContentStatus;

import com.topologi.diffx.xml.XMLWritable;
import com.topologi.diffx.xml.XMLWriter;

/**
 * Returns a list of the most recent logs entries.
 *
 * <p>This generators only work with the <i>LogBack</i> logging framework and if the filter has
 * been configured in the logs.
 *
 *
 * @author Christophe Lauret
 * @version Bastille 0.8.5 - 4 February 2013
 */
public class GetRecentLogsEvents implements ContentGenerator {

  public GetRecentLogsEvents() {
    LogInfo info = Logs.getLogInfo();
    info.init();
  }

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {
    LogInfo info = Logs.getLogInfo();
    if (info.supportsRecentEvents()) {

      // Write out the recent logs as XML
      xml.openElement("recent-logs");
      List<? extends XMLWritable> events = info.listRecentEvents();
      for (XMLWritable e : events) {
        e.toXML(xml);
      }
      xml.closeElement();

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
