package com.weborganic.bastille.security;

import java.io.IOException;

import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentRequest;

import com.topologi.diffx.xml.XMLWriter;

/**
 * A generator to obfuscate or see passwords in clear.
 * 
 * @version 14 October 2011
 * @author Christophe Lauret
 */
public final class ObfuscatorGenerator implements ContentGenerator {

  /**
   * {@inheritDoc}
   */
  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {
    String password = req.getParameter("password", "");
    if (password.length() > 0) {

      // Get the clear version of the password
      String clear = Obfuscator.clear(password);

      // If the password was clear obfuscate it; otherwise assume already obfuscated
      String obf = clear.equals(password)? Obfuscator.obfuscate(password) : password;

      xml.openElement("password", false);
      xml.attribute("clear", clear);
      xml.attribute("obsfuscated", obf);
      xml.closeElement();
    } else {
      xml.emptyElement("no-password");
    }

  }

}
