/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.weborganic.bastille.pageseeder;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.http.HttpSession;

import org.weborganic.bastille.security.Constants;
import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentRequest;

import com.topologi.diffx.xml.XMLWriter;

/**
 * A generator than can connect to PageSeeder and call a servlet.
 *
 * <h3>Configuration</h3>
 * <p>There is no configuration directly required with this generator; however since this generator
 * connects to PageSeeder the <code>bastille.pageseeder</code> properties must setup in order
 * to defined which server to connect to.</p>
 *
 * <h3>Parameters</h3>
 * <p>The following parameter is required:</p>
 * <table>
 *   <tbody>
 *   <tr><th>ps-servlet</th><td>The name of the servlet to connect to (required)</td></tr>
 *   </tbody>
 * </table>
 *
 * <p>The following parameters can also be specified:</p>
 * <table>
 *   <tbody>
 *   <tr><th>ps-method</th><td>The HTTP method to use to connect to PageSeeder, must be either
 *   <code>GET</code>|</code>POST</code>; <code>GET</code> is the default is this parameter is not
 *   specified
 *   </td></tr>
 *   </tbody>
 * </table>
 * <p>Any other parameter will automatically be transmitted to the PageSeeder servlet.
 *
 * <h3>Returned XML</h3>
 * <p>TODO</p>
 *
 * <h4>Error handling</h4>
 * <p>If an error occurs while invoking the servlet, the XML will also include the
 * <code>error</code> and <code>message</code> attributes. The HTTP status should
 * correspond to an HTTP error code.
 * <pre>{@code <ps-servlet resource="/members/[member id]/projects"
 *         http-status="[error]"
 *        content-type="application/xml"
 *               error="[error-type]"
 *             message="[error-message]">
 * </ps-servlet>}</pre>
 *
 * <h3>Permission</h3>
 * <p>This generator will attempt to use the user currently logged in.
 * <p>If the current user is not PageSeeder user or if there is no user currently logged in, the
 * request will be made anonymously.
 *
 * <h3>Usage</h3>
 * <p>This is a generic generator; use this generator when no other specialised generator provides
 * the same functionality.
 *
 * @author Christophe Lauret
 * @version 0.6.7 - 3 June 2011
 * @since 0.6.3
 */
@PSConnected
public final class CallServlet implements ContentGenerator {

  /**
   * {@inheritDoc}
   */
  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {

    // Determine what kind of request to make
    String servlet = req.getParameter("ps-servlet");
    String method = req.getParameter("ps-method", "GET");

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
    if (user instanceof PSUser) {
      connector.setUser((PSUser)user);
    }

    // Grab the XML form the PageSeeder request
    if ("GET".equalsIgnoreCase(method)) {
      connector.get(xml);
    } else if ("POST".equalsIgnoreCase(method)) {
      connector.post(xml);
    } else {
      // default
      connector.get(xml);
    }

  }

}
