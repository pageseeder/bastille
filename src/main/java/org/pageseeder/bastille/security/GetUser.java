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

import org.pageseeder.berlioz.BerliozException;
import org.pageseeder.berlioz.content.ContentGenerator;
import org.pageseeder.berlioz.content.ContentRequest;
import org.pageseeder.xmlwriter.XMLWriter;

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
 * <pre>{@code <generator class="org.pageseeder.bastille.security.GetUser"
 *                         name="[name]" target="[target]"/>}</pre>
 *
 * @author Christophe Lauret (Weborganic)
 * @version 0.6.27 - 29 November 2011
 * @since 0.6.2
 */
public class GetUser implements ContentGenerator {

  /**
   * Retrieves the user from the session.
   *
   * {@inheritDoc}
   */
  @Override
  public final void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {
    User user = getUser(req);
    if (user != null) {
      user.toXML(xml);
    } else {
      xml.emptyElement("no-user");
    }
  }

  /**
   * Returns the user stored in the session.
   *
   * @param req the content request.
   * @return the user if any or <code>null</code>.
   */
  protected static User getUser(ContentRequest req) {
    HttpSession session = req.getSession();
    if (session == null) return null;
    Object o = session.getAttribute(Constants.SESSION_USER_ATTRIBUTE);
    if (o instanceof User) return (User)o;
    // No match or not a user
    return null;
  }

}
