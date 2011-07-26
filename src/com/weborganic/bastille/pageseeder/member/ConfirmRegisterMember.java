/**
 * 
 */
package com.weborganic.bastille.pageseeder.member;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentRequest;

import com.topologi.diffx.xml.XMLWriter;

import com.weborganic.bastille.pageseeder.PSConnector;
import com.weborganic.bastille.pageseeder.PSResourceType;

/**
 * TODO Christophe please help to review this generator.
 * 
 * <p>A generator for confirm PageSeeder register member to PageSeeder.</p>
 * 
 * <h3>Configuration</h3>
 * <p>There is no configuration associated with this generator.</p>
 * <p>The <code>PSResource<code> Object requires the following setting in <code>config/config-mode.xml </code>
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
 * <h3>Parameters</h3>
 * <ul>
 *  <li><code>ps-servlet</code> The request PageSeeder Servlet. (Default:com.pageseeder.ResetPassword)</li>
 *  <li><code>ps-method</code> The HTTP method to use to connect to PageSeeder, must be either POST or GET. (Default:GET)</li>
 *  <li><code>ps-groupName</code> The PageSeeder group name.</li>
 *  <li><code>ps-notify</code> It is always be "none" unless value is 'on'</li>
 *  <li><code>ps-details</code> Full detail and Partial detail. (Default:partial)</li>
 *  <li><code>ps-listed</code> Member email listed in the PageSeeder. (Default:No).</li>
 *  <li><code>ps-login</code> Login Pageseeder.(Default:Yes).</li>
 *  <li><code>ps-confirmGroup</code> Group confirm email.(Default:No)</li>
 *  <li><code>ps-email</code> The PageSeeder Email address.</li>
 *  <li><code>ps-password</code> The PageSeeder Password.</li>
 *  <li><code>ps-action</code> The PageSeeder Action type. (e.g. notifyList, sub, unsub, modify, save)</li>
 * </ul>
 * 
 * @author Christophe Lauret
 * @author Ciber Cai
 * @version beta (06 June 2011)
 */
public class ConfirmRegisterMember implements ContentGenerator {

  /**
   * Logger for debugging
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(ConfirmRegisterMember.class);

  /**
   * Default Member Register Servlet
   * */
  private static final String SERVLET = "com.pageseeder.SubscriptionForm";

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {
    String servlet = req.getParameter("ps-servlet", SERVLET);
    String method = req.getParameter("ps-method", "GET");
    String grouname = req.getParameter("ps-groupName", "");
    String notify = req.getParameter("ps-notify", "").equalsIgnoreCase("on") ? "Immediate" : "None";
    String details = req.getParameter("ps-details", "partial");
    String listed = req.getParameter("ps-listed", "No");
    String login = req.getParameter("ps-login", "Yes");
    String confirmGroup = req.getParameter("ps-confirmGroup", "No");


    //
    String email = req.getParameter("ps-email", "").toLowerCase();
    String password = req.getParameter("ps-password", "");
    String action = req.getParameter("ps-action", "").toLowerCase().equalsIgnoreCase("accept") ? "sub" : "";

    if (action.isEmpty() ||
        password.isEmpty() ||
        !isValidEmailAddress(email)) {
      LOGGER.debug("Invalid data");
      printErrorMsg(xml);
    }

    // Create the request
    PSConnector connector = new PSConnector(PSResourceType.SERVLET, servlet);
    // add configuration parameters
    connector.addParameter("groupName", grouname);
    connector.addParameter("notify", notify);
    connector.addParameter("action", action);
    connector.addParameter("details", details);
    connector.addParameter("confirmGroup", confirmGroup);
    connector.addParameter("login", login);
    connector.addParameter("listed", listed);
    connector.addParameter("details", details);
    connector.addParameter("username", email);
    connector.addParameter("password", password);
    connector.addParameter("action", action);


    // Get PSXML from PageSeeder
    if (method.equalsIgnoreCase("GET")) {
      connector.get(xml);
    } else if (method.equalsIgnoreCase("POST")) {
      connector.post(xml);
    } else {
      // default
      connector.get(xml);
    }

  }

  /***
   * Method for printing error message.
   * XML format
   * <pre>
   * {@code
   *  <title>Some Details Not Specified</title>
   *  <status>missing-parameter</status>
   * }
   * </pre>
   * @param xml
   * @throws IOException
   */
  private void printErrorMsg(XMLWriter xml) throws IOException {
    xml.openElement("title");
    xml.writeText("Some Details Not Specified");
    xml.closeElement();
    xml.openElement("status");
    xml.writeText("missing-parameter");
    xml.closeElement();
  }

  /***
   * method for validate email address.
   * 
   * @param email
   * @return
   */
  private static boolean isValidEmailAddress(String email) {
    if (email == null)
      return false;
    // Quick check
    if (email.indexOf('@') < 0)
      return false;
    // Thorough check
    String expression = "^[\\w\\-]([\\.\\w])+[\\w]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
    Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
    Matcher matcher = pattern.matcher(email);
    return matcher.matches();
  }

}
