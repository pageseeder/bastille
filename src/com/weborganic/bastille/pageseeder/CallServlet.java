package com.weborganic.bastille.pageseeder;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.http.HttpSession;

import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentRequest;

import com.topologi.diffx.xml.XMLWriter;
import com.weborganic.bastille.security.Constants;
import com.weborganic.bastille.security.ps.PageSeederUser;

/**
 * A generator than can connect to a PageSeeder and call a servlet.
 * 
 * <p>Specify the service to call using the <code>ps-servlet</code> parameter. All other 
 * parameters to this generator are transmitted to PageSeeder. 
 * 
 * <p>This is a generic generator; use this generator when no other specialised generator provides 
 * the same functionality. 
 * 
 * @author Christophe Lauret
 * @version 12 April 2011
 */
public final class CallServlet implements ContentGenerator {

  /**
   * {@inheritDoc}
   */
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {

    // Determine what kind of request to make
    String servlet = req.getParameter("ps-servlet");

    // Create the request 
    PSConnector connector = new PSConnector(PSResourceType.SERVLET, servlet);

    // Add parameters
    Enumeration<String> names = req.getParameterNames();
    while (names.hasMoreElements()) {
      String name = names.nextElement();
      String value = req.getParameter(name);
      if (!"ps-servlet".equals(name)) {
        connector.addParameter(name, value);
      }
    }

    // Is the user logged in?
    HttpSession session = req.getSession();
    Object user = session.getAttribute(Constants.SESSION_USER_ATTRIBUTE);
    if (user instanceof PageSeederUser) {
      connector.setUser((PageSeederUser)user);
    }

    // Grab the XML form the PageSeeder request
    connector.get(xml);
  }

}
