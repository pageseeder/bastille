package com.weborganic.bastille.pageseeder;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * @version 13 April 2011
 */
public final class PSConnector {

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
  private final PSResource.Builder _resource;

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
    this._resource = new PSResource.Builder(type, resource);
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
    this._resource.addParameter(name, value);
  }

  /**
   * Connect to PageSeeder.
   * 
   * @param xml the XML to copy from PageSeeder
   * 
   * @throws IOException If an error occurs when trying to write the XML.
   * 
   * @return <code>true</code> if the request was processed without errors;
   *         <code>false</code> otherwise.
   */
  public boolean get(XMLWriter xml) throws IOException {
    return get(xml, null, null);
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
    return get(xml, handler, null);
  }

  /**
   * Connect to PageSeeder and fetch the XML using the GET method. 
   * 
   * <p>Templates can be specified to transform the XML. 
   * 
   * @param xml       The XML to copy from PageSeeder
   * @param templates A set of templates to process the XML (optional)
   * 
   * @throws IOException If an error occurs when trying to write the XML.
   * 
   * @return <code>true</code> if the request was processed without errors;
   *         <code>false</code> otherwise.
   */
  public boolean get(XMLWriter xml, Templates templates) throws IOException {
    return get(xml, null, templates);
  }

  /**
   * Connect to PageSeeder and fetch the XML using the GET method. 
   * 
   * <p>If the handler is not specified, the xml writer receives a copy of the PageSeeder XML.
   * 
   * <p>If templates are specified they take precedence over the handler.
   * 
   * @param xml       the XML to copy from PageSeeder
   * @param handler   the handler for the XML (can be used to rewrite the XML)
   * @param templates A set of templates to process the XML (optional)
   * 
   * @throws IOException If an error occurs when trying to write the XML.
   * 
   * @return <code>true</code> if the request was processed without errors;
   *         <code>false</code> otherwise.
   */
  private boolean get(XMLWriter xml, PSHandler handler, Templates templates) throws IOException {

    // Build the resource
    PSResource r = this._resource.build();

    // Let's start
    xml.openElement("ps-"+r.type().toString().toLowerCase(), true);
    xml.attribute("resource", r.name());

    boolean ok = true;

    // Get the URL
    URL url = null;
    try {
      url = r.toURL(this._user);
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
          if (templates != null) {
            ok = parseXML(connection, xml, templates);
          } else {
            ok = parseXML(connection, xml, handler);
          }

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
   * Parse the Response as XML.
   * 
   * @param connection The HTTP URL connection.
   * @param xml        Where the final XML goes.
   * @param templates  To transform the XML.
   * 
   * @return <code>true</code> if the data was parsed without error;
   *         <code>false</code> otherwise.
   * 
   * @throws IOException If an error occurs while writing the XML.
   */
  private static boolean parseXML(HttpURLConnection connection, XMLWriter xml, Templates templates) throws IOException {
    boolean ok = true;

    // Create an XML Buffer
    StringWriter buffer = new StringWriter();

    InputStream in = null;
    try {
      in = connection.getInputStream();

      // Setup the source
      StreamSource source = new StreamSource(in);
      source.setSystemId(connection.getURL().toString());

      // Setup the result
      StreamResult result = new StreamResult(buffer);

      // Create a transformer from the templates
      Transformer transformer = templates.newTransformer();

      // Process, write directly to the result
      transformer.transform(source, result);

    } catch (TransformerException ex) {
      LOGGER.warn("Error while transforming XML data from URL", ex);
      error(xml, "transform-error", ex.getLocalizedMessage());
      ok = false;

    } catch (IOException ex) {
      LOGGER.warn("Error while parsing XML data from URL", ex);
      error(xml, "io-error", ex.getLocalizedMessage());
      ok = false;

    } finally {
      IOUtils.closeQuietly(in);
    }

    // Write as XML
    xml.writeXML(buffer.toString());

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
}
