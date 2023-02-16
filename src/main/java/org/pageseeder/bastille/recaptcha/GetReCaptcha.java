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
package org.pageseeder.bastille.recaptcha;

import java.io.IOException;

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
  public void process(ContentRequest req, XMLWriter xml) throws IOException {
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
