package com.weborganic.bastille.pageseeder;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weborganic.berlioz.GlobalSettings;
import org.weborganic.berlioz.xml.XMLCopy;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.topologi.diffx.xml.XMLWriter;
import com.topologi.diffx.xml.XMLWriterImpl;
import com.weborganic.bastille.security.ps.PageSeederUser;

/**
 * Represents a request made to the PageSeeder Server.
 * 
 * <p>By default the request is made anonymously. In order to make a request on behalf of a 
 * PageSeeder user, use the {@link #setUser(PageSeederUser)} method - this is required for any page
 * that needs login access.
 * 
 * @author Christophe Lauret
 * @version 11 April 2011
 */
final class PSConnector {

  /** Logger for this class */
  private static final Logger LOGGER = LoggerFactory.getLogger(PSConnector.class);

  /** Bastille version */
  private static final String BASTILLE_VERSION;
  static {
    Package p = Package.getPackage("org.weborganic.berlioz");
    BASTILLE_VERSION = p != null ? p.getImplementationVersion() : "unknown";
  }

  /**
   * The type of resource accessed.
   */
  private final PSResourceType _type;

  /**
   * The resource to access.
   */
  private final String _resource;

  /**
   * The parameters to send.
   */
  private final Map<String, String> _parameters = new HashMap<String, String>();

  /**
   * If specified, the request will be made on behalf of that user.
   */
  private PageSeederUser _user = null;

  /**
   * Creates a new connection to the specified resource.
   * 
   * @param type     The type of resource.
   * @param resource The 
   */
  public PSConnector(PSResourceType type, String resource) {
    this._type = type;
    this._resource = resource;
  }

  /**
   * Sets the user for this request.
   * @param user the user for this request.
   */
  public void setUser(PageSeederUser user) {
    this._user = user;
  }

  /**
   * Add a parameter to this request.
   * 
   * @param name  The name of the parameter
   * @param value The value of the parameter
   */
  public void addParameter(String name, String value) {
    this._parameters.put(name, value);
  }

  /**
   * Returns the URL for this connection.
   * 
   * @return the URL for this connection.
   * 
   * @throws MalformedURLException If the URL is not well-formed
   */
  public URL toURL() throws MalformedURLException {
    Properties pageseeder = GlobalSettings.getNode("bastille.pageseeder");
    // Start building the URL
    StringBuffer url = new StringBuffer();
    url.append(pageseeder.getProperty("scheme", "http")).append("://");
    url.append(pageseeder.getProperty("host",   "localhost")).append(":");
    url.append(pageseeder.getProperty("port",   "8080"));

    // Decompose the resource (in case it contains a query or fragment part)
    String path  = getURLPath(this._resource);
    String query = getURLQuery(this._resource);
    String frag  = getURLFragment(this._resource);

    // Servlets
    if (this._type == PSResourceType.SERVLET) {
      url.append(pageseeder.getProperty("servletprefix", "/ps/servlet")).append('/');
      url.append(path);

    // Services
    } else if (this._type == PSResourceType.SERVICE) {
      url.append(pageseeder.getProperty("siteprefix", "/ps"));
      url.append("/service");
      url.append(path);

    // Any other resource
    } else {
      url.append(path);
    }
    // If the session ID is available
    if (this._user != null && this._user.getJSessionId() != null) {
      url.append(";jsessionid=").append(this._user.getJSessionId());
    }
    // Query Part
    if (query != null) {
      url.append(query);
      url.append("&xformat=xml");
    } else {
      url.append("?xformat=xml");
    }
    try {
      for (Entry<String, String> p : this._parameters.entrySet()) {
        URLEncoder.encode(p.getKey(), "utf-8");
        url.append("&").append(URLEncoder.encode(p.getKey(), "utf-8"));
        url.append("=").append(URLEncoder.encode(p.getValue(), "utf-8"));
      }
    } catch (UnsupportedEncodingException ex) {
      // Should never happen as UTF-8 is supported
      ex.printStackTrace();
    }
    // Fragment if any
    if (frag != null)
      url.append(frag);
    return new URL(url.toString());
  }

