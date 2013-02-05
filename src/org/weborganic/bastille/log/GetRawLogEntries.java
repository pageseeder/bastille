/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.weborganic.bastille.log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.weborganic.bastille.util.Errors;
import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.Beta;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentRequest;
import org.weborganic.berlioz.content.ContentStatus;

import com.topologi.diffx.xml.XMLWriter;

/**
 * Returns the log entries from the specified log file.
 *
 * @author Christophe Lauret
 * @version Bastille 0.8.5 - 3 February 2013
 */
@Beta
public final class GetRawLogEntries implements ContentGenerator {

  /**
   * The default number of lines to read.
   */
  private static final int DEFAULT_MAX_LINES = 1000;

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {

    // the line
    int lines = req.getIntParameter("lines", DEFAULT_MAX_LINES);
    if (lines <= 0) {
      Errors.invalidParameter(req, xml, "lines");
      return;
    }

    String name = req.getParameter("name");
    if (name == null) {
      Errors.noParameter(req, xml, "name");
      return;
    }

    // Get the information about the log framework
    LogInfo info = Logs.getLogInfo();
    if (info.supportsListLogDirectories()) {

      // Parse
      File log = findLog(info, name);

      if (log != null) {

        // Write out
        xml.openElement("log");
        xml.attribute("name", name);
        tail(log, xml, lines);
        xml.closeElement();

      } else {

        // Could not be found
        xml.openElement("no-log");
        xml.attribute("name", name);
        xml.closeElement();
        req.setStatus(ContentStatus.NOT_FOUND);
      }

    } else {

      // No logs directories
      xml.openElement("no-log");
      String message = "The logging framework in use '"+Logs.getLoggingFramework()+"' does not support the listing log files.\n"
          + "Switch to the LogBack library http://logback.qos.ch";
      xml.writeComment(message);
      xml.closeElement();
      req.setStatus(ContentStatus.SERVICE_UNAVAILABLE);

    }
  }

  /**
   *
   * @param src
   * @param out
   * @param maxLines
   *
   * @throws FileNotFoundException
   * @throws IOException
   */
  private static void tail(File src, XMLWriter xml, int maxLines) throws FileNotFoundException, IOException {
    BufferedReader reader = new BufferedReader(new FileReader(src));
    String[] lines = new String[maxLines];
    int last = 0;
    int total = 0;
    for (String line = reader.readLine(); line != null; line = reader.readLine()) {
      if (last == lines.length) {
        last = 0;
      }
      lines[last++] = line;
      total++;
    }
    int n = total > maxLines? total - maxLines : 0;
    for (int i = last; i != last-1; i++) {
      if (i == lines.length) {
        i = 0;
      }
      String line = lines[i];
      if (line != null) {
        xml.openElement("line");
        xml.attribute("n", ++n);
        String level = getLevel(line);
        if (level != null)
          xml.attribute("level", level);
        xml.writeText(line);
        xml.closeElement();
      }
    }
  }

  /**
   * Returns the log instance for the specified name.
   *
   * @param name The name of the log file
   *
   * @return The first matching instance
   */
  private File findLog(LogInfo info, String name) {
    // Identify the log directory to read
    for (File f : info.listLogDirectories()) {
      File log = new File(f, name);
      if (log.exists()) {
        return log;
      }
    }
    return null;
  }

  /**
   * Returns the level from the line.
   *
   * @param name The name of the log file
   *
   * @return The first matching instance
   */
  private static String getLevel(String line) {
    String[] levels = new String[]{"INFO", "WARN", "ERROR", "DEBUG"};
    for (String level : levels) {
      if (line.indexOf(level) != -1) return level;
    }
    return null;
  }

}
