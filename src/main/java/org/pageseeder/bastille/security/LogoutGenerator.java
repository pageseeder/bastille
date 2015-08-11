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
package org.pageseeder.bastille.security;

import java.io.IOException;

import javax.servlet.http.HttpSession;

import org.pageseeder.bastille.pageseeder.PSConnected;
import org.pageseeder.bastille.security.ps.PageSeederAuthenticator;
import org.pageseeder.berlioz.BerliozException;
import org.pageseeder.berlioz.content.ContentGenerator;
import org.pageseeder.berlioz.content.ContentRequest;
import org.pageseeder.xmlwriter.XMLWriter;

/**
 * A generator to Logout.
 *
 * @author Christophe Lauret
 * @version 0.6.8 - 7 June 2011
 * @since 0.6.2
 */
@PSConnected
public final class LogoutGenerator implements ContentGenerator {

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {

    // Get the authenticator
    PageSeederAuthenticator authenticator = new PageSeederAuthenticator();
    HttpSession session = req.getSession();

    // Already logged in?
    User user = (User)session.getAttribute(Constants.SESSION_USER_ATTRIBUTE);
    if (user != null) {
      authenticator.logout(user);
      session.invalidate();
    }

    // XML
    xml.openElement("logout");
    xml.attribute("status", "ok");
    xml.closeElement();

  }

}
