package com.weborganic.bastille.security;

import java.io.IOException;

import javax.servlet.http.HttpSession;

import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.Beta;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentRequest;

import com.topologi.diffx.xml.XMLWriter;
import com.weborganic.bastille.security.ps.PageSeederAuthenticator;

/**
 * A generator to Login.
 * 
 * @author Christophe Lauret
 * @version 7 April 2011
 */
@Beta public class LoginGenerator implements ContentGenerator {

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
    user = authenticator.login(username, password);
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
