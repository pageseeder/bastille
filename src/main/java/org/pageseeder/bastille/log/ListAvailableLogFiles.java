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
import java.io.FileFilter;
import java.io.IOException;

import org.pageseeder.berlioz.Beta;
import org.pageseeder.berlioz.content.ContentGenerator;
import org.pageseeder.berlioz.content.ContentRequest;
import org.pageseeder.berlioz.content.ContentStatus;
import org.pageseeder.berlioz.util.ISO8601;
import org.pageseeder.xmlwriter.XMLWriter;

/**
 * Returns the log entries from the specified log file.
 *
 * @author Christophe Lauret
 * @version Bastille 0.8.6
 * @since Bastille 0.8.5
 */
@Beta
public final class ListAvailableLogFiles implements ContentGenerator {

  /**
   * Only accepts files ending with ".log".
   */
  private static final FileFilter LOG_FILES = new FileFilter() {

    @Override
    public boolean accept(File file) {
      return file.isFile() && file.getName().endsWith(".log");
    }

  };

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws IOException {

    // Get the information about the log framework
    LogInfo info = Logs.getLogInfo();
    if (info.supportsListLogDirectories()) {

      // Identify the log directory to read
      xml.openElement("log-files");
      for (File f : info.listLogDirectories()) {
        if (f.isDirectory()) {
          xml.openElement("log-directory");
          xml.attribute("name", f.getName());
          xml.attribute("path", f.getPath());
          File[] logs = f.listFiles(LOG_FILES);
          for (File log : logs) {
            toXML(log, xml);
          }
          xml.closeElement();
        } else {
          toXML(f, xml);
        }
      }
      xml.closeElement();

    } else {

      // No recent logs
      xml.openElement("no-log-directories");
      String message = "The logging framework in use '"+Logs.getLoggingFramework()+"' does not support recent logs.\n"
          + "Switch to the LogBack library https://logback.qos.ch";
      xml.writeComment(message);
      xml.closeElement();
      req.setStatus(ContentStatus.SERVICE_UNAVAILABLE);

    }
  }

  /**
   * Returns the log file as a
   *
   * @param log the log file
   * @param xml The XML writer
   *
   * @throws IOException If thrown while writing the XML.
   */
  private static void toXML(File log, XMLWriter xml) throws IOException {
    xml.openElement("log-file");
    xml.attribute("name", log.getName());
    xml.attribute("size", Long.toString(log.length()));
    xml.attribute("datetime", ISO8601.DATETIME.format(log.lastModified()));
    xml.closeElement();
  }

}
