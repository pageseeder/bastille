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

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import org.jspecify.annotations.Nullable;
import org.pageseeder.berlioz.http.HttpAcceptHeader;
import org.pageseeder.berlioz.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class to return resources.
 *
 * @author Christophe Lauret
 * @version 0.13.0
 */
public final class Resources {

  /**
   * A logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(Resources.class);

  /** Utility class. */
  private Resources() {
  }

  /**
   * Returns the requested resource or <code>null</code>.
   *
   * @param name the name of the resource to retrieve.
   *
   * @return the corresponding byte array or <code>null</code> if not found or I/O error occurs.
   */
  public static byte @Nullable [] getResource(String name) {
    ClassLoader loader = Resources.class.getClassLoader();
    try (InputStream in = loader.getResourceAsStream(name)) {
      return in != null ? in.readAllBytes() : null;
    } catch (IOException ex) {
      LOGGER.warn("An error occurred while retrieving resource", ex);
      return null;
    }
  }

  /**
   * Indicates whether a resource with the given content type is compressible.
   *
   * @param contentType The content type of the resource.
   * @return <code>true</code> if the content type is compressible; <code>false</code> otherwise.
   */
  public static boolean isCompressible(@Nullable String contentType) {
    if (contentType == null) return false;
    return contentType.startsWith("text")
        || contentType.endsWith("xml")
        || contentType.endsWith("json")
        || contentType.endsWith("javascript");
  }

  /**
   * Indicates whether the client accepts GZip compression.
   *
   * @param req The servlet request we are processing.
   * @return <code>true</code> if the 'Accept-Encoding' header contains "gzip"; <code>false</code> otherwise.
   */
  public static boolean acceptsGZipCompression(HttpServletRequest req) {
    String encoding = req.getHeader(HttpHeaders.ACCEPT_ENCODING);
    return HttpAcceptHeader.accepts(encoding, "gzip");
  }

}
