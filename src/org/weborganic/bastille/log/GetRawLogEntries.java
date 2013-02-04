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
import org.weborganic.berlioz.GlobalSettings;
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
  private static final int DEFAULT_MAX_LINES = 200;

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {

    // the line
    int lines = req.getIntParameter("lines", DEFAULT_MAX_LINES);
    if (lines <= 0) {
      Errors.invalidParameter(req, xml, "lines");
      return;
    }

    // Get the configuration from Bastille
    String dirs = GlobalSettings.get("bastille.logs.path", "");
    if (dirs.length() == 0) {
      Errors.error(req, xml, "config", "The logs have not been configured", ContentStatus.SERVICE_UNAVAILABLE);
      return;
    }

    // Identify the log directory to read
    String[] d = dirs.split(";");
    // FIXME Check that the path is valid

    String path = req.getParameter("path");
    if (path == null) {
      Errors.noParameter(req, xml, "path");
      return;
    }
    if (!path.endsWith(".log")) {
      Errors.invalidParameter(req, xml, "path");
      return;
    }

    // Parse
    File log = new File(path);

    // Write out
    xml.openElement("log");
    xml.attribute("name", log.getName());
    tail(log, xml, lines);
    xml.closeElement();

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
    for (String line = reader.readLine(); line != null; line = reader.readLine()) {
      if (last == lines.length) {
        last = 0;
      }
      lines[last++] = line;
    }
    for (int i = last; i != last-1; i++) {
      if (i == lines.length) {
        i = 0;
      }
      String line = lines[i];
      if (line != null)
        xml.element("line", line);
    }
  }

}
