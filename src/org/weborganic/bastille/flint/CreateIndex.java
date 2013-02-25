/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.weborganic.bastille.flint;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weborganic.bastille.flint.config.FlintConfig;
import org.weborganic.bastille.flint.helpers.IndexMaster;
import org.weborganic.bastille.flint.helpers.IndexNames;
import org.weborganic.bastille.util.Errors;
import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentRequest;
import org.weborganic.berlioz.content.ContentStatus;
import org.weborganic.berlioz.util.ISO8601;
import org.weborganic.flint.IndexException;

import com.topologi.diffx.xml.XMLWriter;

/**
 * Creates the directory for an index in the index folder specified by the Flint config.
 *
 * @author Jean-Baptiste Reure
 * @author Christophe Lauret
 *
 * @version 0.8.9 - 25 February 2013
 * @since 0.8.9
 */
public final class CreateIndex implements ContentGenerator  {

  /** Logger for debugging */
  private static final Logger LOGGER = LoggerFactory.getLogger(CreateIndex.class);

  /** Name of the index parameter. */
  private static final String INDEX_PARAMETER = "index";

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {

    // Index is required
    String index = req.getParameter(INDEX_PARAMETER);
    if (index == null) {
      Errors.noParameter(req, xml, INDEX_PARAMETER);
      return;
    } else if (!IndexNames.isValid(index)) {
      Errors.invalidParameter(req, xml, INDEX_PARAMETER);
      return;
    }

    // Ensuring that the index directory exists
    File root = FlintConfig.directory();
    File newIndex = new File(root, index);
    if (!newIndex.exists()) {
      LOGGER.info("Creating index '{}' in {} directory", index, root.getPath());
      boolean created = newIndex.mkdirs();
      if (created) {
        req.setStatus(ContentStatus.CREATED);
      } else {
        LOGGER.warn("Unable to create index directory for '{}'", index);
        Errors.error(req, xml, "server", "Unable to create index directory for "+index, ContentStatus.INTERNAL_SERVER_ERROR);
        return;
      }

    } else  if (!newIndex.isDirectory()) {
      LOGGER.warn("Unable to create index {}, a file with the same name already exists");
      Errors.error(req, xml, "client", "Unable to create index {}, file already exists", ContentStatus.CONFLICT);
      return;
    }

    // Retrieve it from the multiple indexes
    indexToXML(index, xml);
  }

  /**
   * Output the given index as XML
   *
   * @param name Name of the index
   * @param xml  XML to write the output
   *
   * @throws IOException If thrown while writing the XML.
   */
  private void indexToXML(String name, XMLWriter xml) throws IOException {
    xml.openElement("index");
    xml.attribute("name", name);
    IndexMaster master = FlintConfig.getMaster(name);
    IndexReader reader = null;
    try {
      reader = master.grabReader();
    } catch (IndexException ex) {
      xml.attribute("error", "Failed to load reader: "+ex.getMessage());
    }
    if (reader != null) {
      try {
        xml.attribute("last-modified", ISO8601.DATETIME.format(IndexReader.lastModified(reader.directory())));
        xml.attribute("current", Boolean.toString(reader.isCurrent()));
        xml.attribute("optimized", Boolean.toString(reader.isOptimized()));
        xml.openElement("documents");
        xml.attribute("count", reader.numDocs());
        xml.closeElement();
      } catch (Exception ex) {
        LOGGER.error("Error while extracting index info", ex);
      } finally {
        master.releaseSilently(reader);
      }
    } else {
      xml.attribute("error", "Null reader");
    }
    xml.closeElement();

  }

}
