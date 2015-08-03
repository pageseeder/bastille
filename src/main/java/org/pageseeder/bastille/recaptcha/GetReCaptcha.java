/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.pageseeder.bastille.recaptcha;

import java.io.IOException;

import org.pageseeder.berlioz.BerliozException;
import org.pageseeder.berlioz.GlobalSettings;
import org.pageseeder.berlioz.content.Cacheable;
import org.pageseeder.berlioz.content.ContentGenerator;
import org.pageseeder.berlioz.content.ContentRequest;
import org.pageseeder.berlioz.content.ContentStatus;

import org.pageseeder.xmlwriter.XMLWriter;

/**
 * Returns the details for the recaptcha.
 *
 * @author Christophe Lauret
 * @version Bastille 0.8.4 - 1 Feb 2013
 *
 */
public final class GetReCaptcha implements ContentGenerator, Cacheable {

  @Override
  public String getETag(ContentRequest req) {
    return GlobalSettings.get("bastille.recaptcha.public-key");
  }

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {
    xml.openElement("recaptcha");

    // Display the public key
    try {
      ReCaptcha captcha = ReCaptcha.newReCaptcha();
      xml.attribute("public-key", captcha.publicKey());
      xml.attribute("server", captcha.server());

      // Display the HTML for convenience
      String message = req.getParameter("recaptcha_message");
      xml.openElement("html");
      captcha.toXHTMLForm(xml, message);
      xml.closeElement();

    } catch (ReCaptchaException ex) {
      req.setStatus(ContentStatus.SERVICE_UNAVAILABLE);
      xml.writeComment("Your reCaptcha is not configured properly: "+ex.getMessage());
    }

    xml.closeElement();
  }

}
