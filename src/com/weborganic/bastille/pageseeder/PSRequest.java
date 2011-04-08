package com.weborganic.bastille.pageseeder;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weborganic.berlioz.GlobalSettings;
import org.weborganic.berlioz.xml.XMLCopy;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.topologi.diffx.xml.XMLWriter;

/**
 * 
 * @author Christophe Lauret
 * @version 8 April 2011
 */
public final class PSRequest {

  /** Logger for this class */
  private static final Logger LOGGER = LoggerFactory.getLogger(PSRequest.class);

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
   * Creates a new connection to the specified resource.
   * 
   * @param type     The type of resource.
   * @param resource The 
   */
  public PSRequest(PSResourceType type, String resource) {
    this._type = type;
    this._resource = resource;
  }

  /**
   * Returns the URL for this connection.
   * 
   * @return the URL for this connection.
   */
  public URL toURL() throws MalformedURLException {
    Properties pageseeder = GlobalSettings.getNode("bastille.pageseeder");
    StringBuffer url = new StringBuffer();
    url.append(pageseeder.getProperty("scheme",        "http")).append("://");
    url.append(pageseeder.getProperty("host",          "localhost")).append(":");
    url.append(pageseeder.getProperty("port",          "8080"));
    if (this._type == PSResourceType.SERVLET) {
      url.append(pageseeder.getProperty("servletprefix", "/ps/servlet"));
      url.append(_resource);
    } else {
      url.append(_resource);
    }
    url.append("?xformat=xml");
    for (Entry<String, String> p : this._parameters.entrySet()) {
      // TODO URL escape
      url.append("&").append(p.getKey()).append("=").append(p.getValue());
    }
    return new URL(url.toString());
  }

  /**
   * Connect to PageSeeder.
   * 
   * @param xml the XML to copy from PageSeeder
   */
  public void get(XMLWriter xml) throws MalformedURLException, IOException {

    // Set up the URL
    URL url = toURL();

    // Create the connection
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setDoOutput(true);
    connection.setInstanceFollowRedirects(true);
    connection.setRequestMethod("GET");
    connection.setDefaultUseCaches(false);
    connection.setRequestProperty("X-Requester", "Bastille");

    // Retrieve the content of the response
    int status = connection.getResponseCode();

    if (status == HttpURLConnection.HTTP_OK) {
      InputStream is = connection.getInputStream();
      InputSource source = new InputSource(is);

      // Parse with the XML Copy Handler
      XMLCopy handler = new XMLCopy(xml);
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
      is.close();

    } else if (status >= HttpURLConnection.HTTP_BAD_REQUEST
             && status < HttpURLConnection.HTTP_INTERNAL_ERROR) {
      LOGGER.debug("PageSeeder returned {}: {}", status, connection.getResponseMessage());
    } else {
      LOGGER.warn("PageSeeder returned {}: {}", status, connection.getResponseMessage());
      throw new IOException("Unable to connect to Remote PageSeeder Server");
    }

    // Disconnect
    connection.disconnect();
  }

}
