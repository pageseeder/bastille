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
import java.util.ArrayList;
import java.util.List;

import org.pageseeder.bastille.flint.config.FlintConfig;
import org.pageseeder.bastille.flint.helpers.IndexMaster;
import org.pageseeder.bastille.flint.helpers.IndexNames;
import org.pageseeder.bastille.util.Errors;
import org.pageseeder.berlioz.BerliozException;
import org.pageseeder.berlioz.Beta;
import org.pageseeder.berlioz.content.ContentGenerator;
import org.pageseeder.berlioz.content.ContentRequest;
import org.pageseeder.xmlwriter.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clears the content of the index.
 *
 * <p>The index(es) must be located in the directory specified by the <code>FlintConfig</code>.
 *
 * <p>Multiple indexes can be cleared at once if specified as a comma separated list.
 *
 * <p>Note: access to this is generator MUST be made secured in the Web descriptor.
 *
 * @author Christophe Lauret
 * @version 0.8.9 - 25 February 2013
 * @since 0.6.20
 */
@Beta
public final class ClearIndex implements ContentGenerator  {

  /** Logger for debugging */
  private static final Logger LOGGER = LoggerFactory.getLogger(CreateIndex.class);

  /** Name of the index parameter. */
  private static final String INDEX_PARAMETER = "index";

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {

    String index = req.getParameter(INDEX_PARAMETER);

    if (index == null) {

      // Check that we are in single index config.
      if (FlintConfig.hasMultiple()) {
        LOGGER.warn("Attempted to clear a single index in multiple index configuration");
        Errors.noParameter(req, xml, INDEX_PARAMETER);
        return;
      }

      // Single index
      IndexMaster single = FlintConfig.getMaster();
      single.clear();
      xml.openElement("index");
      xml.attribute("status", "clear");
      xml.closeElement();

    } else {

      // Check that we are in single index config.
      if (!FlintConfig.hasMultiple()) {
        LOGGER.warn("Attempted to clear a named index in single index configuration");
        Errors.invalidParameter(req, xml, INDEX_PARAMETER);
        return;
      }

      List<String> names = toIndexNames(index);

      // No valid index names where specified
      if (names.isEmpty()) {
        Errors.invalidParameter(req, xml, "index");
        return;
      }

      xml.openElement("indexes");
      for (String name : names) {
        IndexMaster master = FlintConfig.getMaster(name);
        master.clear();
        xml.openElement("index");
        xml.attribute("name", name);
        xml.attribute("status", "clear");
        xml.closeElement();
      }
      xml.closeElement();
    }
  }

  /**
   * Return the list of index names from the specified index parameter.
   *
   * <p>Only includes names which are valid and corresponding to an existing index.
   *
   * @param index The index parameter
   * @return the corresponding list.
   */
  private static List<String> toIndexNames(String index) {
    List<String> names = new ArrayList<String>();

    // Check the index names
    for (String name : index.split(",")) {
      if (IndexNames.isValid(name)) {
        File f = new File(FlintConfig.directory(), name);
        if (f.exists() && f.isDirectory()) {
          names.add(name);
        } else {
          LOGGER.debug("Invalid index name '{}' was specified", name);
        }
      } else {
        LOGGER.debug("Invalid index name '{}' was specified", name);
      }
    }
    return names;
  }

}
