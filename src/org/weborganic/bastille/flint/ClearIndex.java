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
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weborganic.bastille.flint.config.FlintConfig;
import org.weborganic.bastille.flint.helpers.IndexMaster;
import org.weborganic.bastille.flint.helpers.IndexNames;
import org.weborganic.bastille.util.Errors;
import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.Beta;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentRequest;

import com.topologi.diffx.xml.XMLWriter;

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
