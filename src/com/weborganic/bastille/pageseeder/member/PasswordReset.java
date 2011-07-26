/**
 * 
 */
package com.weborganic.bastille.pageseeder.member;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentRequest;
import org.weborganic.berlioz.servlet.HttpRequestWrapper;

import com.topologi.diffx.xml.XMLWriter;

import com.weborganic.bastille.pageseeder.PSConnector;
import com.weborganic.bastille.pageseeder.PSResourceType;

/**
 * TODO Christophe please help to review this generator.
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
 *     <entry key="host" value="remotephcmanuals.pageseeder.com" />
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
 *  <li><code>ps-method</code> The HTTP method to use to connect to PageSeeder, must be either POST or GET. (Default:POST)</li>
 *  <li><code>ps-*</code> Any parameter starts from "ps-" will also send to PageSeeder.
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
 * @version beta(02 June 2011)
 */
public class PasswordReset implements ContentGenerator {

  /**
   * Logger for debugging
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(PasswordReset.class);

  private static final String SERVLET = "com.pageseeder.ResetPassword";


  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {

    String servlet = req.getParameter("ps-servlet", SERVLET);
    String method = req.getParameter("ps-method", "POST");

    // Create the request
    PSConnector connector = new PSConnector(PSResourceType.SERVLET, servlet);

    // Add parameters
    if (req instanceof HttpRequestWrapper){
      HttpRequestWrapper rWrapper = (HttpRequestWrapper) req;
      HttpServletRequest hreq = rWrapper.getHttpRequest();

      Enumeration<String> names = hreq.getParameterNames();
      while (names.hasMoreElements()) {
        String name = names.nextElement();
        String value = req.getParameter(name, "");
        if (name.startsWith("ps-") && !value.isEmpty() && !name.equals("ps-method")) {
          LOGGER.debug("Add parameter name:{} value:{}", name.substring(3), value);
          connector.addParameter(name.substring(3), value);
        }
      }
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
