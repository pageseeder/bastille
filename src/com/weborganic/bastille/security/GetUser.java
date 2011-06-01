package com.weborganic.bastille.security;

import java.io.IOException;

import javax.servlet.http.HttpSession;

import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentRequest;

import com.topologi.diffx.xml.XMLWriter;

/**
 * Returns the XML for the user currently logged in.
 * 
 * <p>The actual user implementation depends on the authentication mechanism.
 * 
 * <p>A user is considered to be logged in if a <code>User</code> instance can be found in the 
 * current session; this happens when the user logs in.
 * 
 * <h3>Configuration</h3>
 * <p>There is no configuration associated with this generator; however the login and logout 
 * servlets must be configured in the Web descriptor (<code>/WEB-INF/web.xml</code>).
 *
 * <h3>Parameters</h3>
 * <p>There is no parameter.
 * 
 * <h3>Returned XML</h3>
 * <p>This generator only returns the user data if the user is logged in.
 * <p>Here is a sample XML of a PageSeeder user.
 * <pre>{@code <user type="pageseeder">
 *   <id>123</id>
 *   <username>jsmith</username>
 *   <firstname>John</firstname>
 *   <surname>Smith</surname>
 *   <email>No Email</email>
 * </user>}</pre>
 * <p><i>(All elements are mandatory)</i></p>
 * 
 * <p>When the user is not logged in, this generator simply returns:
 * <pre>{@code <no-user/>}</pre>
 *
 * <h3>Usage</h3>
 * <p>To use this generator in Berlioz (in <code>/WEB-INF/config/services.xml</code>):
 * <pre>{@code <generator class="org.weborganic.bastille.security.GetUser" 
 *                         name="[name]" target="[target]"/>}</pre>
 *
 * @author Christophe Lauret (Weborganic)
 * @version 7 April 2011
 */
public final class GetUser implements ContentGenerator {

  /**
   * Retrieves the user from the session.
   * 
   * {@inheritDoc}
   */
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {
    HttpSession session = req.getSession();
    Object o = session.getAttribute(Constants.SESSION_USER_ATTRIBUTE);
    if (o instanceof User) {
      ((User)o).toXML(xml);
    } else {
      xml.emptyElement("no-user");
    }
  }

}
