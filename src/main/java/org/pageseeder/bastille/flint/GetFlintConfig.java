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

import org.pageseeder.bastille.flint.config.FlintConfig;
import org.pageseeder.bastille.flint.config.IFlintConfig;
import org.pageseeder.bastille.util.FileFilters;
import org.pageseeder.berlioz.BerliozException;
import org.pageseeder.berlioz.Beta;
import org.pageseeder.berlioz.content.ContentGenerator;
import org.pageseeder.berlioz.content.ContentRequest;
import org.pageseeder.berlioz.util.ISO8601;
import org.pageseeder.flint.local.LocalIndex;
import org.pageseeder.xmlwriter.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        File[] subdirs = directory.listFiles(FileFilters.getFolders());
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
    long modified = LocalIndex.getLastModified(index);
    boolean exists = modified > 0;
    xml.attribute("exists", Boolean.toString(exists));
    if (exists) {
      xml.attribute("modified", ISO8601.format(modified, ISO8601.DATETIME));
    }
    xml.closeElement();
  }

}
