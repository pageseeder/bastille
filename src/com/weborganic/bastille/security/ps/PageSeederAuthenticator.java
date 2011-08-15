/*
 * Copyright (c) 2011 weborganic systems pty. ltd.
 */
package com.weborganic.bastille.security.ps;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weborganic.berlioz.GlobalSettings;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.weborganic.bastille.security.AuthenticationResult;
import com.weborganic.bastille.security.Authenticator;
import com.weborganic.bastille.security.Constants;
import com.weborganic.bastille.security.User;

/**
 * An authenticator that uses PageSeeder to authenticate users.
 * 
 * @author Christophe Lauret
 * @version 0.6.11 - 15 August 2011
 * @since 0.6.2
 */
public final class PageSeederAuthenticator implements Authenticator {

  /** Logger for this class */
  private static final Logger LOGGER = LoggerFactory.getLogger(PageSeederAuthenticator.class);

  /**
   * The PageSeeder login requires a username and password and checks them against the members on
   * a PageSeeder Server.
   * 
   * {@inheritDoc}
   */
  public AuthenticationResult login(HttpServletRequest req) throws IOException {

    // Grab the username and password
    String username = req.getParameter("username");
    String password = req.getParameter("password");

    // Required details
    if (username == null || password == null) {
      return AuthenticationResult.INSUFFICIENT_DETAILS;
    }

    // Get the session
    HttpSession session = req.getSession();

    // Already logged in?
    if (session != null) {
      Object o = session.getAttribute(Constants.SESSION_USER_ATTRIBUTE);
      if (o instanceof PageSeederUser) {
        PageSeederUser current = (PageSeederUser)o;
        // Already logged in and it is the current user
        if (username.equals(current.getUsername())) {
          return AuthenticationResult.ALREADY_LOGGED_IN;

        // Already logged in as a different user
        } else {
          logout(current);
          session.invalidate();
          session = req.getSession(true);
        }
      }
    }

    // Perform login
    User user = login(username, password);
    if (user != null) {
      session.setAttribute(Constants.SESSION_USER_ATTRIBUTE, user);
      return AuthenticationResult.LOGGED_IN;
    } else {
      return AuthenticationResult.INCORRECT_DETAILS;
    }
  }

  /**
   * {@inheritDoc}
   */
  public AuthenticationResult logout(HttpServletRequest req) throws IOException {
    // Get the session
    HttpSession session = req.getSession();
    if (session != null) {
      User user = (User)session.getAttribute(Constants.SESSION_USER_ATTRIBUTE);
      if (user != null) {
        logout(user);
      }
      // Invalidate the session and create a new one
      session.invalidate();
      session = req.getSession(true);
      return AuthenticationResult.LOGGED_OUT;
    }

    // User was already logged out
    return AuthenticationResult.ALREADY_LOGGED_OUT;
  }

  /**
   * Logins the user using their username and password. 
   * 
   * @param username The username of the user to login
   * @param password The password of the user to login
   * @return The corresponding user or <code>null</code>
   * @throws IOException Should any I/O error occurs while connecting to the server.
   */
  public static PageSeederUser login(String username, String password) throws IOException {

    // Set up the URL
    URL url = toLoginURL(username, password);
    PageSeederUser user = null;

    // Create the connection
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setDoOutput(true);
    connection.setInstanceFollowRedirects(true);
    connection.setRequestMethod("GET");
    connection.setDefaultUseCaches(false);
    connection.setRequestProperty("X-Requester", "Bastille");

    // Then connect
    LOGGER.debug("Connecting to PageSeeder with {}", url);
    connection.connect();

    // Retrieve the content of the response
    int status = connection.getResponseCode();

    // If OK parse the response and get the user
    if (status == HttpURLConnection.HTTP_OK) {
      InputStream is = connection.getInputStream();
      InputSource source = new InputSource(is);
      user = parse(source);
      is.close();

    // If a client error occurs
    } else if (status >= HttpURLConnection.HTTP_BAD_REQUEST
            && status < HttpURLConnection.HTTP_INTERNAL_ERROR) {
      LOGGER.debug("PageSeeder returned {}: {}", status, connection.getResponseMessage());

    // If a server error occurs
    } else {
      LOGGER.warn("PageSeeder returned {}: {}", status, connection.getResponseMessage());
      throw new IOException("Unable to connect to Remote PageSeeder Server");
    }

    // Disconnect and return the user if there is one
    connection.disconnect();
    return user;
  }

  @Override
  public boolean logout(User user) throws IOException {

    return false;
  }

  // Private helpers
  // ----------------------------------------------------------------------------------------------

  /**
   * Returns the URL used to login to PageSeeder.
   *
   * @param username the PageSeeder username to login with.
   * @param password the corresponding PageSeeder password.
   * 
   * @return the corresponding URL
   * 
   * @throws MalformedURLException If the
   */
  private static URL toLoginURL(String username, String password) throws MalformedURLException {
    Properties pageseeder = GlobalSettings.getNode("bastille.pageseeder");
    StringBuffer url = new StringBuffer();
    url.append(pageseeder.getProperty("scheme",        "http")).append("://");
    url.append(pageseeder.getProperty("host",          "localhost")).append(":");
    url.append(pageseeder.getProperty("port",          "8080"));
    url.append(pageseeder.getProperty("servletprefix", "/ps/servlet"));
    url.append("/com.pageseeder.ChangeDetailsForm");
    url.append("?username=").append(username);
    url.append("&password=").append(password);
    url.append("&xformat=xml");
    return new URL(url.toString());
  }

  /**
   * Parse the XML returned by PageSeeder and return the corresponding user.
   * 
   * @param source the XML returned by the login servlet.
   * @return the corresponding PageSeeder user.
   * 
   * @throws IOException If thrown by the parser.
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

    /** Member element */
    private static final String MEMBER = "mem";
    /** Member's ID element */
    private static final String ID = "id";
    /** Member's surname element */
    private static final String SURNAME = "surname";
    /** Member's username element */
    private static final String USERNAME = "username";
    /** Member's first name element */
    private static final String FIRSTNAME = "firstname";
    /** Member's email element */
    private static final String EMAIL = "memberemail";
    /** Member's JSession ID element */
    private static final String JSESSIONID = "wo-jsessionid";

    /** State variable to indicate whether we are within the Member element */
    private boolean inMem = false;
    /** State variable to indicate whether to record character data in the buffer */
    private boolean record = false;
    /** A buffer for character data */
    private StringBuffer buffer = new StringBuffer();
    /** Stores the member's information */
    private Map<String, String> map = new HashMap<String, String>();

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
