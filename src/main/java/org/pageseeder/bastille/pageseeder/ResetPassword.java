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
package org.pageseeder.bastille.pageseeder;

import java.io.IOException;

import org.pageseeder.berlioz.BerliozException;
import org.pageseeder.berlioz.Beta;
import org.pageseeder.berlioz.content.ContentGenerator;
import org.pageseeder.berlioz.content.ContentRequest;
import org.pageseeder.berlioz.content.ContentStatus;
import org.pageseeder.xmlwriter.XMLWriter;

/**
 * Invokes PageSeeder to reset the password for the user.
 *
 * <h3>Configuration</h3>
 * <p>There is no configuration associated with this generator.</p>
 * <p>However, the <code>PSResource<code> Object requires the following setting in <code>config/config-mode.xml </code>
 *
 * <pre>
 * {@code
 * <node name="pageseeder">
 *   <map>
 *     <entry key="scheme" value="http" />
 *     <entry key="host" value="www.pageseeder.com" />
 *     <entry key="port" value="80" />
 *     <entry key="siteprefix" value="/ps" />
 *     <entry key="servletprefix" value="/ps/servlet" />
 *   </map>
 * </node>
 * }
 * </pre>
 *
 * <h3>Parameters</h3>
 * <ul>
 *  <li><code>email</code> The Reset EMail Address.
 *  <li><code>group</code> The group the user is a member of, use this to use a specific email
 *  template (optional).
 * </ul>
 *
 * <h3>Returned XML</h3>
 * <pre>
 * {@code
 * <ps-servlet resource="com.pageseeder.ResetPassword" http-status="200" content-type="application/xml">
 * <root>
 * ...
 * </root>
 * </ps-servlet>
 * }
 * </pre>
 *
 * @author Christophe Lauret
 * @version 16 September 2011
 *
 * @since 0.6.14
 */
@Beta
public final class ResetPassword implements ContentGenerator {

  /**
   * The PageSeeder Servlet to reset the password.
   */
  private static final String SERVLET = "com.pageseeder.ResetPassword";

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {

    // Grab parameters
    String email = req.getParameter("email");
    if (email == null) { missing(req, xml, "email"); return; }

    // Reset the password on PageSeeder
    PSConnector connector = new PSConnector(PSResourceType.SERVLET, SERVLET);
    connector.addParameter("email", email);
    String group = req.getParameter("group");
    if (group != null) {
      connector.addParameter("group", group);
    }
    connector.post(xml);
  }

  /**
   * Write response for missing required parameter.
   *
   * @param req  The content request
   * @param xml  The XML writer
   * @param name The name of the missing parameter.
   *
   * @throws IOException If thrown by XML writer
   */
  private static void missing(ContentRequest req, XMLWriter xml, String name) throws IOException {
    xml.openElement("error");
    xml.attribute("type", "client");
    xml.attribute("message", "The parameter '"+name+"' was not specified.");
    xml.closeElement();
    req.setStatus(ContentStatus.BAD_REQUEST);
  }
}
