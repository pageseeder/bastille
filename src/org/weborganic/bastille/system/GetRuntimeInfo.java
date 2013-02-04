/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.weborganic.bastille.system;

import java.io.IOException;

import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.Beta;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentRequest;

import com.topologi.diffx.xml.XMLWriter;

/**
 * Returns information from the runtime object.
 *
 * @author Christophe Lauret
 * @version Bastille 0.8.5 - 5 February 2013
 */
@Beta
public class GetRuntimeInfo implements ContentGenerator {

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {
    Runtime runtime = Runtime.getRuntime();

    xml.openElement("runtime");
    xml.attribute("processors", runtime.availableProcessors());

    // Memory information
    xml.openElement("memory");
    xml.attribute("free",  Long.toString(runtime.freeMemory()));
    xml.attribute("total", Long.toString(runtime.totalMemory()));
    xml.attribute("max",   Long.toString(runtime.maxMemory()));
    xml.closeElement();

    xml.closeElement();
  }

}
