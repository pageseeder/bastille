/*
 *  Copyright (c) 2011 Allette Systems pty. ltd.
 */
package com.weborganic.bastille.web.general;

import java.io.IOException;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentRequest;

import com.topologi.diffx.xml.XMLWriter;

import com.weborganic.bastille.pageseeder.PSConnector;
import com.weborganic.bastille.pageseeder.PSResourceType;
import com.weborganic.bastille.security.Constants;
import com.weborganic.bastille.security.ps.PageSeederUser;
import com.weborganic.bastille.web.general.utils.HttpServletUtils;


/**
 * <p>A generator for sending a request to PageSeeder in order to change personal details.</p>
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
 *  <li><code>ps-method</code> The HTTP method to use to connect to PageSeeder, must be either POST or GET. (Default:GET)</li>
 * 
 *  <li><code>ps-firstname</code> PageSeeder firstname.</li>
 *  <li><code>ps-lastname</code> PageSeeder surname.</li>
 *  <li><code>ps-email</code> PageSeeder email.</li>
 *  <li><code>ps-emailconfirm</code> PageSeeder confirm email.</li>
 *  <li><code>ps-password</code> PageSeeder password.</li>
 *  <li><code>ps-passwordconfirm</code> PageSeeder confirm password.</li>
 * </ul>
 * 
 * @author Ciber Cai
 * @version 03 August 2011
 */
public class GetMyDetails implements ContentGenerator{

  /**
   * Logger for debugging
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(GetMyDetails.class);

  private static final String SERVLET = "com.pageseeder.ChangeDetailsForm";


  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {
    HttpSession session = req.getSession();

    String psMethod = req.getParameter("ps-method", "GET");
    String method = HttpServletUtils.getMethod(req).toLowerCase();

    LOGGER.info("SERVLET  " + req.getParameter("ps-servlet", SERVLET));

    // Create the request
    PSConnector connector = new PSConnector(PSResourceType.SERVLET, SERVLET);

    Object o = session.getAttribute(Constants.SESSION_USER_ATTRIBUTE);
    if (o instanceof PageSeederUser) {
      PageSeederUser u = (PageSeederUser) o;
      LOGGER.debug("JSessionId {}", u.getJSessionId());
      connector.setUser(u);
    } else {
      xml.emptyElement("login-required");
      return;
    }

    // Grab the XML form the PageSeeder request
    if (psMethod.equalsIgnoreCase("GET") || method.equalsIgnoreCase("GET")) {
      LOGGER.debug("Retrieve user detail by menthod {} ", method);
      connector.get(xml);
    } else if (method.equalsIgnoreCase("POST")) {
      LOGGER.debug("Update user detail by menthod {} ", method);
      connector.addParameter("firstName", req.getParameter("ps-firstname",""));
      connector.addParameter("surname", req.getParameter("ps-lastname",""));
      connector.addParameter("email", req.getParameter("ps-email",""));
      connector.addParameter("emailConfirm", req.getParameter("ps-emailconfirm",""));
      connector.addParameter("password", req.getParameter("ps-password",""));
      connector.addParameter("passwordconfirm", req.getParameter("ps-passwordconfirm",""));
      connector.post(xml);
    } else {
      // default
      //connector.get(xml);
    }

  }


}
