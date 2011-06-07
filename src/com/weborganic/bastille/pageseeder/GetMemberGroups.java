/*
 * Copyright (c) 2011 weborganic systems pty. ltd.
 */
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
 * <h3>Configuration</h3>
 * <p>There is no configuration associated with this generator.</p>
 * 
 * <h3>Parameters</h3>
 * <p>No parameter required, however, the user does need to be logged in as a PageSeeder User.
 * 
 * <h3>Returned XML</h3>
 * <p>This generator invokes a PageSeeder service and returns the content of this service verbatim.
 * <p>The content is wrapped in:
 * <pre>{@code <ps-service resource="/members/[member id]/projects" 
 *         http-status="[status code]"
 *        content-type="application/xml">
 *   <!-- XML returned by PageSeeder Service -->
 * </ps-service>}</pre>
 * 
 * <p>The PageSeeder service returns the projects and group in hierarchical form.
 * <pre>{@code <projects for="[user email]">
 *   <!-- for each project -->
 *   <project id="[project id]" name="[name]"
 *   description="[description]"
 *         owner="[owner]" owner-directory="[owner dir]">
 *     <!-- for each group/sub-project -->
 *     <group id="[group id]" name="[name]"
 *   description="[description]"
 *        owner="[owner]" owner-directory="[owner dir]"/>
 *     <!-- more groups / sub-project ... -->
 *   </project>
 *   <!-- more projects ... -->
 * </projects>}</pre>
 *
 * <h4>Error handling</h4>
 * <p>If an error occurs while invoking the service, the XML will also include the 
 * <code>error</code> and <code>message</code> attributes. The HTTP status should 
 * correspond to an HTTP error code.
 * <pre>{@code <ps-service resource="/members/[member id]/projects" 
 *         http-status="[error]"
 *        content-type="application/xml"
 *               error="[error-type]"
 *             message="[error-message]">
 * </ps-service>}</pre>
 *
 * <h4>Sample output</h4>
 * <pre>{@code <content generator="com.weborganic.bastille.pageseeder.GetMemberGroups"
 *              name="classes" target="main" status="ok">
 *   <ps-service resource="/members/123/projects" http-status="200" 
 *           content-type="application/xml">
 *     <projects for="No Email">
 *       <project id="5" name="Project X" 
 *             owner="XYZ" owner-directory="project_x"
 *       description="This is Project X">
 *         <project id="6" name="project_x-2011"
 *         description="Project X in 2011 (Sub-project)"
 *               owner="XYZ" owner-directory="project_x-2011">
 *           <group id="7" name="project_x-2011-dev" 
 *         description="Dev group on project X in 2011"
 *               owner="XYZ" owner-directory="project_x-2011"/>
 *         </project>
 *       </project>
 *     </projects>
 *   </ps-service>
 * </content>}</pre>
 * 
 * <h3>Usage</h3>
 * <p>To use this generator in Berlioz (in <code>/WEB-INF/config/services.xml</code>):
 * <pre>{@code <generator class="com.weborganic.bastille.pageseeder.GetMemberGroups" 
 *            name="[name]" target="[target]"/>}</pre>
 * 
 * @author Christophe Lauret
 * @version 0.6.2 - 11 April 2011
 * @since 0.6.2
 */
@PSConnected(login = true)
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
      PSConnector tunnel = new PSConnector(PSResourceType.SERVICE, url);
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
