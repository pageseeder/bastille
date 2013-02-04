/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.weborganic.bastille.log;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.weborganic.bastille.util.Errors;
import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.Beta;
import org.weborganic.berlioz.GlobalSettings;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentRequest;
import org.weborganic.berlioz.content.ContentStatus;
import org.weborganic.berlioz.util.ISO8601;

import com.topologi.diffx.xml.XMLWriter;

/**
 * Returns the log entries from the specified log file.
 *
 * @author Christophe Lauret
 * @version Bastille 0.8.5 - 4 February 2013
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
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {

    // Get the configuration from Bastille
    String dirs = GlobalSettings.get("bastille.logs.path", "");
    if (dirs.length() == 0) {
      Errors.error(req, xml, "config", "The logs have not been configured", ContentStatus.SERVICE_UNAVAILABLE);
      return;
    }

    // Identify the log directory to read
    xml.openElement("log-files");
    for (String d : dirs.split(";")) {
      File f = new File(d);
      if (f.isDirectory()) {
        xml.openElement("log-directory");
        xml.attribute("name", f.getName());
        xml.attribute("path", f.getPath());
        File[] logs = f.listFiles(LOG_FILES);
        for (File log : logs) {
          xml.openElement("log-file");
          xml.attribute("name", log.getName());
          xml.attribute("size", Long.toString(log.length()));
          xml.attribute("datetime", ISO8601.CALENDAR_DATE.format(log.lastModified()));
          xml.closeElement();
        }
        xml.closeElement();
      }
    }
    xml.closeElement();
  }

}
