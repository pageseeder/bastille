/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.pageseeder.bastille.pageseeder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.pageseeder.berlioz.xml.XMLCopy;
import org.pageseeder.xmlwriter.XMLWriter;
import org.pageseeder.xmlwriter.XMLWriterImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Wraps an HTTP connection to PageSeeder.
 *
 * @author Christophe Lauret
 *
 * @version 0.8.1 - 18 December 2012
 * @since 0.6.7
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
    Package p = Package.getPackage("org.pageseeder.bastille");
    BASTILLE_VERSION = p != null ? p.getImplementationVersion() : "unknown";
  }

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
   * The user who initiated the connection.
   *
   * A <code>null</code> value indicates an anonymous connection.
   */
  private final PSUser _user;

  /**
   * The part boundary.
   */
  private final String _boundary;

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
   * @param user       The user who initiated the connection (may be <code>null</code>)
   * @param boundary   The boundary to use for multipart (may be <code>null</code>)
   */
  private PSConnection(HttpURLConnection connection, PSResource resource, Type type, PSUser user, String boundary) {
    this._connection = connection;
    this._resource = resource;
    this._type = type;
    this._user = user;
    this._boundary = boundary;
  }

  /**
   * Add a part to the request (write the contents directly to the stream).
   *
   * @param part The encoding to specify in the Part's header
   * @throws IOException Should any error occur while writing the part on the output
   */
  public void addXMLPart(String part) throws IOException {
    addXMLPart(part, null);
  }

  /**
   * Add a part to the request (write the contents directly to the stream).
   *
   * @param part     The encoding to specify in the Part's header
   * @param headers A list of headers added to this XML Part ('content-type' header is ignored)
   *
   * @throws IOException Should any error occur while writing
   */
  public void addXMLPart(String part, Map<String, String> headers) throws IOException {
    if (this.out == null) {
      this.out = new DataOutputStream(this._connection.getOutputStream());
    }
    try {
      if (this._type != Type.MULTIPART)
        throw new IOException("Cannot add XML part unless connection type is set to Multipart");

      // Start with boundary
      IOUtils.write(this._boundary+"\r\n", this.out, UTF8);
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

    } catch (IOException ex) {
      IOUtils.closeQuietly(this.out);
      this.out = null;
      throw ex;
    }
  }

  /**
   * Closes the output stream when writing to the connection.
   *
   * <p>This method does nothing if the output stream hasn't been created.
   *
   * @throws IOException If thrown by the close method.
   */
  public void closeOutput() throws IOException {
    if (this.out != null) {
      this.out.close();
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
   * @deprecated There is no need to call this method; the socket will be recycled.
   *
   * @see HttpURLConnection#disconnect()
   */
  @Deprecated
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
    return process(xml, null, null, null);
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
    return process(xml, handler, null, null);
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
    return process(xml, null, templates, null);
  }

  /**
   * Process the specified PageSeeder connection.
   *
   * <p>Templates can be specified to transform the XML.
   *
   * @param xml        The XML to copy from PageSeeder
   * @param templates  A set of templates to process the XML (optional)
   * @param parameters Parameters to send to the XSLT transformer (optional)
   *
   * @throws IOException If an error occurs when trying to write the XML.
   *
   * @return <code>true</code> if the request was processed without errors;
   *         <code>false</code> otherwise.
   */
  public boolean process(XMLWriter xml, Templates templates, Map<String, String> parameters) throws IOException {
    return process(xml, null, templates, parameters);
  }

  /**
   * Connect to PageSeeder and fetch the XML using the GET method.
   *
   * <p>If the handler is not specified, the XML writer receives a copy of the PageSeeder XML.
   *
   * <p>If templates are specified they take precedence over the handler.
   *
   * @param xml        The XML to copy from PageSeeder
   * @param handler    The handler for the XML (can be used to rewrite the XML)
   * @param templates  A set of templates to process the XML (optional)
   * @param parameters Parameters to send to the transformer (optional).
   *
   * @throws IOException If an error occurs when trying to write the XML.
   *
   * @return <code>true</code> if the request was processed without errors;
   *         <code>false</code> otherwise.
   */
  protected boolean process(XMLWriter xml, PSHandler handler, Templates templates, Map<String, String> parameters)
      throws IOException {
    // Let's start
    xml.openElement("ps-"+this._resource.type().toString().toLowerCase(), true);
    xml.attribute("resource", this._resource.name());

    boolean ok = true;

    try {
      // Retrieve the content of the response
      int status = this._connection.getResponseCode();
      xml.attribute("http-status", status);

      if (isOK(status) || (this._resource.includeErrorContent() && isError(status))) {

        String contentType = this._connection.getContentType();

        // Strip ";charset" declaration if any
        if (contentType != null && contentType.indexOf(";charset=") > 0) {
          contentType = contentType.substring(0, contentType.indexOf(";charset="));
        }

        // Return content is XML try to parse it
        if (isXML(contentType)) {
          xml.attribute("content-type", "application/xml");
          if (templates != null) {
            ok = parseXML(this._connection, xml, templates, parameters);
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

        // Updating the session of the PageSeeder user
        if (this._user != null) {
          PSSession session = this._user.getSession();
          if (session != null) {
            session.update();
          }
        } else {
          PSSession session = PSUsers.getAnonymous();
          if (PSUsers.isValid(session)) {
            session.update();
          } else {
            LOGGER.info("Setting anonymous PageSeeder session");
            PSUsers.setAnonymous(new PSSession(getJSessionIDfromCookie(this._connection)));
          }
        }

      } else {
        LOGGER.info("PageSeeder returned {}: {}", status, this._connection.getResponseMessage());
        psError(xml, "pageseeder-error", this._connection);
        ok = false;
      }

    // Could not connect to the server
    } catch (ConnectException ex) {
      String message = ex.getMessage();
      LOGGER.info("Unable to connect to PageSeeder: {}", message);
      error(xml, "connection-error", message != null? message : "Unable to connect");
      ok = false;

    } finally {
      // There is no need to use the disconnect() method on the connection,
      // the socket will be recycled after the content has been read fully.
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
  protected static PSConnection connect(PSResource resource, Type type, PSUser user) throws IOException {
    URL url = resource.toURL(user, type == Type.POST? false : true);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setDoOutput(true);
    connection.setInstanceFollowRedirects(true);
    connection.setRequestMethod(type == Type.GET? "GET" : "POST");
    connection.setDefaultUseCaches(false);
    connection.setRequestProperty("X-Requester", "Bastille-"+BASTILLE_VERSION);
    String boundary = null;

    // POST using "application/x-www-form-urlencoded"
    if (type == Type.POST) {
      String parameters = resource.getPOSTFormURLEncodedContent();
      connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
      connection.setRequestProperty("Content-Length", Integer.toString(parameters.length()));
      connection.setDoInput(true);
      writePOSTData(connection, parameters);

    // POST using "multipart/form-data"
    } else if (type == Type.MULTIPART) {
      boundary = "-----------" + new Random().nextLong();
      connection.setDoInput(true);
      connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
    }
    return new PSConnection(connection, resource, type, user, boundary);
  }

  /**
   * Write the POST content.
   *
   * @param connection The URL connection
   * @param data       The data to write
   *
   * @throws IOException Should any error occur while writing.
   */
  private static void writePOSTData(HttpURLConnection connection, String data) throws IOException {
    OutputStream post = null;
    try {
      post = connection.getOutputStream();
      post.write(data.getBytes(UTF8));
      post.flush();
    } catch (IOException ex) {
      LOGGER.error("An error occurred while writing POST data", ex);
      IOUtils.closeQuietly(post);
      throw ex;
    }
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
    factory.setNamespaceAware(true);

    InputStream in = null;
    try {
      // Get the source as input stream
      in = isOK(connection.getResponseCode())? connection.getInputStream() : connection.getErrorStream();
      InputSource source = new InputSource(in);

      // Ensure the encoding is correct
      String encoding = connection.getContentEncoding();
      if (encoding != null) {
        source.setEncoding(encoding);
      }

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
   * @param parameters Parameters to send to the transformer (optional).
   *
   * @return <code>true</code> if the data was parsed without error;
   *         <code>false</code> otherwise.
   *
   * @throws IOException If an error occurs while writing the XML.
   */
  private static boolean parseXML(HttpURLConnection connection, XMLWriter xml, Templates templates,
      Map<String, String> parameters) throws IOException {
    boolean ok = true;

    // Create an XML Buffer
    StringWriter buffer = new StringWriter();

    InputStream in = null;
    try {
      in = isOK(connection.getResponseCode())? connection.getInputStream() : connection.getErrorStream();

      // Setup the source
      StreamSource source = new StreamSource(in);
      source.setSystemId(connection.getURL().toString());

      // Setup the result
      StreamResult result = new StreamResult(buffer);

      // Create a transformer from the templates
      Transformer transformer = templates.newTransformer();

      // Add parameters
      if (parameters != null) {
        for (Entry<String, String> p : parameters.entrySet()) {
          transformer.setParameter(p.getKey(), p.getValue());
        }
      }

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
      in = isOK(connection.getResponseCode())? connection.getInputStream() : connection.getErrorStream();
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
   * Indicates whether the response was successful based on the HTTP code.
   *
   * @param code the HTTP status code.
   * @return <code>true</code> if the code is between 200 and 299 (included);
   *         <code>false</code>.
   */
  private static boolean isOK(int code) {
    return code >= HttpURLConnection.HTTP_OK && code < HttpURLConnection.HTTP_MULT_CHOICE;
  }

  /**
   * Indicates whether the response failed based on the HTTP code.
   *
   * @param code the HTTP status code.
   * @return <code>true</code> if the code is greater than 400 (included);
   *         <code>false</code>.
   */
  private static boolean isError(int code) {
    return code >= HttpURLConnection.HTTP_BAD_REQUEST;
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
   * Adds the attributes for when error occurs
   *
   * @param xml        The XML output.
   * @param error      The error code.
   * @param connection The PS Connection.
   *
   * @throws IOException If thrown while writing the XML.
   */
  private static void psError(XMLWriter xml, String error, HttpURLConnection connection) throws IOException {
    xml.attribute("error", error);
    String message = null;
    InputStream err = null;
    try {
      err = getErrorStream(connection);

      // Setup the input source
      InputSource source = new InputSource(err);
      String encoding = connection.getContentEncoding();
      if (encoding != null) {
        source.setEncoding(encoding);
      }

      // And parse!
      message = ErrorMessageGrabber.getMessage(source);

    } catch (IOException ex) {
      ex.printStackTrace();
    } finally {
      IOUtils.closeQuietly(err);
    }

    if (message == null || "".equals(message)) {
      message = connection.getResponseMessage();
    }
    xml.attribute("message", message);
  }

  /**
   * Returns the error stream to parse.
   *
   * <p>If debug is enabled, the content of the error stream is printed onto the System error stream.
   *
   * @param connection HTTP connection.
   *
   * @return the error stream.
   *
   * @throws IOException If thrown while writing the XML.
   */
  private static InputStream getErrorStream(HttpURLConnection connection) throws IOException {
    InputStream err = null;
    if (LOGGER.isDebugEnabled()) {
      InputStream tmp = connection.getErrorStream();
      try {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        IOUtils.copy(tmp, buffer);
        buffer.writeTo(System.err);
        err = new ByteArrayInputStream(buffer.toByteArray());
      } finally {
        IOUtils.closeQuietly(tmp);
      }
    } else {
      err = connection.getErrorStream();
    }
    return err;
  }

  /**
   * Extract the JSession ID from the 'Set-Cookie' response header.
   *
   * @param connection The connection (must have been connected)
   * @return the JSession ID or <code>null</code>.
   */
  private static String getJSessionIDfromCookie(HttpURLConnection connection) {
    String name = "JSESSIONID=";
    String cookie = connection.getHeaderField("Set-Cookie");
    if (cookie != null && cookie.length() > name.length()) {
      int from = cookie.indexOf(name);
      int to = cookie.indexOf(';', from+name.length());
      if (from != -1 && to != -1) return cookie.substring(from+name.length(), to);
    }
    // no sessionid it seems.
    return null;
  }

  /**
   * Extracts the error message from the "message" element in the XML response returned by PageSeeder.
   *
   * @author Christophe Lauret
   * @version 15 November 2011
   */
  private static class ErrorMessageGrabber extends DefaultHandler {

    /**
     * Name of the message element.
     */
    private static final String MESSAGE_ELEMENT = "message";

    /**
     * The error message.
     */
    private final StringBuilder message = new StringBuilder();

    /** State: Within "message" element. */
    private boolean isMessage = false;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
      if (MESSAGE_ELEMENT.equals(localName) || MESSAGE_ELEMENT.equals(qName)) {
        this.isMessage = true;
      }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
      if (MESSAGE_ELEMENT.equals(localName) || MESSAGE_ELEMENT.equals(qName)) {
        this.isMessage = false;
      }
    }

    @Override
    public void characters(char[] ch, int start, int length) {
      if (this.isMessage) {
        this.message.append(ch, start, length);
      }
    }

    /**
     * Returns the error message found in the XML.
     *
     * @return the error message found in the XML.
     */
    public String getMessage() {
      return this.message.toString();
    }

    /**
     * Returns the error message found in the specified XML Input Source.
     *
     * @param source the XML input source to parse.
     * @return the error message found in the specified XML Input Source.
     *
     * @throws IOException If unable to parse response due to IO error.
     */
    public static String getMessage(InputSource source) throws IOException {
      String message = null;
      try {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(false);

        // And parse!
        SAXParser parser = factory.newSAXParser();
        ErrorMessageGrabber grabber = new ErrorMessageGrabber();
        parser.parse(source, grabber);
        message = grabber.getMessage();
      } catch (SAXException ex) {
        LOGGER.warn("Unable to parse error message from PS Response", ex);
      } catch (ParserConfigurationException ex) {
        LOGGER.warn("Unable to parse error message from PS Response", ex);
      }
      return message;
    }
  }

}
