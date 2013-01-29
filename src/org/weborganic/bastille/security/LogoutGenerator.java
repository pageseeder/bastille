/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.weborganic.bastille.security;

import java.io.IOException;

import javax.servlet.http.HttpSession;

import org.weborganic.bastille.pageseeder.PSConnected;
import org.weborganic.bastille.security.ps.PageSeederAuthenticator;
import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentRequest;

import com.topologi.diffx.xml.XMLWriter;

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