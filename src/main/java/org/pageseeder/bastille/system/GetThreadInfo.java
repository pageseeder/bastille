/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.pageseeder.bastille.system;

import java.io.IOException;

import org.pageseeder.bastille.util.Errors;
import org.pageseeder.berlioz.BerliozException;
import org.pageseeder.berlioz.Beta;
import org.pageseeder.berlioz.content.ContentGenerator;
import org.pageseeder.berlioz.content.ContentRequest;

import org.pageseeder.xmlwriter.XMLWriter;

/**
 * Returns information about a thread.
 *
 * @author Christophe Lauret
 * @version Bastille 0.8.7 - 20 February 2013
 */
@Beta
public final class GetThreadInfo implements ContentGenerator {

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {

    long threadId = -1;
    String id = req.getParameter("id", "-1");
    if (id != null) {
      try {
        threadId = Long.parseLong(id);
      } catch (NumberFormatException ex) {
        Errors.invalidParameter(req, xml, "id");
        return;
      }
    } else {
      threadId = Thread.currentThread().getId();
    }

    Thread thread = Threads.getThread(threadId);
    toXML(thread, xml);
  }

  /**
   * Return all the threads with stack traces
   *
   * @param thread     The thread to serialise as XML
   * @param xml The XML writer
   *
   * @throws IOException If thrown while writing XML.
   */
  private static void toXML(Thread thread, XMLWriter xml)
      throws IOException {
    xml.openElement("thread", true);
    xml.attribute("id", Long.toString(thread.getId()));
    xml.attribute("name", thread.getName());
    xml.attribute("priority", thread.getPriority());
    xml.attribute("state", thread.getState().name());
    xml.attribute("alive", Boolean.toString(thread.isAlive()));
    xml.attribute("daemon", Boolean.toString(thread.isDaemon()));
    xml.attribute("group", thread.getThreadGroup().getName());

    StackTraceElement[] stacktrace = thread.getStackTrace();
    if (stacktrace != null) {
      xml.openElement("stacktrace");
      for (StackTraceElement element : stacktrace) {
        xml.openElement("element");
        String method = element.getMethodName();
        String filename = element.getFileName();
        int line = element.getLineNumber();
        xml.attribute("class", element.getClassName());
        if (filename != null) xml.attribute("filename", filename);
        if (method != null) xml.attribute("method", method);
        if (line >= 0) xml.attribute("line", line);
        xml.closeElement();
      }
      xml.closeElement();
    }

    xml.closeElement();
  }
}
