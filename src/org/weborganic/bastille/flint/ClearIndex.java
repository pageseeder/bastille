/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.weborganic.bastille.flint;

import java.io.IOException;

import org.weborganic.bastille.flint.config.FlintConfig;
import org.weborganic.bastille.flint.helpers.IndexMaster;
import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.Beta;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentRequest;

import com.topologi.diffx.xml.XMLWriter;

/**
 * Clears the content of the index.
 *
 * <p>The index must be located in the '/index' directory.
 *
 * <p>Note: access to this is generator MUST be made secured in the Web descriptor.
 *
 * @author Christophe Lauret
 * @version 0.7.4 - 18 October 2012
 * @since 0.6.20
 */
@Beta
public final class ClearIndex implements ContentGenerator  {

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {

    String index = req.getParameter("index");

    if (index == null) {

      // Single index
      IndexMaster single = FlintConfig.getMaster();
      single.clear();
      xml.openElement("index");
      xml.attribute("status", "clear");
      xml.closeElement();

    } else {

      // Multiple indexes
      String[] names = index.split(",");
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

}
