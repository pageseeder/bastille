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
package org.pageseeder.bastille.xml;

import java.io.File;
import java.io.IOException;

import org.pageseeder.berlioz.BerliozException;
import org.pageseeder.berlioz.content.Cacheable;
import org.pageseeder.berlioz.content.ContentGenerator;
import org.pageseeder.berlioz.content.ContentRequest;
import org.pageseeder.berlioz.content.Environment;
import org.pageseeder.berlioz.util.FileUtils;
import org.pageseeder.berlioz.util.ISO8601;
import org.pageseeder.berlioz.util.MD5;
import org.pageseeder.xmlwriter.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Returns information about the file specified by the Berlioz path in the public folder.
 *
 * <p>If the file is a directory, lists the files corresponding to the specified directory.
 *
 * <h3>Configuration</h3>
 * <p>No configuration required for this generator.</p>
 *
 * <h3>Parameters</h3>
 * <p>No parameter needed for this generator.</p>
 *
 * <h3>Returned XML</h3>
 * <pre>{@code
 *   <file name="[filename]"
 *         path="[path_to_folder]"
 *         type="file"
 *         media-type="[media_type]"
 *         length="[file_size]"
 *         modified="[ISO8601_datetime]">
 * }</pre>
 *
 * <p>For a folder:
 * <pre>{@code
 *   <file name="[filename]" path="[path_from_root]" type="folder">
 *     <!-- for each file... -->
 *     <file name="" ... />
 *   </file>
 * }</pre>
 *
 * <p>If the file does not exist,
 * <pre>{@code
 *   <file name="[filename]"
 *         path="[path_to_folder]"
 *         status="not-found">
 * }</pre>
 *
 * <h4>File attributes</h4>
 * <ul>
 *   <li><code>name</code>: the name of the file (including extension);</li>
 *   <li><code>path</code>: the path from the root of the website;</li>
 *   <li><code>type</code>: is either 'file' or 'folder';</li>
 *   <li><code>length</code>: the full length of the file;</li>
 *   <li><code>modified</code>: the last modified date and time of the file using ISO8601;</li>
 *   <li><code>media-type</code>: the media type of the file based on the file extension as
 *   specified in Berlioz, if the file extension does not map to any media type returns
 *   "application/octet-stream";</li>
 *   <li><code>content-type</code>: same as media type (deprecated);</li>
 *   <li><code>status</code> equals 'not-found</li>
 * </ul>
 *
 * <p>Since Version 0.6.1, this generator returns both the <code>media-type</code> and
 * <code>content-type</code> attributes. Use <code>media-type</code>.
 *
 * @author Christophe Lauret
 * @version 0.6.35 - 21 May 2012
 * @since 0.6.0
 */
public final class GetFileInfo implements ContentGenerator, Cacheable  {

  /**
   * Where the private files are.
   */
  private volatile File folder = null;

  /**
   * Logger for debugging
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(GetFileInfo.class);

  /**
   * Returns a weak Etag based on the file path, length and last modified date.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public String getETag(ContentRequest req) {
    Environment env = req.getEnvironment();
    File file = env.getPublicFile(req.getBerliozPath());
    return MD5.hash(req.getBerliozPath()+"_"+file.length()+"x"+file.lastModified());
  }

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws IOException {
    Environment env = req.getEnvironment();
    if (this.folder == null) {
      this.folder = env.getPublicFolder();
    }
    File file = env.getPublicFile(req.getBerliozPath());

    if (file != null) {
      LOGGER.info("Retrieving file information for {}", file.getAbsolutePath());
      toXML(file, xml);
    } else {
      LOGGER.warn("Attempted to access non public file {}", req.getBerliozPath());
    }
  }

  /**
   * Serialise the specified file as XML.
   *
   * @param f   the file.
   * @param xml the xml where the file information goes to.
   * @throws IOException Should any IO occurs while retrieving the info or writing XML.
   */
  private void toXML(File f, XMLWriter xml) throws IOException {
    xml.openElement("file");
    xml.attribute("name", f.getName());
    xml.attribute("path", FileUtils.path(this.folder, f));
    if (f.exists()) {

      if (f.isDirectory()) {
        xml.attribute("type", "folder");
        for (File x : f.listFiles()) {
          toXML(x, xml);
        }

      } else {
        xml.attribute("type", "file");
        xml.attribute("content-type", getMediaType(f));
        xml.attribute("media-type", getMediaType(f));
        xml.attribute("length", Long.toString(f.length()));
        xml.attribute("modified", ISO8601.format(f.lastModified(), ISO8601.DATETIME));
      }

    } else {
      xml.attribute("status", "not-found");
    }
    xml.closeElement();
  }

  /**
   * Returns the MIME type of the given file based on the global MIME properties.
   *
   * <p>Falls back on <code>"application/octet-stream"</code> if the MIME type is unknown.
   *
   * @param f The file
   * @return the corresponding MIME type
   */
  private String getMediaType(File f) {
    String mime = FileUtils.getMediaType(f);
    return (mime != null)? mime : "application/octet-stream";
  }
}
