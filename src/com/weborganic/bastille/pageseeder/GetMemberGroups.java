package com.weborganic.bastille.pageseeder;

import java.io.IOException;

import javax.servlet.http.HttpSession;

import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentRequest;

import com.topologi.diffx.xml.XMLWriter;
import com.weborganic.bastille.security.Constants;
import com.weborganic.bastille.security.ps.PageSeederUser;

/**
 * A generator that returns the list of projects and groups a user is a member of.
 * 
 * <p>No parameter required, however, the user does need to be logged in as a 
 * {@link PageSeederUser}.
 * 
 * @author Christophe Lauret
 * @version 11 April 2011
 */
public final class GetMemberGroups implements ContentGenerator {

  /**
   * {@inheritDoc}
   */
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {

    // Is the user logged in?
    HttpSession session = req.getSession();
    Object o = session.getAttribute(Constants.SESSION_USER_ATTRIBUTE);

    // The user must be logged in.
    if (o instanceof PageSeederUser) {

      // Setting up the request
      PageSeederUser user = (PageSeederUser)o;
      String url = "/members/"+user.id()+"/projects";
      PSRequest tunnel = new PSRequest(PSResourceType.SERVICE, url);
      tunnel.setUser(user);

      // Start Serialising as XML
      tunnel.get(xml);

    } else {
      xml.openElement("ps-service", true);
      xml.attribute("error", "permission-error");
      xml.attribute("message", "This service requires a PageSeeder user to be logged in");
      xml.closeElement();
    }

  }

}
