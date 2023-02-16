/*
 * Copyright 2015 Allette Systems (Australia)
 * http://www.allette.com.au
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pageseeder.bastille.recaptcha;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.apache.commons.io.IOUtils;
import org.pageseeder.berlioz.GlobalSettings;
import org.pageseeder.berlioz.content.ContentRequest;
import org.pageseeder.berlioz.servlet.HttpContentRequest;
import org.pageseeder.xmlwriter.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple object encapsulating the ReCaptcha configuration.
 *
 * @deprecated The ReCaptcha API is no longer accessible
 *
 * @author Christophe Lauret
 * @version Bastille 0.8.4
 */
@Deprecated
public final class ReCaptcha {

  /**
   * A logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(ReCaptcha.class);

  /**
   * The default URL to connect to.
   */
  public static final String DEFAULT_HTTP_SERVER = "http://api.recaptcha.net";

  /**
   * The default secure URL to connect to.
   */
  public static final String DEFAULT_HTTPS_SERVER = "https://api-secure.recaptcha.net";

  /**
   * The URL to verify.
   */
  public static final String VERIFY_URL = "http://api-verify.recaptcha.net/verify";

  /**
   * Used for the connection timeout (in ms)
   */
  private static final int TEN_SECONDS = 10000;

  /**
   * ReCaptcha public key.
   */
  private final String _publicKey;

  /**
   * ReCaptcha private key.
   */
  private final String _privateKey;

  /**
   * The URL of the reCaptcha server.
   */
  private final String _server;

  /**
   * Create a new ReCaptcha instance.
   *
   * @param publicKey  The public key.
   * @param privateKey The private key.
   * @param secure     <code>true</code> to contact the ReCaptcha servers via HTTPS;
   *                   <code>false</code> for HTTP.
   */
  private ReCaptcha(String publicKey, String privateKey, boolean secure) {
    this._publicKey = publicKey;
    this._privateKey = privateKey;
    this._server = secure? DEFAULT_HTTPS_SERVER : DEFAULT_HTTP_SERVER;
  }

  /**
   * Create a new ReCaptcha instance.
   *
   * @param publicKey  The public key.
   * @param privateKey The private key.
   * @param server     The URL of the reCaptcha server
   */
  private ReCaptcha(String publicKey, String privateKey, String server) {
    this._publicKey = publicKey;
    this._privateKey = privateKey;
    this._server = server;
  }

  /**
   * Returns the public key to connect to the ReCapatcha server..
   *
   * @return the public key to connect to the ReCapatcha server..
   */
  public String publicKey() {
    return this._publicKey;
  }

  /**
   * Returns the private key to connect to the ReCapatcha server.
   *
   * @return the private key to connect to the ReCapatcha server..
   */
  public String privateKey() {
    return this._privateKey;
  }

  /**
   * Returns the URL of the reCaptcha server.
   *
   * @return the URL of the reCaptcha server
   */
  public String server() {
    return this._server;
  }

  /**
   * Invoke the reCaptcha Verify API to check whether the challenge has been passed by the user.
   *
   * <p>All three parameters are required by the ReCaptcha server.
   *
   * @param remoteAddr The remote address
   * @param challenge  the challenge (supplied by the original ReCaptcha form)
   * @param response   The response to the challenge (from the user)
   *
   * @return The results of the reCaptcha challenge
   *
   * @throws ReCaptchaException if either argument is <code>null</code>
   */
  public ReCaptchaResult verify(String remoteAddr, String challenge, String response) throws ReCaptchaException {
    if (challenge == null)
      return new ReCaptchaResult(false, "recaptcha-missing-challenge");
    if (remoteAddr == null)
      return new ReCaptchaResult(false, "recaptcha-missing-remoteaddress");
    if (response == null)
      return new ReCaptchaResult(false, "recaptcha-missing-response");
    String postParameters = "privatekey=" + encode(this._privateKey)
                          + "&remoteip=" + encode(remoteAddr)
                          + "&challenge=" + encode(challenge)
                          + "&response=" + encode(response);
    LOGGER.debug("Verifying response '{}' on server {} ", response, VERIFY_URL);
    String message = getDataFromPost(VERIFY_URL, postParameters);
    ReCaptchaResult result = ReCaptchaResult.parse(message);
    LOGGER.debug("Server replied {}:{} ", result.isValid(), result.message());
    return result;
  }

