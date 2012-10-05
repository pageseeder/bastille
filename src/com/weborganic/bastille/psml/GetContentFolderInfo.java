/*
 * This file is part of the Bastille library.
 *
 * Available under a commercial licence, contact Weborganic.
 *
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package com.weborganic.bastille.psml;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.content.Cacheable;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentRequest;
import org.weborganic.berlioz.util.FileUtils;

import com.topologi.diffx.xml.XMLWriter;

/**
 * Returns information about a file in the WEB-INF/psml based on the specified by the path info.
 *
 * <p>If the file is a directory, lists the files corresponding to the specified directory.
 *
 * <h3>Configuration</h3>
 * <p>The root XML folder can be configured globally using the Berlioz configuration:
 * <p>For example:
 * <pre>{@code
 * <node name="bastille">
 *   <map/>
 *   <node name="xml">
 *     <map>
 *       <entry key="folder"     value="xml/content"/>
 *     </map>
 *   </node>
 * </node>
 * }</pre>
 *
 * <h3>Parameters</h3>
 * <p>The <var>path</var> to the resource can be specified using the path parameter, if the path parameter
 * is not specified, the path will use the path info.
 *
 * <h3>Returned XML</h3>
 * <p>XML for a file:
 * <pre>{@code
 *   <file name="[filename]"
 *         path="[path_to_folder]"
 *         type="file"
 *         media-type="[media_type]"
 *         length="[file_size]"
 *         modified="[ISO8601_datetime]">
 * }</pre>
 *
 * <p>XML for a folder:
 * <pre>{@code
 *   <file name="[filename]" path="[path_from_root]" type="folder">
 *     <!-- for each file... -->
 *     <file name="" ... />
 *   </file>
 * }</pre>
 *
 * <p>XML if the file does not exist,
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
 * @author Christophe Lauret
 * @version 0.6.5 - 23 May 2010
 * @since 0.6.0
 */
public final class GetContentFolderInfo implements ContentGenerator, Cacheable {

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
  private static final Logger LOGGER = LoggerFactory.getLogger(GetContentFolderInfo.class);

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
