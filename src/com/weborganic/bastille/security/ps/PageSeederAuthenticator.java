package com.weborganic.bastille.security.ps;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.weborganic.bastille.security.Authenticator;
import com.weborganic.bastille.security.Constants;
import com.weborganic.bastille.security.User;

/**
 * An authenticator that uses PageSeeder to authenticate users.
 * 
 * @author Christophe Lauret
 * @version 7 April 2011
 */
public final class PageSeederAuthenticator implements Authenticator {

  /**
   * {@inheritDoc}
   */
  public boolean login(HttpServletRequest req) throws IOException {

    // Grab the username and password
    String username = req.getParameter("username");
    String password = req.getParameter("password");

    // Get the session
    HttpSession session = req.getSession();

    // Already logged in?
    boolean requires = true;
    if (session != null) {
      Object o = session.getAttribute(Constants.SESSION_USER_ATTRIBUTE);
      if (o instanceof PageSeederUser) {
        PageSeederUser current = (PageSeederUser)o;
        // Already logged in and it is the current user
        if (username != null && username.equals(current.getUsername())) {
          requires = false;
        } else {
          logout(current);
          session.invalidate();
          session = req.getSession(true);
        }
      }
    }

    User user = null;
    if (requires) {
      // Perform login
      user = login(username, password);
      session.setAttribute(Constants.SESSION_USER_ATTRIBUTE, user);
    }
    return user != null;
  }

  @Override
  public boolean logout(HttpServletRequest req) throws IOException {
    // Get the session
    HttpSession session = req.getSession();
    if (session != null) {
      User user = (User)session.getAttribute(Constants.SESSION_USER_ATTRIBUTE);
      if (user != null) {
        logout(user);
      }
    }

    session.invalidate();
    session = req.getSession(true);
    return false;
  }

  @Override
  public User login(String username, String password) throws IOException {

    // Set up the URL
    URL url = toLoginURL(username, password);
    User user = null;

    // Create the connection
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setDoOutput(true);
    connection.setInstanceFollowRedirects(true);
    connection.setRequestMethod("GET");
    connection.setDefaultUseCaches(false);
    connection.setRequestProperty("X-Requester", "Bastille");

    // Then connect
    connection.connect();

    // Retrieve the content of the response
    int status = connection.getResponseCode();

    if (status == 200) {
      InputStream is = connection.getInputStream();
      InputSource source = new InputSource(is);
      user = parse(source);
      is.close();

    } else if (status >= 400 && status < 500) {
      System.err.println("PageSeeder returned "+status+": "+connection.getResponseMessage());
    } else {
      throw new IOException("Unable to connect to Remote PageSeeder Server");
    }

    connection.disconnect();

    return user;
  }

  @Override
  public boolean logout(User user) throws IOException {

    return false;
  }

  private final URL toLoginURL(String username, String password) throws MalformedURLException {
    return new URL("http://localhost:8088/ps/servlet/com.pageseeder.ChangeDetailsForm?username="+username+"&password="+password+"&xformat=xml");
  }

  /**
   * 
   * @param is
   * @return
   */
  private static PageSeederUser parse(InputSource source) throws IOException {
    PSUserHandler handler = new PSUserHandler();
    SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.setValidating(false);
    factory.setNamespaceAware(false);
    
    try {
      SAXParser parser = factory.newSAXParser();
      parser.parse(source, handler);
    } catch (ParserConfigurationException ex) {
      ex.printStackTrace();
    } catch (SAXException ex) {
      ex.printStackTrace();
    }
    return handler.getUser();
  }

  // Inner class ==================================================================================

  /**
   * Parses the XML returned the ChangeDetailsForm Servlet.
   * 
   * <pre>{@code
   *   <wo-jsessionid>76AF8EDE185D6D0F34DFD53982BC0570</wo-jsessionid>
   *   <mem>
   *     <id>1</id>
   *     <surname>Smith</surname>
   *     <username>jsmith</username>
   *     <firstname>John</firstname>
   *     <bouncereceived>0</bouncereceived>
   *   </mem>
   *   <memberemail>jsmith@email.com</memberemail>
   * }</pre>
   * 
   * @author Christophe Lauret
   * @version 7 April 2011
   */
  private static class PSUserHandler extends DefaultHandler {
    
    private static final String MEMBER = "mem";
    private static final String ID = "id";
    private static final String SURNAME = "surname";
    private static final String USERNAME = "username";
    private static final String FIRSTNAME = "firstname";
    private static final String EMAIL = "memberemail";
    private static final String JSESSIONID = "wo-jsessionid";
    
    private boolean inMem = false;
    private boolean record = false;
    private StringBuffer buffer = new StringBuffer();
    private Map<String,String> map = new HashMap<String,String>();

    /**
     * {@inheritDoc}
     */
    public void startElement(String uri, String local, String name, Attributes attributes) throws SAXException {
      if (MEMBER.equals(name)) this.inMem = true;
      if (this.inMem) {
        this.record = ID.equals(name) || SURNAME.equals(name) || USERNAME.equals(name) || FIRSTNAME.equals(name);
      } else {
        this.record = EMAIL.equals(name) || JSESSIONID.equals(name);
      }
    }

    /**
     * {@inheritDoc}
     */
    public void endElement(String uri, String local, String name) throws SAXException {
      if (MEMBER.equals(name)) this.inMem = false;
      if (this.inMem) {
        if (ID.equals(name)) this.map.put(ID, this.buffer.toString());
        else if (SURNAME.equals(name)) this.map.put(SURNAME, this.buffer.toString());
        else if (USERNAME.equals(name)) this.map.put(USERNAME, this.buffer.toString());
        else if (FIRSTNAME.equals(name)) this.map.put(FIRSTNAME, this.buffer.toString());
        this.buffer.setLength(0);
      } else if (EMAIL.equals(name)) {
        this.map.put(EMAIL, this.buffer.toString());
        this.buffer.setLength(0);
      } else if (JSESSIONID.equals(name)) {
        this.map.put(JSESSIONID, this.buffer.toString());
        this.buffer.setLength(0);
      }
    }

    /**
     * {@inheritDoc}
     */
    public void characters(char[] ch, int start, int length) throws SAXException {
      if (this.record) this.buffer.append(ch, start, length);
    }

    /**
     * @return a new PageSeeder User from the values parsed.
     */
    private PageSeederUser getUser() {
      Long id = Long.parseLong(this.map.get(ID));
      PageSeederUser user = new PageSeederUser(id);
      user.setEmail(this.map.get(EMAIL));
      user.setUsername(this.map.get(USERNAME));
      user.setSurname(this.map.get(SURNAME));
      user.setFirstname(this.map.get(FIRSTNAME));
      user.setJSessionId(this.map.get(JSESSIONID));
      return user;
    }
  }

}
