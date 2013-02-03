/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.weborganic.bastille.system;

import java.io.IOException;

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
public class GetThreads implements ContentGenerator {

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {

    ThreadGroup root = getRootThreadGroup();

    // organised
    toXML(root, xml);

  }

  /**
   * Get Root group thread.
   *
   * @return The root group thread.
   */
  private static ThreadGroup getRootThreadGroup() {
    ThreadGroup current = Thread.currentThread().getThreadGroup();
    ThreadGroup parent;
    while ((parent = current.getParent()) != null)
      current = parent;
    return current;
  }

  /**
   * Display the specified group thread as a tree.
   *
   * @param group The group thread to display as a tree
   * @param xml   The XML Writer
   *
   * @throws IOException Should an error occur while writing the XML
   */
  private static void toXML(ThreadGroup group, XMLWriter xml) throws IOException {
    xml.openElement("thread-group");
    xml.attribute("name", group.getName());

    // sub thread groups
    ThreadGroup[] groups = new ThreadGroup[group.activeGroupCount()];
    int gcount = group.enumerate(groups);
    for (ThreadGroup g : groups) {
      if (g != null) toXML(g, xml);
    }

    // threads
    Thread[] threads = new Thread[group.activeCount()];
    int tcount = group.enumerate(threads);
    for (Thread t : threads) {
      // Only display the threads part of the current group
      if (t != null && t.getThreadGroup() == group) {
        xml.openElement("thread", true);
        xml.attribute("id", Long.toString(t.getId()));
        xml.attribute("name", t.getName());
        xml.attribute("priority", t.getPriority());
        xml.attribute("state", t.getState().name());
        xml.attribute("alive", Boolean.toString(t.isAlive()));
        xml.attribute("daemon", Boolean.toString(t.isDaemon()));
        xml.closeElement();
      }
    }
    xml.closeElement();
  }
}
