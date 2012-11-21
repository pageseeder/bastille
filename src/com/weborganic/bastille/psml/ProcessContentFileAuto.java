/*
 * This file is part of the Bastille library.
 *
 * Available under a commercial licence, contact Weborganic.
 *
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package com.weborganic.bastille.psml;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.content.Cacheable;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentRequest;
import org.weborganic.berlioz.content.ContentStatus;

import com.topologi.diffx.xml.XMLWriter;

/**
 * @author Christophe Lauret
 * @version 0.7.7 - 25 October 2012
 * @since 0.7.7
 */
public final class ProcessContentFileAuto implements ContentGenerator, Cacheable {

  /**
   * Logger for debugging
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(ProcessContentFileAuto.class);

  @Override
  public String getETag(ContentRequest req) {
    String pathInfo = req.getBerliozPath();
    PSMLFile psml = PSMLConfig.getContentFile(pathInfo);
    return PSMLLinkProcessor.getEtag(psml);
  }

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {

    // Identify the file
    String pathInfo = req.getBerliozPath();
    PSMLFile psml = PSMLConfig.getContentFile(pathInfo);
    LOGGER.debug("Processing {}", psml);

    // If the PSML does not exist
    if (!psml.exists()) {
      req.setStatus(ContentStatus.NOT_FOUND);
    }

    // Grab the data
    String data = PSMLLinkProcessor.process(psml);

    // Write on the output
    xml.writeXML(data);
  }

}
