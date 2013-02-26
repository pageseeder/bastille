/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.weborganic.bastille.flint;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.weborganic.bastille.flint.config.FlintConfig;
import org.weborganic.bastille.flint.config.IFlintConfig;
import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.Beta;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentRequest;

import com.topologi.diffx.xml.XMLWriter;

/**
 * Checks that the templates are valid.
 *
 * @author Christophe Lauret
 * @version 0.7.4 - 19 October 2012
 * @since 0.6.20
 */
@Beta
public final class CheckTemplates implements ContentGenerator  {

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {

    // Get the templates from the config.
    IFlintConfig config = FlintConfig.get();

    // List all templates for the config
    Map<String, File> templates = config.getIXMLTemplates();

    // Print XML
    xml.openElement("index-templates");
    for (Entry<String, File> t : templates.entrySet()) {
      String media = t.getKey();
      File def = t.getValue();
      toXML(media, def, xml);
    }
    xml.closeElement();
  }

  /**
   * Check whether the templates can be compiled.
   *
   * @param media     The media type
   * @param itemplate The templates to load.
   * @param xml       XMl output
   *
   * @throws IOException If an IO error occurred while writing XML.
   */
  private static void toXML(String media, File itemplate, XMLWriter xml) throws IOException {
    xml.openElement("index-templates");
    xml.attribute("mediatype", media);
    xml.attribute("filename", itemplate != null? itemplate.getName() : "null");
    if (itemplate == null || !itemplate.exists()) {
      xml.attribute("status", "error");
      xml.attribute("cause", "not-found");
    } else {
      try {
        compile(itemplate);
        xml.attribute("status", "ok");
      } catch (IOException ex) {
        xml.attribute("status", "error");
        xml.attribute("cause", "io-exception");
        String message = ex.getMessage();
        xml.element("message", message != null? message : "");
      } catch (TransformerException ex) {
        xml.attribute("status", "error");
        xml.attribute("cause", "transformer-exception");
        String message = ex.getMessageAndLocation();
        xml.element("message", message != null? message : "");
      }
    }
    xml.closeElement();

  }

  /**
   * Check whether the templates can be compiled.
   *
   * @param itemplate The templates to load.
   *
   * @return The Compiled XSLT templates
   *
   * @throws IOException          If an IO error occurred
   * @throws TransformerException If an XSLT compilation error occurs.
   */
  private static Templates compile(File itemplate) throws IOException, TransformerException {
    // load the templates from the source file
    InputStream in = null;
    Templates templates = null;
    try {
      in = new FileInputStream(itemplate);
      Source source = new StreamSource(in);
      source.setSystemId(itemplate.toURI().toString());
      TransformerFactory factory = TransformerFactory.newInstance();
      templates = factory.newTemplates(source);
    } finally {
      IOUtils.closeQuietly(in);
    }
    return templates;
  }

}
