package com.weborganic.bastille.pageseeder;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;

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
 * Wraps an HTTP connection to PageSeeder.
 * 
 * @author Christophe Lauret
 * @version 30 May 2011
 */
public final class PSConnection {

  /** Logger for this class */
  private static final Logger LOGGER = LoggerFactory.getLogger(PSConnection.class);

  /**
   * The type of connection.
   */
  public enum Type {

    /** A simple GET request. */
    GET,

    /** A simple POST request. */
    POST,

    /** A Form-Multipart request via POST. */
    MULTIPART

  };

  /** Bastille version */
  private static final String BASTILLE_VERSION;
  static {
    Package p = Package.getPackage("org.weborganic.berlioz");
    BASTILLE_VERSION = p != null ? p.getImplementationVersion() : "unknown";
  }

  /**
   * The boundary String, used when uploading as multipart
   */
  private static final String BOUNDARY = "-----------7e5619ace89c1";

  /**
   * The boundary String, used when uploading as multipart
   */
  private static final String UTF8 = "utf-8";

  /**
   * The wrapped HTTP Connection.
   */
  private final HttpURLConnection _connection;

  /**
   * The PageSeeder resource corresponding to the target of the URL.
   */
  private final PSResource _resource;

  /**
   * The type of connection.
   */
  private final Type _type;

  /**
   * The output stream used to write the data.
   */
  private DataOutputStream out = null;

  /**
   * Can only be created by the factory method.
   *
   * @param connection The wrapped HTTP Connection.
   * @param resource   The underlying resource to access.
   * @param type       The type of connection.
   */
  private PSConnection(HttpURLConnection connection, PSResource resource, Type type) {
    this._connection = connection;
    this._resource = resource;
    this._type = type;
  }

  /**
   * Add a part to the request (write the contents directly to the stream).
   * 
   * @param part The encoding to specify in the Part's header
   * @throws IOException 
   */
  public void addXMLPart(String part) throws IOException {
    addXMLPart(part, null);
  }

  /**
   * Add a part to the request (write the contents directly to the stream).
   * 
   * @param part     The encoding to specify in the Part's header
   * @param headers A list of headers added to this XML Part ('content-type' header is ignored)
   */
  public void addXMLPart(String part, Map<String, String> headers) throws IOException {
    if (this.out == null) {
      this.out = new DataOutputStream(this._connection.getOutputStream());
    }
    try {
      // Start with boundary
      IOUtils.write(BOUNDARY+"\r\n", this.out, UTF8);
      // Headers if specified
      if (headers != null) {
        for (Entry<String, String> h : headers.entrySet()) {
          String name = h.getKey();
          if (!"content-type".equalsIgnoreCase(name)) {
            IOUtils.write(name + ": " + headers.get(h.getValue()) + "\r\n", this.out, UTF8);
          }
        }
      }
      // Write content type
      IOUtils.write("Content-Type: text/xml; charset=\"utf-8\"\r\n\r\n", this.out, UTF8);
      IOUtils.write(part, this.out, UTF8);
      IOUtils.write("\r\n", this.out, UTF8);

    } catch (IOException e) {
      this._connection.disconnect();
      throw e;
    }
  }

  /**
   * Returns the response code of the underlying HTTP connection.
   * 
   * @see HttpURLConnection#getResponseCode()
   * @return the response code of the underlying HTTP connection.
   * @throws IOException If thrown by the underlying HTTP connection.
   */
  public int getResponseCode() throws IOException {
    return this._connection.getResponseCode();
  }

  /**
   * Returns the response message of the underlying HTTP connection.
   * 
   * @see HttpURLConnection#getResponseMessage()
   * @return the response message of the underlying HTTP connection.
   * @throws IOException If thrown by the underlying HTTP connection.
   */
  public String getResponseMessage() throws IOException {
    return this._connection.getResponseMessage();
  }

  /**
   * Returns the content type of the underlying HTTP connection.
   * 
   * @see HttpURLConnection#getContentType()
   * @return the content type of the underlying HTTP connection.
   * @throws IOException If thrown by the underlying HTTP connection.
   */
  public String getContentType() throws IOException {
    return this._connection.getContentType();
  }

  /**
   * Disconnects the underlying HTTP connection.
   * 
   * @see HttpURLConnection#disconnect()
   */
  public void disconnect() {
    this._connection.disconnect();
  }

  /**
   * Returns the underlying HTTP connection.
   * 
   * <p>This method can be useful to perform additional operations on the connection which are not
   * provided by this class.
   * 
   * @return the underlying HTTP connection.
   */
  public HttpURLConnection connection() {
    return this._connection;
  }

