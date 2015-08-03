/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.pageseeder.bastille.system;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

import org.pageseeder.bastille.util.Errors;
import org.pageseeder.berlioz.BerliozException;
import org.pageseeder.berlioz.Beta;
import org.pageseeder.berlioz.content.ContentGenerator;
import org.pageseeder.berlioz.content.ContentRequest;
import org.pageseeder.berlioz.content.ContentStatus;

import org.pageseeder.xmlwriter.XMLWriter;

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

    int interval = req.getIntParameter("interval", 100);

    // Check that the interval is positive
    if (interval <= 0) {
      Errors.error(req, xml, "client", "Interval must be strictly positive", ContentStatus.BAD_REQUEST);
      return;
    }

    long threadId = -1L;
    try {
      threadId = Long.parseLong(req.getParameter("thread", "-1"));
    } catch (NumberFormatException ex) {
      Errors.invalidParameter(req, xml, "thread");
      return;
    }

    try {
      ThreadMXBean bean = ManagementFactory.getThreadMXBean();
      // measure
      Sample start;
      Sample end;
      if (threadId == -1L) {
        long current = Thread.currentThread().getId();
        start = global(bean, current);
        Thread.sleep(interval);
        end = global(bean, current);
      } else {
        start = single(bean, threadId);
        Thread.sleep(interval);
        end = single(bean, threadId);
      }

      // Calculate
      long time = end.time() - start.time();
      long user = end.user() - start.user();
      long cpu = end.cpu() - start.cpu();

      // Write XML
      xml.openElement("sample");
      xml.attribute("interval", Long.toString(interval));
      xml.attribute("cpu", Long.toString(cpu*100 / time));
      xml.attribute("user", Long.toString(user*100 / time));
      xml.attribute("system", Long.toString((cpu - user)*100 / time));
      xml.closeElement();

    } catch (InterruptedException ex) {

    }
  }

  /**
   * Return a sample for the whole system.
   *
   * @param bean The thread management instance
   * @param current the ID of the current thread.
   *
   * @return the corresponding sample
   */
  private Sample global(ThreadMXBean bean, long current) {
    long cpu = 0L;
    long user = 0L;
    final long[] ids = bean.getAllThreadIds();
    for (long id : ids) {
      // Exclude this thread
      if (id == current) continue;
      final long c = bean.getThreadCpuTime(id);
      final long u = bean.getThreadUserTime(id);
      // Ignore dead threads
      if (c == -1 || u == -1) continue;
      cpu += c;
      user += u;
    }
    return new Sample(cpu, user);
  }

  /**
   * Return a sample for a single thread
   *
   * @param bean The thread management instance
   * @param id   The ID of the thread to measure.
   *
   * @return the corresponding sample
   */
  private Sample single(ThreadMXBean bean, long id) {
    final long cpu = bean.getThreadCpuTime(id);
    final long user = bean.getThreadUserTime(id);
    // The thread has died!
    if (cpu == -1 || user == -1) {
      return new Sample(0L, 0L);
    } else {
      return new Sample(cpu, user);
    }
  }

  /**
   * Co
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
