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
package org.pageseeder.bastille.psml;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;

import org.pageseeder.bastille.util.Errors;
import org.pageseeder.berlioz.BerliozException;
import org.pageseeder.berlioz.content.Cacheable;
import org.pageseeder.berlioz.content.ContentGenerator;
import org.pageseeder.berlioz.content.ContentRequest;
import org.pageseeder.berlioz.content.ContentStatus;
import org.pageseeder.berlioz.util.FileUtils;
import org.pageseeder.xmlwriter.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Returns information about a file in the WEB-INF/psml based on the specified by the path info.
 *
 * @author Christophe Lauret
 */
public final class GetFolderInfo implements ContentGenerator, Cacheable {

  /**
   * Filters XML files only.
   */
  private static final FileFilter DIRECTORIES_OR_PSML_FILES = new FileFilter() {
    @Override
    public boolean accept(File file) {
      return file.isDirectory() || file.getName().endsWith(PSMLConfig.DEFAULT_PSML_EXTENSION);
    }
  };

  /**
   * Logger for debugging
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(GetFolderInfo.class);

  /**
   * The content folder to recompute the
   */
  private volatile File ancestor = null;

  @Override
  public String getETag(ContentRequest req) {
    String path = req.getParameter("path");
    if (path == null) return null;
    PSMLFile folder = PSMLConfig.getFolder(path);
    File f = folder.file();
    if (!f.exists()) return null;
    return Long.toString(f.lastModified());
  }

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {
    // Initialise
    if (this.ancestor == null) {
      this.ancestor = PSMLConfig.getRoot();
    }

    // Check that the path has been specified
    String path = req.getParameter("path");
    if (path == null) {
      Errors.noParameter(req, xml, "path");
      return;
    }

    // Grab the file
    PSMLFile folder = PSMLConfig.getFolder(path);
    LOGGER.debug("Retrieving overview for {}", folder);

    // If the PSML does not exist
    if (!folder.exists()) {
      req.setStatus(ContentStatus.NOT_FOUND);
    }

    if (FileUtils.contains(this.ancestor, folder.file())) {
      LOGGER.info("Retrieving content folder information for {}", req.getBerliozPath());
      toXML(folder.file(), xml);
    } else {
      LOGGER.warn("Attempted to access unauthorizes private file {}", req.getBerliozPath());
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
    xml.attribute("path", FileUtils.path(this.ancestor, f));
    if (f.exists()) {
      SimpleDateFormat ISO8601Local = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

      if (f.isDirectory()) {
        xml.attribute("type", "folder");
        for (File x : f.listFiles(DIRECTORIES_OR_PSML_FILES)) {
          toXML(x, xml);
        }

      } else {
        xml.attribute("type", "file");
        xml.attribute("content-type", getMediaType(f));
        xml.attribute("media-type", getMediaType(f));
        xml.attribute("length", Long.toString(f.length()));
        xml.attribute("modified", ISO8601Local.format(f.lastModified()));
      }

    } else {
      xml.attribute("status", "not-found");
    }
    xml.closeElement();
  }

  /**
   * Returns the MIME type of the given file based on the global MIME properties
   *
   * @param f The file
   * @return the corresponding MIME type
   */
  private String getMediaType(File f) {
    String mime = FileUtils.getMediaType(f);
    return (mime != null)? mime : "application/octet-stream";
  }

}
