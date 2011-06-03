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
 * A generator than can connect to a PageSeeder and call a PageSeeder service.
 * 
 * <p>Specify the service to call using the <code>ps-service</code> parameter. All other 
 * parameters to this generator are transmitted to PageSeeder. 
 * 
 * <p>This is a generic generator; use this generator when no other specialised generator provides 
 * the same functionality.
 * 
 * @author Christophe Lauret
 * @version 12 April 2011
 */
public final class CallService implements ContentGenerator {

  /**
   * {@inheritDoc}
   */
  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {

    // Determine what kind of request to make
    String service = req.getParameter("ps-service");
    String method = req.getParameter("method", "GET");

    // Create the request
    PSConnector connector = new PSConnector(PSResourceType.SERVICE, service);

    // Add parameters
    Enumeration<String> names = req.getParameterNames();
    while (names.hasMoreElements()) {
      String name = names.nextElement();
      String value = req.getParameter(name);
      if (!"ps-service".equals(name)) {
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
    if (method.equalsIgnoreCase("GET")) {
      connector.get(xml);
    } else if (method.equalsIgnoreCase("POST")) {
      connector.post(xml);
    } else {
      // default
      connector.get(xml);
    }

  }

}
