/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.weborganic.bastille.system;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Map;
import java.util.Map.Entry;

import org.weborganic.bastille.util.Errors;
import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.Beta;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentRequest;

import com.topologi.diffx.xml.XMLWriter;

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

    boolean stacktraces = "true".equals(req.getParameter("stacktraces"));
    boolean threadtime = "true".equals(req.getParameter("threadtime"));



    ThreadMXBean bean = ManagementFactory.getThreadMXBean();

    xml.openElement("thread");

    ThreadInfo info = bean.getThreadInfo(threadId);

    //TODO
  }

}
