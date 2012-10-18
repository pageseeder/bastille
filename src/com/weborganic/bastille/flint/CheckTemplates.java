/*
 * This file is part of the Bastille library.
 *
 * Available under a commercial licence, contact Weborganic.
 *
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
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

import com.topologi.diffx.xml.XMLWriter;
import com.weborganic.bastille.flint.helpers.FlintConfig;

/**
 * Checks that the templates are valid.
 *
 * @author Christophe Lauret
 * @version 0.7.4 - 18 October 2012
 * @since 0.6.20
 */
@Beta
public final class CheckTemplates implements ContentGenerator  {

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {

    // Get the templates from the config.
    File def = FlintConfig.itemplates();

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
