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
package org.pageseeder.bastille.util;

import org.pageseeder.berlioz.util.FileUtils;
import org.pageseeder.berlioz.xml.XmlWriter;

import java.io.File;
import java.io.FileFilter;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * A utility class to manipulate paths.
 *
 * @author Christophe Lauret
 *
 * @version 0.12.0
 */
public final class Paths {

  /**
   * The default media type returned when a file's media type is unknown.
   */
  private static final String DEFAULT_MEDIA_TYPE = "application/octet-stream";

  /**
   * Formatter for the "modified" attribute used by {@link #toXml(File, File, FileFilter, XmlWriter)},
   * in the local time zone.
   */
  private static final DateTimeFormatter ISO8601_LOCAL = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

  /**
   * Utility class.
   */
  private Paths() {
  }

  /**
   * Returns the media type of the given file based on the global MIME properties.
   *
   * @param f The file
   * @return the corresponding media type, or {@value #DEFAULT_MEDIA_TYPE} if unknown
   */
  public static String getMediaType(File f) {
    String mime = FileUtils.getMediaType(f);
    return (mime != null)? mime : DEFAULT_MEDIA_TYPE;
  }

  /**
   * Filters and normalizes the value in the path information.
   *
   * @param path The path to normalize.
   * @return The same path without an '/' at the end.
   */
  public static String normalize(String path) {
    String normalized = path;
    // trailing '/'
    if (path.endsWith("/") && path.length() > 1) {
      normalized = path.substring(0, path.length()-1);
    }
    // double '//'
    if (normalized.contains("//")) {
      normalized = normalized.replaceAll("//+", "/");
    }
    // self
    if (normalized.indexOf("./") == 0) {
      normalized = normalized.substring(2);
    }
    if (normalized.contains("/./")) {
      normalized = normalized.replace("/./", "/");
    }
    // parent
    while (normalized.indexOf("/../") > normalized.indexOf('/')+1) {
      normalized = normalized.replaceFirst("/[^/.]+/\\.\\./", "/");
    }
    if (normalized.indexOf("/../") > 1) {
      normalized = normalized.replaceFirst("^[^/.]+/\\.\\./", "");
    }
    return normalized;
  }

  /**
   * Computes the path from a path to another.
   *
   * <pre>{@code
   * /          to /      => .
   * /a         to /a     => .
   * /a         to /b     => ../b
   * /a/a/../c  to /a     => ../../a
   * /          to /b     => b
   * /          to /b/c   => b/c
   * }</pre>
   *
   * @param from The path to start from
   * @param to   The path to arrive at
   *
   * @return the path from a to b
   */
  public static String path(String from, String to) {
    String f = Paths.normalize('/'+from);
    String t = Paths.normalize('/'+to);
    if (f.equals(t)) return ".";
    StringBuilder path = new StringBuilder();
    int i = -1;
    while (f.length() > 1 && (i = f.indexOf('/', i+1)) >= 0) {
      if (path.length() > 0) {
        path.append('/');
      }
      path.append("..");
    }
    if (path.length() == 0 && !t.isEmpty()) {
      path.append(t.substring(1));
    } else {
      path.append(t);
    }
    return path.toString();
  }

  /**
   * Serialises the specified file as XML, recursing into directories and filtering their
   * children using the given filter.
   *
   * @param ancestor the root file the "path" attribute is computed from.
   * @param f        the file to serialise.
   * @param filter   the filter used to select files when recursing into a directory.
   * @param xml      the xml where the file information goes to.
   */
  public static void toXml(File ancestor, File f, FileFilter filter, XmlWriter xml) {
    xml.openElement("file");
    xml.attribute("name", f.getName());
    String path = FileUtils.path(ancestor, f);
    xml.attribute("path", path != null ? path : "");
    if (f.exists()) {
      if (f.isDirectory()) {
        xml.attribute("type", "folder");
        File[] children = f.listFiles(filter);
        if (children != null) {
          for (File x : children) {
            toXml(ancestor, x, filter, xml);
          }
        }

      } else {
        xml.attribute("type", "file");
        xml.attribute("media-type", getMediaType(f));
        xml.attribute("length", Long.toString(f.length()));
        xml.attribute("modified", ISO8601_LOCAL.format(Instant.ofEpochMilli(f.lastModified()).atZone(ZoneId.systemDefault())));
      }

    } else {
      xml.attribute("status", "not-found");
    }
    xml.closeElement();
  }

}
