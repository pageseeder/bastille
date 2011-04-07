package com.weborganic.bastille.security;

import java.io.IOException;

import javax.servlet.http.HttpSession;

import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentRequest;

import com.topologi.diffx.xml.XMLWriter;

/**
 * Returns the User currently logged in.
 * 
 * <p>The user implementation depends on the authentication mechanism.
 * 
 * <p>Only returns the user data if the user is logged in, otherwise returns:
 * <code>{@code <no-user/>}</code>
 * 
 * <p>Sample Berlioz config:
 * <pre>
 * {@code
 *   <generator class="org.weborganic.bastille.security.GetUser" name="user" target="header">
 *     <parameter name="path" source="uri" value="path"/>
 *   </generator>
 * }
 * </pre>
 * 
 * @see User
 * 
 * @author Christophe Lauret
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
