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
package org.pageseeder.bastille.xml;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;

import javax.xml.parsers.SAXParser;

import org.pageseeder.berlioz.BerliozException;
import org.pageseeder.berlioz.content.ContentGenerator;
import org.pageseeder.berlioz.content.ContentRequest;
import org.pageseeder.berlioz.furi.Token;
import org.pageseeder.berlioz.furi.TokenOperator;
import org.pageseeder.berlioz.furi.TokenVariable;
import org.pageseeder.berlioz.furi.URIParameters;
import org.pageseeder.berlioz.furi.URITemplate;
import org.pageseeder.berlioz.furi.Variable;
import org.pageseeder.berlioz.xml.BerliozEntityResolver;
import org.pageseeder.berlioz.xml.BerliozErrorHandler;
import org.pageseeder.berlioz.xml.XMLCopy;
import org.pageseeder.berlioz.xml.XMLUtils;
import org.pageseeder.xmlwriter.XMLWriter;
import org.pageseeder.xmlwriter.XMLWriterImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * A content generator that can be used to get content from a URI template.
 *
 * @deprecated Will be removed in 0.12
 *
 * @author Christophe Lauret
 * @version 0.6.0 - 19 July 2010
 * @since 0.6.0
 */
@Deprecated
public class GetXMLFromURITemplate implements ContentGenerator {

  /**
   * Logger for debugging
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(GetXMLFromURITemplate.class);

  /**
   * {@inheritDoc}
   */
  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {
    // The URI template
    String template = req.getParameter("uri-template");
    if (template == null) throw new BerliozException("This generator could not be used because it does not specify a URI template.");

    // Constructing the URL from the parameters
    URIParameters parameters = new URIParameters();
    for (Token i : URITemplate.digest(template)) {
      if (i instanceof TokenVariable) {
        Variable v = ((TokenVariable)i).getVariable();
        parameters.set(v.name(), req.getParameter(v.name()));
      } else if (i instanceof TokenOperator) {
        for (Variable v : ((TokenOperator)i).variables()) {
          parameters.set(v.name(), req.getParameter(v.name()));
        }
      }
    }

    String url = URITemplate.expand(template, parameters);
    LOGGER.debug(url);

    // Invoking the service and retrieve the data
    xml.writeXML(get(url));
  }

  /**
   * Returns the PageSeeder Service data as XML.
   *
   * @param url The PageSeeder URL to invoke.
   */
  protected static String get(String url) throws IOException, BerliozException {
    StringWriter w = new StringWriter();

    try {
      // Check that the URL is valid
      URI.create(url);
      // Parse and copy the content
      XMLWriter buffer = new XMLWriterImpl(w);
      buffer.openElement("service");
      buffer.attribute("url", url);
      parse(url, new XMLCopy(buffer));
      buffer.closeElement();
      LOGGER.info("parsed {}", url);
      buffer.flush();

    } catch (IllegalArgumentException ex) {
      LOGGER.warn("URI {} is not valid: {}", url, ex.getMessage());
      w = error(url, "Specified URI is not valid: "+ex.getMessage());

    } catch (SAXException ex) {
      LOGGER.warn("Unable to parse XML for URI {}: {}", url, ex.getMessage());
      w = error(url, "Unable to parse XML: "+ex.getMessage());

    } catch (IOException ex) {
      LOGGER.warn("IO error while parsing XML for URI {}: {}", url, ex.getMessage());
      w = error(url, "IO error while parsing XML: "+ex.getMessage());
    }

    return w.toString();
  }

  /**
   * Parses the specified file using the given handler.
   *
   * @param url     The XML file to parse.
   * @param handler The content handler to use.
   *
   * @throws BerliozException Should something unexpected happen.
   */
  private static void parse(String url, ContentHandler handler) throws SAXException, IOException, BerliozException {
    SAXParser parser = XMLUtils.getParser(false);
    // get the reader
    XMLReader reader = parser.getXMLReader();
    // configure the reader
    reader.setContentHandler(handler);
    reader.setEntityResolver(BerliozEntityResolver.getInstance());
    reader.setErrorHandler(BerliozErrorHandler.getInstance());
    // parse
    reader.parse(new InputSource(url));
  }

  /**
   * Reports an error as XML on a string writer.
   *
   * @param url     The URL causing the problem.
   * @param message The error message.
   *
   * @return the XML as a string writer.
   */
  private static StringWriter error(String url, String message) throws IOException {
    StringWriter w = new StringWriter();
    XMLWriter buffer = new XMLWriterImpl(w);
    buffer.openElement("service");
    buffer.attribute("url", url);
    buffer.attribute("status", "error");
    buffer.writeText(message);
    buffer.closeElement();
    return w;
  }

}
