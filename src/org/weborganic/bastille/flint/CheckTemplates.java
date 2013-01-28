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

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.weborganic.bastille.flint.config.FlintConfig;
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
    File def = FlintConfig.get().getIXMLTemplates("text/xml");

    // Print XML
    xml.openElement("index-templates");
    xml.attribute("name", def != null? def.getName() : "null");
    if (def == null || !def.exists()) {
      xml.attribute("status", "error");
      xml.attribute("cause", "not-found");
    } else {
      try {
        compile(def);
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
  private Templates compile(File itemplate) throws IOException, TransformerException {
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
