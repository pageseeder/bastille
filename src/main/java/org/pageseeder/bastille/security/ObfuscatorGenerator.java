/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.pageseeder.bastille.security;

import java.io.IOException;

import org.pageseeder.berlioz.BerliozException;
import org.pageseeder.berlioz.content.ContentGenerator;
import org.pageseeder.berlioz.content.ContentRequest;

import org.pageseeder.xmlwriter.XMLWriter;

/**
 * A generator to obfuscate or see passwords in clear.
 *
 * @version 14 October 2011
 * @author Christophe Lauret
 */
public final class ObfuscatorGenerator implements ContentGenerator {

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {
    String password = req.getParameter("password", "");
    if (password.length() > 0) {

      String clear = password;
      String obscur = password;

      if (password.startsWith("OB1:")) {
        clear = Obfuscator.clear(password);
      } else {
        obscur = Obfuscator.obfuscate(password);
      }

      xml.openElement("password", false);
      xml.attribute("clear", clear);
      xml.attribute("obscur", "OB1:"+obscur);
      xml.closeElement();
    } else {
      xml.emptyElement("no-password");
    }

  }

}
