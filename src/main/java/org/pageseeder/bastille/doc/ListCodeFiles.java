/*
 * Copyright 2015 Allette Systems (Australia)
 * http://www.allette.com.au
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pageseeder.bastille.doc;

import java.io.File;
import java.io.FileFilter;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.jspecify.annotations.Nullable;
import org.pageseeder.bastille.util.Paths;
import org.pageseeder.berlioz.content.Cacheable;
import org.pageseeder.berlioz.content.Environment;
import org.pageseeder.berlioz.content.Request;
import org.pageseeder.berlioz.content.Response;
import org.pageseeder.berlioz.content.XmlGenerator;
import org.pageseeder.berlioz.util.FileUtils;
import org.pageseeder.berlioz.xml.XmlWriter;

/**
 * Returns the XSLT documentation using the Cobble format
 *
 * @author Christophe Lauret
 * @version 0.13.0
 */
public final class ListCodeFiles implements XmlGenerator, Cacheable {

  /**
   * Filters XML files only.
   */
  private static final FileFilter DIRECTORIES_OR_XSLT_FILES = file -> file.isDirectory() || file.getName().endsWith(".xsl");

  /**
   * Formatter for the "modified" attribute, in the local time zone.
   */
  private static final DateTimeFormatter ISO8601_LOCAL = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

  @Override
  public @Nullable String getETag(Request req) {
    return null;
  }

  @Override
  public Response generate(Request req, XmlWriter xml) {
    Environment env = req.getEnvironment();
    File root = env.getPrivateFolder();
    File xslt = env.getPrivateFile("xslt");

    // XSLT documentation first
    toXml(xslt, xml, root);

    return Response.ok();
  }

  /**
   * Serialise the specified file as XML.
   *
   * @param f   the file.
   * @param xml the xml where the file information goes to.
   */
  private void toXml(File f, XmlWriter xml, File ancestor) {
    xml.openElement("file");
    xml.attribute("name", f.getName());
    xml.attribute("path", FileUtils.path(ancestor, f));
    if (f.exists()) {

      if (f.isDirectory()) {
        xml.attribute("type", "folder");
        File[] children = f.listFiles(DIRECTORIES_OR_XSLT_FILES);
        if (children != null) {
          for (File x : children) {
            toXml(x, xml, ancestor);
          }
        }

      } else {
        xml.attribute("type", "file");
        xml.attribute("media-type", Paths.getMediaType(f));
        xml.attribute("length", Long.toString(f.length()));
        xml.attribute("modified", ISO8601_LOCAL.format(Instant.ofEpochMilli(f.lastModified()).atZone(ZoneId.systemDefault())));
      }

    } else {
      xml.attribute("status", "not-found");
    }
    xml.closeElement();
  }

}
