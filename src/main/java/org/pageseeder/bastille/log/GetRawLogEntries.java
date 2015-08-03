/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.pageseeder.bastille.log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import org.pageseeder.bastille.util.Errors;
import org.pageseeder.berlioz.BerliozException;
import org.pageseeder.berlioz.Beta;
import org.pageseeder.berlioz.content.ContentGenerator;
import org.pageseeder.berlioz.content.ContentRequest;
import org.pageseeder.berlioz.content.ContentStatus;

import org.pageseeder.xmlwriter.XMLWriter;

/**
 * Returns the log entries from the specified log file.
 *
 * @author Christophe Lauret
 * @version Bastille 0.8.6 - 6 February 2013
 * @since Bastille 0.8.5
 */
@Beta
public final class GetRawLogEntries implements ContentGenerator {

  /**
   * Levels to look for.
   */
  private static final String[] LEVELS = new String[]{"INFO", "WARN", "ERROR", "DEBUG"};

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
   * Returns the tail of the specified file
   *
   * @param log      The log file to read.
   * @param xml      The XML writer
   * @param maxLines The maximum amount of lines to includes in the result.
   *
   * @throws IOException If thrown while reading the file or writing the XML out
   */
  private static void tail(File log, XMLWriter xml, int maxLines) throws IOException {
    // Get the tail
    Lines lines = tail(log, maxLines);

    // Write the lines out
    int n = lines.from();
    for (String line : lines) {
      xml.openElement("line");
      xml.attribute("n", ++n);
      String level = getLevel(line);
      if (level != null)
        xml.attribute("level", level);
      xml.writeText(line);
      xml.closeElement();
    }
  }

  /**
   * Returns the log instance for the specified name.
   *
   * @param info The logging framework info
   * @param name The name of the log file
   *
   * @return The first matching instance
   */
  private static File findLog(LogInfo info, String name) {
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
   * Returns the level from the line by looking for the level in the line.
   *
   * <p>Looking for any of "INFO", "WARN", "ERROR" or "DEBUG".
   *
   * @param line The name of the log file
   *
   * @return The first matching instance
   */
  private static String getLevel(String line) {
    for (String level : LEVELS) {
      if (line.indexOf(level) != -1) return level;
    }
    return null;
  }

  private static Lines tail(File log, int maxLines) throws IOException {
    BufferedReader reader = null;
    Lines lines = new Lines(maxLines);
    // Extract the last max lines first
    try {
      reader = new BufferedReader(new FileReader(log));
      for (String line = reader.readLine(); line != null; line = reader.readLine()) {
        lines.add(line);
      }
    } catch (IOException ex) {
      throw ex;
    } finally {
      if (reader != null) reader.close();
    }
    return lines;
  }

  /**
   *
   * @author Christophe Lauret
   * @version 21 February 2013
   */
  private static final class Lines implements Iterable<String> {

    /** The actual lines stored. */
    private final String[] _lines;

    /** The total number of lines added */
    private int total = 0;

    /**
     * @param capacity The maximum number of lines to keep track of.
     */
    public Lines(int capacity) {
      this._lines = new String[capacity];
    }

    /**
     * Add a line to this class ensuring that if the capacity is reached the new line replaces the oldest line.
     *
     * @param line The line to add.
     */
    private void add(String line) {
      this._lines[this.total % this._lines.length] = line;
      this.total++;
    }

    /**
     * @return the maximum number of lines this object can hold.
     */
    public int capacity() {
      return this._lines.length;
    }

    /**
     * @return index of the first line.
     */
    public int from() {
      return this.total > this._lines.length? this.total - this._lines.length : 0;
    }

    /**
     * @return index of the last line.
     */
    public int to() {
      return this.total;
    }

    @Override
    public Iterator<String> iterator() {
      return new Iterator<String>() {

        private int i = Lines.this.total > Lines.this._lines.length? Lines.this.total % Lines.this._lines.length : 0;
        private final int upto = Lines.this.total > Lines.this._lines.length? this.i + Lines.this._lines.length : Lines.this.total;

        @Override
        public boolean hasNext() {
          return this.i < this.upto;
        }

        @Override
        public String next() {
          String next = Lines.this._lines[this.i % Lines.this._lines.length];
          this.i++;
          return next;
        }

        @Override
        public void remove() {
          throw new UnsupportedOperationException();
        }
      };
    }

  }

}