  /**
   * Writes the form to display the captcha as XHTML.
   *
   * @param xml The XML output
   * @param message the error message from the server (may be <code>null</code>)
   *
   * @throws IOException if thrown while writing the XML
   */
  public void toXHTMLForm(XMLWriter xml, String message) throws IOException {

    String errorPart = message == null ? "" : "&amp;error=" + encode(message);

    // JavaScript
    xml.openElement("script");
    xml.attribute("type", "text/javascript");
    xml.attribute("src", this._server + "/challenge?k=" + this._publicKey + errorPart);
    xml.closeElement();

    // NoJavaScript
    xml.openElement("noscript");
    xml.openElement("iframe");
    xml.attribute("src", this._server+"/noscript?k="+this._publicKey + errorPart);
    xml.attribute("height", "300");
    xml.attribute("width", "500");
    xml.attribute("frameborder", "0");
    xml.closeElement();
    xml.emptyElement("br");
    xml.openElement("textarea");
    xml.attribute("name", "recaptcha_challenge_field");
    xml.attribute("rows", "3");
    xml.attribute("cols", "40");
    xml.closeElement();
    xml.openElement("input");
    xml.attribute("type", "hidden");
    xml.attribute("name", "recaptcha_response_field");
    xml.attribute("value", "manual_challenge");
    xml.closeElement();
    xml.closeElement();
  }

  // Factory methods
  // ----------------------------------------------------------------------------------------------

  /**
   * Creates a new ReCaptcha instance from the Bastille properties.
   *
   * <ul>
   *  <li><code>bastille.recaptcha.public-key</code></li>
   *  <li><code>bastille.recaptcha.private-key</code></li>
   *  <li><code>bastille.recaptcha.server</code></li>
   *  <li><code>bastille.recaptcha.secure</code></li>
   * </ul>
   *
   * @return a new ReCaptcha instance
   *
   * @throws ReCaptchaException If the server has not been configured properly
   */
  public static ReCaptcha newReCaptcha() throws ReCaptchaException {
    String publicKey = GlobalSettings.get("bastille.recaptcha.public-key");
    String privateKey = GlobalSettings.get("bastille.recaptcha.private-key");
    if (publicKey == null)
      throw new ReCaptchaException("Property 'bastille.recaptcha.public-key' is null");
    if (privateKey == null)
      throw new ReCaptchaException("Property 'bastille.recaptcha.private-key' is null");
    String server = GlobalSettings.get("bastille.recaptcha.server");
    ReCaptcha recaptcha;
    if (server != null) {
      recaptcha = new ReCaptcha(publicKey, privateKey, server);
    } else {
      boolean secure = GlobalSettings.get("bastille.recaptcha.secure", false);
      recaptcha = new ReCaptcha(publicKey, privateKey, secure);
    }
    return recaptcha;
  }

  /**
   * Automatically create a ReCaptcha instance and verify the standard parameters send.
   *
   * @param req The content request.
   *
   * @return a new ReCaptcha instance
   *
   * @throws ReCaptchaException If the server has not been configured properly
   */
  public static ReCaptchaResult verify(ContentRequest req) throws ReCaptchaException {
    ReCaptcha recaptcha = newReCaptcha();
    String challenge = req.getParameter("recaptcha_challenge_field");
    String response = req.getParameter("recaptcha_response_field");
    if (req instanceof HttpContentRequest) {
      HttpContentRequest http = (HttpContentRequest)req;
      return recaptcha.verify(http.getHttpRequest().getRemoteAddr(), challenge, response);
    } else throw new ReCaptchaException("Unable to get remote IP");
  }

  // private helpers
  // ----------------------------------------------------------------------------------------------

  /**
   * Make an HTTP Post request to the specified URL.
   *
   * @param theurl   The URL of the verify server.
   * @param postdata The data to POST
   *
   * @return The corresponding data
   *
   * @throws ReCaptchaException Wraps any exception.
   */
  private static String getDataFromPost(String theurl, String postdata) throws ReCaptchaException {
    InputStream in = null;
    URLConnection connection = null;
    try {
      // Initialise connection
      URL url = new URL(theurl);
      connection = url.openConnection();
      connection.setDoOutput(true);
      connection.setDoInput(true);
      connection.setReadTimeout(TEN_SECONDS);
      connection.setConnectTimeout(TEN_SECONDS);

      // Write POST data
      OutputStream out = connection.getOutputStream();
      out.write(postdata.getBytes());
      out.flush();

      in = connection.getInputStream();

      // Read response
      ByteArrayOutputStream bout = new ByteArrayOutputStream();
      byte[] buf = new byte[1024];
      while (true) {
        int rc = in.read(buf);
        if (rc <= 0) {
          break;
        } else {
          bout.write(buf, 0, rc);
        }
      }

      out.close();

      return bout.toString();
    } catch (IOException ex) {
      LOGGER.warn("Cannot load URL", ex);
      throw new ReCaptchaException("Cannot load URL: " + ex.getMessage(), ex);
    } finally {
      IOUtils.closeQuietly(in);
    }
  }

  /**
   * Encodes the specified string for the URL
   *
   * @param s The string to encode
   *
   * @return The encoded string.
   */
  private static String encode(String s) {
    try {
      return URLEncoder.encode(s, "utf-8");
    } catch (UnsupportedEncodingException ex) {
      throw new IllegalArgumentException(ex);
    }
  }

}
