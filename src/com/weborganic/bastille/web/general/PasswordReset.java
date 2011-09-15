package com.weborganic.bastille.web.general;

/**
 * 
 */

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentRequest;

import com.topologi.diffx.xml.XMLWriter;

import com.weborganic.bastille.pageseeder.PSConnector;
import com.weborganic.bastille.pageseeder.PSResourceType;
import com.weborganic.bastille.web.general.utils.HttpServletUtils;


/**
 * 
 * <p>A generator for sending a request to PageSeeder in order to reset the PageSeeder member password.</p>
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
 *  <li><code>ps-servlet</code> The request PageSeeder Servlet. (Default:com.pageseeder.ResetPassword)</li>
 *  <li><code>ps-email</code> The Reset EMail Address.
 * </ul>
 * 
 * <h3>Returned XML</h3>
 * <pre>
 * {@code
 * <node name="pageseeder">
 * <ps-servlet resource="com.pageseeder.ResetPassword" http-status="200" content-type="application/xml">
 * <root>
 * ...
 * </root>
 * </ps-servlet>
 * }
 * </pre>
 * 
 * @author Christophe Lauret
 * @author Ciber Cai
 * @version beta(14 September 2011)
 * 
 */
public class PasswordReset implements ContentGenerator {

  /**
   * Logger for debugging
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(PasswordReset.class);

  private static final String SERVLET = "com.pageseeder.ResetPassword";


  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {
    String method = HttpServletUtils.getMethod(req).toLowerCase();

    LOGGER.info("Method " + method);
    LOGGER.info("SERVLET  " + req.getParameter("ps-servlet", SERVLET));

    if (method.equalsIgnoreCase("POST") || req.getParameter("ps-method","").equalsIgnoreCase("POST")){
      doPost(req, xml);
    } else {
      doGet(req, xml);
    }
  }


  /***
   * Method for HTTP GET.
   * 
   * @param req
   * @param xml
   * @throws IOException
   */
  private void doGet(ContentRequest req, XMLWriter xml) throws IOException{
    LOGGER.info("Get Method Return");

    xml.openElement("http");
    xml.attribute("method", HttpServletUtils.getMethod(req).toLowerCase());
    xml.attribute("status", "ok");
    xml.closeElement();
  }


  /***
   * Method for HTTP POST.
   * 
   * @param req
   * @param xml
   * @throws IOException
   */
  private void doPost(ContentRequest req, XMLWriter xml) throws IOException{
    LOGGER.info("POST Method Return");

    String servlet = req.getParameter("ps-servlet", SERVLET);
    String email = req.getParameter("ps-email", "");

    // Create the request
    PSConnector connector = new PSConnector(PSResourceType.SERVLET, servlet);

    // Add parameters
    if (!email.isEmpty()){
      connector.addParameter("email", email);
    }

    connector.post(xml);
  }
}