  /**
   * Returns the PageSeeder resource corresponding to the URL.
   * 
   * @return the PageSeeder resource corresponding to the URL.
   */
  public PSResource resource() {
    return this._resource;
  }

  /**
   * Returns the type of connection.
   * 
   * @return the type of connection.
   */
  public Type type() {
    return this._type;
  }


  /**
   * Process the specified PageSeeder connection. 
   * 
   * <p>If the handler is not specified, the xml writer receives a copy of the PageSeeder XML.
   * 
   * @param xml     the XML to copy from PageSeeder
   * 
   * @throws IOException If an error occurs when trying to write the XML.
   * 
   * @return <code>true</code> if the request was processed without errors;
   *         <code>false</code> otherwise.
   */
  public boolean process(XMLWriter xml) throws IOException {
    return process(xml, null, null);
  }

  /**
   * Process the specified PageSeeder connection. 
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
  public boolean process(XMLWriter xml, PSHandler handler) throws IOException {
    return process(xml, handler, null);
  }

  /**
   * Process the specified PageSeeder connection.
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
  public boolean process(XMLWriter xml, Templates templates) throws IOException {
    return process(xml, null, templates);
  }

  /**
   * Connect to PageSeeder and fetch the XML using the GET method. 
   * 
   * <p>If the handler is not specified, the xml writer receives a copy of the PageSeeder XML.
   * 
   * <p>If templates are specified they take precedence over the handler.
   * 
   * @param xml        The XML to copy from PageSeeder
   * @param handler    The handler for the XML (can be used to rewrite the XML)
   * @param templates  A set of templates to process the XML (optional)
   * 
   * @throws IOException If an error occurs when trying to write the XML.
   * 
   * @return <code>true</code> if the request was processed without errors;
   *         <code>false</code> otherwise.
   */
  protected boolean process(XMLWriter xml, PSHandler handler, Templates templates) 
      throws IOException {
    // Let's start
    xml.openElement("ps-"+this._resource.type().toString().toLowerCase(), true);
    xml.attribute("resource", this._resource.name());

    boolean ok = true;

    try {
      // Retrieve the content of the response
      int status = this._connection.getResponseCode();
      xml.attribute("http-status", status);

      if (status >= HttpURLConnection.HTTP_OK && status < HttpURLConnection.HTTP_MULT_CHOICE) {

        String contentType = this._connection.getContentType();

        // Strip ";charset" declaration if any
        if (contentType != null && contentType.indexOf(";charset=") > 0)
          contentType = contentType.substring(0, contentType.indexOf(";charset="));

        // Return content is XML try to parse it
        if (isXML(contentType)) {
          xml.attribute("content-type", "application/xml");
          if (templates != null) {
            ok = parseXML(this._connection, xml, templates);
          } else {
            ok = parseXML(this._connection, xml, handler);
          }

        // Text content
        } else if (contentType != null && contentType.startsWith("text/")) {
          xml.attribute("content-type", contentType);
          ok = parseText(this._connection, xml);

        // Probably binary
        } else {
          xml.attribute("content-type", contentType);
          xml.openElement("binary");
          xml.closeElement();
        }

      } else {
        LOGGER.info("PageSeeder returned {}: {}", status, this._connection.getResponseMessage());
        error(xml, "http-error", this._connection.getResponseMessage());
        ok = false;
      }

    } finally {
      // TODO Disconnect (???)
      if (this._connection != null) this._connection.disconnect();
      xml.closeElement();
    }

    // Return the final status
    return ok;
  }

  // Static methods
  // ----------------------------------------------------------------------------------------------

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

  /**
   * Create a PageSeeder connection for the specified URL and method.
   * 
   * <p>The connection is configured to:
   * <ul>
   *   <li>Follow redirects</li>
   *   <li>Be used for output</li>
   *   <li>Ignore cache by default</li>
   * </ul>
   * 
   * @param resource The resource to connect to.
   * @param type     The type of connection.
   * @param user     The user login to use (optional).
   * @return A newly opened connection to the specified URL
   * @throws IOException Should an exception be returns while opening the connection 
   */
  protected static PSConnection connect(PSResource resource, Type type, PageSeederUser user) throws IOException {
    URL url = resource.toURL(user);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setDoOutput(true);
    connection.setInstanceFollowRedirects(true);
    connection.setRequestMethod(type == Type.GET? "GET" : "POST");
    connection.setDefaultUseCaches(false);
    if (type == Type.MULTIPART) {
      connection.setDoInput(true);
      connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
    }
    connection.setRequestProperty("X-Requester", "Bastille-"+BASTILLE_VERSION);
    return new PSConnection(connection, resource, type);
  }

  // Processors
  // ----------------------------------------------------------------------------------------------

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
