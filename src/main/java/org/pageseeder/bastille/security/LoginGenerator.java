/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.pageseeder.bastille.security;

import java.io.IOException;

import javax.servlet.http.HttpSession;

import org.pageseeder.bastille.security.ps.PageSeederAuthenticator;
import org.pageseeder.berlioz.BerliozException;
import org.pageseeder.berlioz.Beta;
import org.pageseeder.berlioz.content.ContentGenerator;
import org.pageseeder.berlioz.content.ContentRequest;

import org.pageseeder.xmlwriter.XMLWriter;

/**
 * A generator to Login.
 *
 * @author Christophe Lauret
 * @version 0.6.2 - 7 April 2011
 * @since 0.6.2
 */
@Beta public final class LoginGenerator implements ContentGenerator {

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {
    String username = req.getParameter("username");
    String password = req.getParameter("password");

    // Get the authenticator
    PageSeederAuthenticator authenticator = new PageSeederAuthenticator();

    HttpSession session = req.getSession();

    // Already logged in?
    User user = (User)session.getAttribute(Constants.SESSION_USER_ATTRIBUTE);
    if (user != null) {
      authenticator.logout(user);
      session.invalidate();
      session = req.getSession();
    }

    // Perform login
    user = PageSeederAuthenticator.login(username, password);
    session.setAttribute(Constants.SESSION_USER_ATTRIBUTE, user);

    // XML
    xml.openElement("login");
    if (user != null) {
      xml.attribute("status", "ok");
      user.toXML(xml);
    } else  {
      xml.attribute("status", "ok");
    }
    xml.closeElement();

  }

}
