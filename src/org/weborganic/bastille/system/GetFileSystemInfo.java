/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.weborganic.bastille.system;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.Beta;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentRequest;

import com.topologi.diffx.xml.XMLWriter;

/**
 * Returns information about the underlying file system.
 *
 * @author Christophe Lauret
 * @version Bastille 0.8.4 - 2 February 2013
 */
@Beta
public final class GetFileSystemInfo implements ContentGenerator {

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {

    // Free and total space
    File pub = req.getEnvironment().getPublicFolder();
    File priv = req.getEnvironment().getPrivateFolder();
    xml.openElement("file-system");
    xml.attribute("free-space", Long.toString(pub.getFreeSpace()));
    xml.attribute("total-space", Long.toString(pub.getTotalSpace()));

    if ("true".equals(req.getParameter("details"))) {
      // Go through public and private folders
      analyze(pub, "public", xml);
      analyze(priv, "private", xml);
    }

    xml.closeElement();
  }


  private static void analyze(File dir, String name, XMLWriter xml) throws IOException {
    DirInfo global = new DirInfo(name);
    List<DirInfo> locals = new ArrayList<DirInfo>();
    File[] files = dir.listFiles();
    for (File f : files) {
      if (f.isDirectory()) {
        if (!"WEB-INF".equals(f.getName())) {
          DirInfo local = new DirInfo(f.getName());
          analyze(local, f);
          locals.add(local);
        }
      } else {
        global.add(f);
      }
    }
    xml.openElement(name);
    for (DirInfo local : locals) {
      global.add(local);
    }
    xml.attribute("total-size", Long.toString(global.getSize()));
    xml.attribute("total-count", global.getCount());
    for (DirInfo local : locals) {
      xml.openElement("directory");
      xml.attribute("name", local.getName());
      xml.attribute("file-size", Long.toString(local.getSize()));
      xml.attribute("file-count", local.getCount());
      xml.closeElement();
    }
    xml.closeElement();
  }

  private static void analyze(DirInfo local, File dir) {
    File[] files = dir.listFiles();
    for (File f : files) {
      if (f.isDirectory()) {
        analyze(local, f);
      } else {
        local.add(f);
      }
    }
  }

  private static class DirInfo {

    private final String _name;
    private long size = 0;
    private int count = 0;

    public DirInfo(String name) {
      this._name = name;
    }

    public void add(File f) {
      this.size = this.size + f.length();
      this.count++;
    }

    public void add(DirInfo info) {
      this.size = this.size + info.getSize();
      this.count = this.count + info.getCount();
    }

    public String getName() {
      return this._name;
    }
    public long getSize() {
      return this.size;
    }

    public int getCount() {
      return this.count;
    }
  }

}
