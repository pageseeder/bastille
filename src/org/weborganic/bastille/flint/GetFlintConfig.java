/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.weborganic.bastille.flint;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weborganic.bastille.flint.config.FlintConfig;
import org.weborganic.bastille.flint.config.IFlintConfig;
import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.Beta;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentRequest;
import org.weborganic.berlioz.util.ISO8601;

import com.topologi.diffx.xml.XMLWriter;

/**
 * Return the flint configuration attributes and the list of indexes in the system.
 *
 * @author Christophe Lauret
 * @version 25 February 2013
 */
@Beta
public final class GetFlintConfig implements ContentGenerator {

  /** Logger for debugging */
  private static final Logger LOGGER = LoggerFactory.getLogger(GetFlintConfig.class);

  /**
   * To list only folders
   */
  private static final FileFilter FOLDERS_ONLY = new FileFilter() {
    @Override
    public boolean accept(File d) {
      return d.isDirectory();
    }
  };

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {
    IFlintConfig config = FlintConfig.get();
    File directory = FlintConfig.directory();
    //config
    xml.openElement("flint-config");
    // TODO: not the best way to display where the index is located!
    xml.attribute("directory", directory.getName().equals("index")? "index" : directory.getName());
    xml.attribute("multiple", Boolean.toString(config.hasMultiple()));
    xml.attribute("class", config.getClass().getName());
    if (config.hasMultiple()) {
      if (directory.exists() && directory.isDirectory()) {
        File[] subdirs = directory.listFiles(FOLDERS_ONLY);
        for (File f : subdirs) {
          toBasicIndexXML(xml, f);
        }
      }
    }
    xml.closeElement();
  }

  /**
   * Returns the basic index XML by just checking that the index directory exists and is recognised by Lucene.
   *
   * @param xml   The XML Writer to use.
   * @param index The directory containing the index.
   *
   * @throws IOException Should an error occur while writing the XML.
   */
  private void toBasicIndexXML(XMLWriter xml, File index) throws IOException {
    xml.openElement("index");
    xml.attribute("name", index.getName());
    Directory dir = null;
    try {
      dir = FSDirectory.open(index);
      boolean exists = IndexReader.indexExists(dir);
      xml.attribute("exists", Boolean.toString(exists));
      if (exists) {
        long modified = IndexReader.lastModified(dir);
        xml.attribute("modified", ISO8601.format(modified, ISO8601.DATETIME));
      }

    } catch (IOException ex) {
      LOGGER.warn("Error while tring to read index {}", index.getName());

    } finally {
      if (dir != null) dir.close();
    }

    xml.closeElement();
  }

}
