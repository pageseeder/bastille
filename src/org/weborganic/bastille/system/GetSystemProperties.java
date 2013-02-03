/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.weborganic.bastille.system;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.Properties;

import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.Beta;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentRequest;

import com.topologi.diffx.xml.XMLWriter;

/**
 * Returns system properties as returned by the <code>System</code> class.
 *
 * @author Christophe Lauret
 * @version Bastille 0.8.5 - 4 February 2013
 */
@Beta
public final class GetSystemProperties implements ContentGenerator {

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {

    xml.openElement("system");

    // Enumerate the system properties
    Properties properties = System.getProperties();
    for (Entry<Object, Object> p : properties.entrySet()) {
      xml.openElement("property");
      xml.attribute("name",  (String)p.getKey());
      xml.attribute("value", (String)p.getValue());
      xml.closeElement();
    }

    xml.closeElement();
  }

}