  /**
   * Connect to PageSeeder.
   * 
   * @param xml the XML to copy from PageSeeder
   * 
   * @throws IOException ???
   * 
   * @return <code>true</code> if the request was processed without errors;
   *         <code>false</code> otherwise.
   */
  public boolean get(XMLWriter xml) throws IOException {
    return get(xml, null);
  }

  /**
   * Connect to PageSeeder and fetch the XML using the GET method. 
   * 
   * <p>If the handler is not specified, the xml writer receives a copy of the PageSeeder XML.
   * 
   * @param xml     the XML to copy from PageSeeder
   * @param handler the handler for the XML (can be used to rewrite the XML) 
   * 
   * @throws IOException If an error occurs when trying to write the XML.
   * 
   * @return <code>true</code> if the request was processed without errors;
   *         <code>false</code> otherwise.
   */
  public boolean get(XMLWriter xml, PSHandler handler) throws IOException {

    // Let's start
    xml.openElement("ps-"+this._type.toString().toLowerCase(), true);
    xml.attribute("resource", this._resource);

    boolean ok = true;

    // Get the URL
    URL url = null;
    try {
      url = toURL();
      LOGGER.debug("PageSeeder URL: {}", url.toString());
    } catch (MalformedURLException ex) {
      LOGGER.warn("Malformed URL: {}", ex.getMessage());
      error(xml, "malformed-url", ex.getLocalizedMessage());
      xml.closeElement();
      return false;
    }

    // TODO handle POST and other methods

    // Create the connection
    HttpURLConnection connection = null;
    try {
      connection = (HttpURLConnection) url.openConnection();
      connection.setDoOutput(true);
      connection.setInstanceFollowRedirects(true);
      connection.setRequestMethod("GET");
      connection.setDefaultUseCaches(false);
      connection.setRequestProperty("X-Requester", "Bastille-"+BASTILLE_VERSION);

      // Retrieve the content of the response
      int status = connection.getResponseCode();
      xml.attribute("http-status", status);

      if (status == HttpURLConnection.HTTP_OK) {

        String contentType = connection.getContentType();

        // Strip ";charset" declaration if any
        if (contentType != null && contentType.indexOf(";charset=") > 0)
          contentType = contentType.substring(0, contentType.indexOf(";charset="));

        // Return content is XML try to parse it
        if (isXML(contentType)) {
          xml.attribute("content-type", "application/xml");
          ok = parseXML(connection, xml, handler);

        // Text content
        } else if (contentType != null && contentType.startsWith("text/")) {
          xml.attribute("content-type", contentType);
          ok = parseText(connection, xml);

        // Probably binary
        } else {
          xml.attribute("content-type", contentType);
          xml.openElement("binary");
          xml.closeElement();
        }

      } else {
        LOGGER.info("PageSeeder returned {}: {}", status, connection.getResponseMessage());
        error(xml, "http-error", connection.getResponseMessage());
        ok = false;
      }

    } finally {
      // Disconnect
      if (connection != null) connection.disconnect();
      xml.closeElement();
    }

    // Return the final status
    return ok;
  }

  /**
   * Indicates if the content type corresponds to XML content.
   * 
   * @param contentType The content type
   * @return <code>true</code> if equal to "text/xml" or "application/xml" or end with "+xml";
   *         <code>false</code> otherwise. 
   */
  private static boolean isXML(String contentType) {
    return "text/xml".equals(contentType)
        || "application/xml".equals(contentType)
        || contentType.endsWith("+xml");
  }

  // Parsers ======================================================================================

