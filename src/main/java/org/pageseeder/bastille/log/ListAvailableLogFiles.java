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

import org.pageseeder.berlioz.Beta;
import org.pageseeder.berlioz.content.ContentStatus;
import org.pageseeder.berlioz.content.Request;
import org.pageseeder.berlioz.content.Response;
import org.pageseeder.berlioz.content.XmlGenerator;
import org.pageseeder.berlioz.util.ISO8601;
import org.pageseeder.berlioz.xml.XmlWriter;

/**
 * Returns the log entries from the specified log file.
 *
 * @author Christophe Lauret
 * @version 0.8.6
 * @since 0.8.5
 */
@Beta
public final class ListAvailableLogFiles implements XmlGenerator {

  /**
   * Only accepts files ending with ".log".
   */
  private static final FileFilter LOG_FILES = file -> file.isFile() && file.getName().endsWith(".log");

  @Override
  public Response generate(Request req, XmlWriter xml) {

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
            toXml(log, xml);
          }
          xml.closeElement();
        } else {
          toXml(f, xml);
        }
      }
      xml.closeElement();

      return Response.ok();

    } else {

      // No recent logs
      xml.openElement("no-log-directories");
      String message = "The logging framework in use '"+Logs.getLoggingFramework()+"' does not support recent logs.\n"
          + "Switch to the LogBack library https://logback.qos.ch";
      xml.comment(message);
      xml.closeElement();

      return Response.status(ContentStatus.SERVICE_UNAVAILABLE);

    }
  }

  /**
   * Returns the log file as a
   *
   * @param log the log file
   * @param xml The XML writer
   */
  private static void toXml(File log, XmlWriter xml) {
    xml.openElement("log-file");
    xml.attribute("name", log.getName());
    xml.attribute("size", Long.toString(log.length()));
    xml.attribute("datetime", ISO8601.DATETIME.format(log.lastModified()));
    xml.closeElement();
  }

}
