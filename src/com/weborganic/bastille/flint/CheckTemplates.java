/*
 * Copyright (c) 2011 weborganic systems pty. ltd.
 */
package com.weborganic.bastille.flint;

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
import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.Beta;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentRequest;
import org.weborganic.berlioz.content.Environment;

import com.topologi.diffx.xml.XMLWriter;

/**
 * Checks that the templates are valid.
 * 
 * @author Christophe Lauret 
 * @version 0.6.21 - 29 September 2011
 * @since 0.6.20
 */
@Beta
public final class CheckTemplates implements ContentGenerator  {

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {

    // Check the
    Environment env = req.getEnvironment();
    File def = env.getPrivateFile("ixml/default.xsl");

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
   * 
   * @param def
   * @return
   * @throws IOException
   * @throws TransformerException
   */
  private Templates compile(File def) throws IOException, TransformerException {
    // load the templates from the source file
    InputStream in = null;
    Templates templates = null;
    try {
      in = new FileInputStream(def);
      Source source = new StreamSource(in); 
      source.setSystemId(def.toURI().toString());
      TransformerFactory factory = TransformerFactory.newInstance();
      templates = factory.newTemplates(source);
    } finally {
      IOUtils.closeQuietly(in);
    }
    return templates;
  }

}