  /**
   * Parse the Response as XML.
   * 
   * @param connection The HTTP URL connection.
   * @param xml        Where the final XML goes.
   * @param handler    To transform the XML (optional).
   * 
   * @return <code>true</code> if the data was parsed without error;
   *         <code>false</code> otherwise.
   * 
   * @throws IOException If an error occurs while writing the XML.
   */
  private static boolean parseXML(HttpURLConnection connection, XMLWriter xml, PSHandler handler) throws IOException {
    boolean ok = true;

    // Create an XML Buffer
    StringWriter w = new StringWriter();
    XMLWriter buffer = new XMLWriterImpl(w);

    // Parse with the XML Copy Handler
    DefaultHandler h = null; 
    if (handler != null) {
      handler.setXMLWriter(xml);
      h = handler;

    // Parse with the XML Copy Handler
    } else {
      h = new XMLCopy(buffer);
    }

    SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.setValidating(false);
    factory.setNamespaceAware(false);

    InputStream in = null;
    try {
      // Get the source as input stream
      in = connection.getInputStream();
      InputSource source = new InputSource(in);

      // Ensure the encoding is correct
      String encoding = connection.getContentEncoding();
      if (encoding != null) source.setEncoding(encoding);

      // And parse!
      SAXParser parser = factory.newSAXParser();
      parser.parse(source, h);

    } catch (IOException ex) {
      LOGGER.warn("Error while parsing XML data from URL", ex);
      error(xml, "io-error", ex.getLocalizedMessage());
      ok = false;

    } catch (ParserConfigurationException ex) {
      LOGGER.warn("Error while configuring parser for PageSeeder data", ex);
      error(xml, "sax-config-error", ex.getLocalizedMessage());
      ok = false;

    } catch (SAXException ex) {
      LOGGER.info("Error parsing XML response!", ex);
      error(xml, "sax-parse-error", ex.getLocalizedMessage());
      ok = false;

    } finally {
      IOUtils.closeQuietly(in);
    }

    // Write as XML
    buffer.flush();
    xml.writeXML(w.toString());

    return ok;
  }

  /**
   * Parse the response as text.
   * 
   * @param connection The HTTP URL connection.
   * @param xml        Where the final XML goes.
   * 
   * @return <code>true</code> if the data was parsed without error;
   *         <code>false</code> otherwise.
   * 
   * @throws IOException If an error occurs while writing the XML.
   */
  private static boolean parseText(HttpURLConnection connection, XMLWriter xml) throws IOException {
    // Get the source as input stream
    InputStream in = null;
    StringWriter buffer = new StringWriter();
    boolean ok = true;

    try {
      in = connection.getInputStream();
      String encoding = connection.getContentEncoding();
      IOUtils.copy(in, buffer, encoding);
    } catch (IOException ex) {
      LOGGER.warn("Error while parsing text data from URL", ex);
      error(xml, "io-error", ex.getLocalizedMessage());
      ok = false;
    } finally {
      IOUtils.closeQuietly(in);
    }

    // Write as CDATA section
    xml.writeXML("<![CDATA[");
    xml.writeXML(buffer.toString());
    xml.writeXML("]]>");

    return ok;
  }

  /**
   * Adds the attributes for when error occurs
   * 
   * @param xml     The XML output.
   * @param error   The error code.
   * @param message The error message. 
   * 
   * @throws IOException If thrown while writing the XML.
   */
  private static void error(XMLWriter xml, String error, String message) throws IOException {
    xml.attribute("error", error);
    xml.attribute("message", message);
  }

  /**
   * Returns the fragment part of the URL.
   * 
   * @param resource the path to the resource 
   * @return the part before any '#' or '?'. 
   */
  private static String getURLPath(String resource) {
    int h = resource.lastIndexOf('#');
    String r = h > 0? resource.substring(0, h) : resource;
    int q = r.indexOf('?');
    if (q > 0) return r.substring(0, q);
    else return r;
  }

  /**
   * Returns the query part of the URL.
   * 
   * @param resource the path to the resource 
   * @return the part after and including '?' if it exists; otherwise <code>null</code> 
   */
  private static String getURLQuery(String resource) {
    int q = resource.indexOf('?');
    int h = resource.lastIndexOf('#');
    if (q < 0 || (h > 0 && h < q)) return null;
    if (h > q) return resource.substring(q, h);
    else return resource.substring(q);
  }

  /**
   * Returns the fragment part of the URL.
   * 
   * @param resource the path to the resource 
   * @return the part after and including '#' if it exists; otherwise <code>null</code> 
   */
  private static String getURLFragment(String resource) {
    int h = resource.indexOf('#');
    return h >= 0 ? resource.substring(h) : null;
  }
}
