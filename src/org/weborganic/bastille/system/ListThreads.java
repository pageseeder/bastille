/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.weborganic.bastille.system;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.Beta;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentRequest;

import com.topologi.diffx.xml.XMLWriter;

/**
 * Returns information from the runtime object.
 *
 * <ul>
 *   <li><code>NEW</code>. The thread has been created, but hasn't run yet.</li>
 *   <li><code>TERMINATED</code>. The thread has run to completion, but hasn't been deleted yet by the JVM.</li>
 *   <li><code>RUNNABLE</code>. The thread is running.</li>
 *   <li><code>BLOCKED</code>. The thread is blocked waiting on a lock (such as in a synchronized block or method).</li>
 *   <li><code>WAITING</code>. The thread is waiting until another thread calls notify().</li>
 *   <li><code>TIMED_WAITING</code>. The thread is either waiting or in a sleep().</li>
 * </ul>
 *
 * @author Christophe Lauret
 * @version Bastille 0.8.5 - 4 February 2013
 */
@Beta
public class ListThreads implements ContentGenerator {

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {

    boolean stacktraces = "true".equals(req.getParameter("stacktraces"));

    xml.openElement("threads");

    if (stacktraces) {
      // Use slow but convenient method to load the threads with their stack traces
      Map<Thread, StackTraceElement[]> all = Thread.getAllStackTraces();
      toXML(all, xml);

    } else {
      // Use old-school method
      ThreadGroup root = getRootThreadGroup();
      toXML(root, xml);
    }

    xml.closeElement();
  }

  /**
   * Get Root group thread.
   *
   * @return The root group thread.
   */
  private static ThreadGroup getRootThreadGroup() {
    ThreadGroup current = Thread.currentThread().getThreadGroup();
    ThreadGroup parent;
    while ((parent = current.getParent()) != null) {
      current = parent;
    }
    return current;
  }

  /**
   * Display the specified group thread.
   *
   * @param group The group thread to display as a tree
   * @param xml   The XML Writer
   *
   * @throws IOException Should an error occur while writing the XML
   */
  private static void toXML(ThreadGroup group, XMLWriter xml) throws IOException {
    // Grab all the threads
    Thread[] threads = new Thread[group.activeCount()];
    while (group.enumerate(threads, true) == threads.length) {
      threads = new Thread[threads.length * 2];
    }

    // threads
    for (Thread t : threads) {
      // Only display the threads part of the current group
      if (t != null) {
        xml.openElement("thread", true);
        xml.attribute("id", Long.toString(t.getId()));
        xml.attribute("name", t.getName());
        xml.attribute("priority", t.getPriority());
        xml.attribute("state", t.getState().name());
        xml.attribute("alive", Boolean.toString(t.isAlive()));
        xml.attribute("daemon", Boolean.toString(t.isDaemon()));
        xml.attribute("group", t.getThreadGroup().getName());
        xml.closeElement();
      }
    }
  }

  /**
   * Return all the threads with stack traces
   *
   * @param all The threads
   * @param xml The XML writer
   * @throws IOException If thrown while writing XML.
   */
  private static void toXML(Map<Thread, StackTraceElement[]> all, XMLWriter xml) throws IOException {
    for (Entry<Thread, StackTraceElement[]> e : all.entrySet()) {
      Thread t = e.getKey();
      StackTraceElement[] s = e.getValue();
      xml.openElement("thread", true);
      xml.attribute("id", Long.toString(t.getId()));
      xml.attribute("name", t.getName());
      xml.attribute("priority", t.getPriority());
      xml.attribute("state", t.getState().name());
      xml.attribute("alive", Boolean.toString(t.isAlive()));
      xml.attribute("daemon", Boolean.toString(t.isDaemon()));
      xml.attribute("group", t.getThreadGroup().getName());
      if (s != null) {
        xml.openElement("stacktrace");
        for (StackTraceElement ste : s) {
          xml.openElement("element");
          String method = ste.getMethodName();
          String filename = ste.getFileName();
          int line = ste.getLineNumber();
          xml.attribute("class", ste.getClassName());
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
}
