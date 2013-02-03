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
import java.lang.management.ThreadMXBean;

import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.Beta;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentRequest;

import com.topologi.diffx.xml.XMLWriter;

/**
 * Returns the User, System and CPU times.
 *
 * @author Christophe Lauret
 * @version Bastille 0.8.5 - 4 February 2013
 */
@Beta
public class GetCPUTime implements ContentGenerator {

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {

    long interval = req.getIntParameter("interval", 100);

    try {
      // measure
      long threadId = Thread.currentThread().getId();
      Sample start = sample();
      Thread.sleep(interval);
      Sample end = sample();

      // Calculate
      long time = end.time() - start.time();
      long user = end.user() - start.user();
      long cpu = end.cpu() - start.cpu();

      // Write XML
      xml.openElement("sample");
      xml.attribute("interval", Long.toString(interval));
      xml.attribute("cpu", Long.toString(cpu*100 / time));
      xml.attribute("user", Long.toString(user*100 / time));
      xml.closeElement();

    } catch (InterruptedException ex) {

    }
  }



  /**
   * Return a sample.
   */
  private Sample sample() {
    long cpu = 0L;
    long user = 0L;
    ThreadMXBean bean = ManagementFactory.getThreadMXBean( );
    final long[] ids = bean.getAllThreadIds( );
    for (long id : ids) {
      if (id == 123456)
          continue;   // TODO Exclude this thread
      final long c = bean.getThreadCpuTime(id);
      final long u = bean.getThreadUserTime(id);
      if ( c == -1 || u == -1 )
          continue;   // Thread died
      cpu += c;
      user += u;
    }
    return new Sample(cpu, user);
  }

  /**
   *
   */
  private static class Sample {
    public final long _time = System.nanoTime();
    public final long _cpu;
    public final long _user;
    public Sample(long cpu, long user) {
      this._cpu = cpu;
      this._user = user;
    }
    public long cpu() {
      return this._cpu;
    }
    public long user() {
      return this._user;
    }
    public long time() {
      return this._time;
    }
  }

}