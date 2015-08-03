/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.pageseeder.bastille.psml;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.pageseeder.berlioz.BerliozException;
import org.pageseeder.berlioz.content.Cacheable;
import org.pageseeder.berlioz.content.ContentGenerator;
import org.pageseeder.berlioz.content.ContentRequest;
import org.pageseeder.berlioz.util.FileUtils;

import org.pageseeder.xmlwriter.XMLWriter;

/**
 * Returns information about a file in the WEB-INF/psml based on the specified by the path info.
 *
 * <p>If the file is a directory, lists the files corresponding to the specified directory.
 *
 * @author Christophe Lauret
 * @version 0.7.5 - 25 October 2012
 * @since 0.7.5
 */
public final class GetContentFolderInfoAuto implements ContentGenerator, Cacheable {

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
  private static final Logger LOGGER = LoggerFactory.getLogger(GetContentFolderInfoAuto.class);

  /**
   * The content folder to recompute the
   */
  private volatile File ancestor = null;

  @Override
  public String getETag(ContentRequest req) {
    String pathInfo = req.getBerliozPath();
    PSMLFile psml = PSMLConfig.getContentFolder(pathInfo);
    if (!psml.exists()) return null;
    File f = psml.file();
    return Long.toString(f.lastModified());
  }

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {

    // Initialise
    if (this.ancestor == null) {
      File root = PSMLConfig.getRoot();
      this.ancestor = new File(root, "content");
    }

    // Identify the folder

    File folder = new File(this.ancestor, req.getBerliozPath());

    if (FileUtils.contains(this.ancestor, folder)) {
      LOGGER.info("Retrieving content folder information for {}", req.getBerliozPath());
      toXML(folder, xml);
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
