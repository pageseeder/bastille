/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.weborganic.bastille.log;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentRequest;
import org.weborganic.berlioz.content.ContentStatus;

import com.topologi.diffx.xml.XMLWritable;
import com.topologi.diffx.xml.XMLWriter;

/**
 * Returns a list of the most recent logs entries.
 *
 * <p>This generators only work with the <i>LogBack</i> logging framework and if the filter has
 * been configured in the logs.
 *
 *
 * @author Christophe Lauret
 * @version Bastille 0.8.5 - 4 February 2013
 */
public class GetRecentLogsEntries implements ContentGenerator {

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {
    // TODO Auto-generated method stub
  //  "org.weborganic.bastille.log.logback.RecentLogs"

    Package core = Package.getPackage("ch.qos.logback.core");
    if (core != null) {

      // Write out the recent logs as XML
      xml.openElement("recent-logs");
      XMLWritable recent = getRecentLogs();
      recent.toXML(xml);
      xml.closeElement();

    } else {
      // No recent logs
      xml.openElement("no-recent-logs");
      xml.writeComment("This service requires the LogBack library http://logback.qos.ch");
      xml.writeComment("Then use <filter class=\"org.weborganic.bastille.log.logback.RecentLogFilter\" />");
      xml.closeElement();
      req.setStatus(ContentStatus.SERVICE_UNAVAILABLE);
    }

  }


  /**
   * Returns the recent logs using reflection.
   *
   * @return the recent logs using reflection as a XMLWritable.
   *
   * @throws BerliozException
   */
  private static XMLWritable getRecentLogs() throws BerliozException {
    final Class<?>[] noArg = new Class<?>[0];
    final Object[] noObj = new Object[0];
    try {
      Class<?> recent = Class.forName("org.weborganic.bastille.log.logback.RecentLogs");
      Method getInstance = recent.getMethod("newInstance", noArg);
      Object o = getInstance.invoke(null, noObj);
      return (XMLWritable)o;
    } catch (ClassNotFoundException ex) {
      throw new BerliozException("Unable to get recent logs", ex);
    } catch (NoSuchMethodException ex) {
      throw new BerliozException("Unable to get recent logs", ex);
    } catch (InvocationTargetException ex) {
      throw new BerliozException("Unable to get recent logs", ex);
    } catch (IllegalAccessException ex) {
      throw new BerliozException("Unable to get recent logs", ex);
    }
  }
}
