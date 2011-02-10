package com.weborganic.bastille.xml;

import java.io.File;

import java.io.IOException;
import java.text.SimpleDateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.content.Cacheable;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentGeneratorBase;
import org.weborganic.berlioz.content.ContentRequest;
import org.weborganic.berlioz.content.Environment;
import org.weborganic.berlioz.util.FileUtils;

import com.topologi.diffx.xml.XMLWriter;

/**
 * List the files corresponding to the specified directory.
 * 
 * @author Christophe Lauret 
 * @version 25 May 2010
 */
public class GetFileInfo extends ContentGeneratorBase implements ContentGenerator, Cacheable  {

  /**
   * Where the private files are.
   */
  private volatile File folder = null;

  /**
   * Logger for debugging
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(GetXMLFromPathInfo.class);

  /**
   * {@inheritDoc}
   */
  public String getETag(ContentRequest req) {
    Environment env = req.getEnvironment();
    File file = env.getPublicFile(req.getPathInfo());
    return req.getPathInfo()+"_"+file.length()+"x"+file.lastModified();
  }

  /**
   * {@inheritDoc}
   */
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {
    Environment env = req.getEnvironment();
    if (this.folder == null) this.folder = env.getPublicFolder();
    File file = env.getPublicFile(req.getPathInfo());

    if (file != null) {
      LOGGER.info("Retrieving file information for {}", file.getAbsolutePath());
      toXML(file, xml);
    } else {
      LOGGER.warn("Attempted to access non public file {}", req.getPathInfo());
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
      SimpleDateFormat ISO8601Local = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

      if (f.isDirectory()) {
        xml.attribute("type", "folder");
        for (File x : f.listFiles()) {
          toXML(x, xml);
        }

      } else {
        xml.attribute("type", "file");
        xml.attribute("content-type", getMIME(f));
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
  private String getMIME(File f) {
    String mime = FileUtils.getMediaType(f);
    return (mime != null)? mime : "application/octet-stream";
  }
}
