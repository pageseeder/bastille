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
package org.pageseeder.bastille.flint;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.pageseeder.bastille.flint.config.FlintConfig;
import org.pageseeder.bastille.flint.helpers.IndexMaster;
import org.pageseeder.bastille.flint.helpers.IndexNames;
import org.pageseeder.bastille.util.Errors;
import org.pageseeder.berlioz.BerliozException;
import org.pageseeder.berlioz.content.ContentGenerator;
import org.pageseeder.berlioz.content.ContentRequest;
import org.pageseeder.berlioz.content.ContentStatus;
import org.pageseeder.berlioz.util.ISO8601;
import org.pageseeder.flint.IndexException;
import org.pageseeder.xmlwriter.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
