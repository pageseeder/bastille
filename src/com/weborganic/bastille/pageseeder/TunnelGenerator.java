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
 * A generator than can tunnel a request through to PageSeeder.
 * 
 * <p>Use this generator to connect to servlet, service, etc... when no other generator provides
 * the same functionality. 
 * 
 * @author Christophe Lauret
 * @version 8 April 2011
 */
public final class TunnelGenerator implements ContentGenerator {

  /**
   * {@inheritDoc}
   */
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {

    // Determine what kind of request to make
    String servlet = req.getParameter("servlet");

    // Create the request 
    PSRequest tunnel = new PSRequest(PSResourceType.SERVLET, servlet);

    // Is the user logged in?
    HttpSession session = req.getSession();
    Object user = session.getAttribute(Constants.SESSION_USER_ATTRIBUTE);
    if (user instanceof PageSeederUser) {
      tunnel.setUser((PageSeederUser)user);
    }

    // Start Serialising as XML
    xml.openElement("tunnel", true);

    // Resource
    xml.openElement("resource");
    xml.attribute("type", "servlet");
    xml.attribute("name", servlet);
    xml.closeElement();

    // Grab the XML form the PageSeeder request
    tunnel.get(xml);

    // Done!
    xml.closeElement();
  }

}
