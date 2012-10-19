/*
 * This file is part of the Bastille library.
 *
 * Available under a commercial licence, contact Weborganic.
 *
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package com.weborganic.bastille.flint;

import java.io.IOException;

import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.Beta;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentRequest;

import com.topologi.diffx.xml.XMLWriter;
import com.weborganic.bastille.flint.config.FlintConfig;
import com.weborganic.bastille.flint.helpers.IndexMaster;

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
