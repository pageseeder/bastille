/*
 * This file is part of the Bastille library.
 *
 * Available under a commercial licence, contact Weborganic.
 *
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package com.weborganic.bastille.psml;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.content.Cacheable;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentRequest;
import org.weborganic.berlioz.content.ContentStatus;

import com.topologi.diffx.xml.XMLWriter;
import com.weborganic.bastille.util.Errors;

/**
 * Returns a file from the path.
 *
 * @author Christophe Lauret
 * @version 0.7.0 - 6 October 2012
 * @since 0.7.0
 */
public final class GetFile implements ContentGenerator, Cacheable {

  /**
   * Logger for debugging
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(GetFile.class);

  @Override
  public String getETag(ContentRequest req) {
    String path = req.getParameter("path");
    if (path == null) return null;
    PSMLFile psml = PSMLConfig.getFile(path);
    if (!psml.exists()) return null;
    File f = psml.file();
    return Long.toString(f.lastModified());
  }

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {
    // Check that the path has been specified
    String path = req.getParameter("path");
    if (path == null) {
      Errors.noParameter(req, xml, "path");
      return;
    }

    // Grab the file
    PSMLFile psml = PSMLConfig.getFile(path);
    LOGGER.debug("Retrieving {}", psml);

    // If the PSML does not exist
    if (!psml.exists()) {
      req.setStatus(ContentStatus.NOT_FOUND);
    }

    // Grab the data
    String data = PSMLCache.getContent(psml);

    // Write on the output
    xml.writeXML(data);
  }

}
