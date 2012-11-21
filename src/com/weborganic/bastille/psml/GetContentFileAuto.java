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

/**
 * This generator returns the content of a PSML file using the Berlioz path from content folder.
 *
 * <p>This generator behaves as if its were using the Berlioz path as path parameter of the <code>GetContentFile</code>
 * generator
 *
 * <p>The Berlioz path depends on the mapping of the Berlioz servlet in the Web descriptor (web.xml) and usually
 * corresponds to the value of the '*' in the path component of a matching URL. The Berlioz path is then appended
 * to the /content folder and added the '.psml' extension. to identify the file to load.
 *
 * <h3>Configuration</h3>
 * <p>No configuration is required.</p>
 *
 * <p>The root of the PSML folder can be configured using the global property <code>bastille.psml.root</code> which
 * can be either an absolute path or a relative path from the global repository.
 *
 * <p>By default, the PSML root is set to "psml" which usually corresponds to the /WEB-INF/psml folder of your
 * Web application.
 *
 * <h3>Parameters</h3>
 * <p>This generator does not accept or require any parameter.</p>
 *
 * <h3>Returned XML</h3>
 * <p>Like most PSML generators returning a PSML file, this generator will wrap the content of the file with
 * the <code>{@code <psml-file>}</code> element.
 *
 * The base attribute is the relative path from the PSML root to the folder, since references (xrefs and image
 * sources) in PSML are relative, this attribute can be used to construct the path to images or cross-referenced
 * documents.
 *
 * @author Christophe Lauret
 * @version 0.7.5 - 25 October 2012
 * @since 0.7.5
 */
public final class GetContentFileAuto implements ContentGenerator, Cacheable {

  /**
   * Logger for debugging
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(GetContentFileAuto.class);

  @Override
  public String getETag(ContentRequest req) {
    String pathInfo = req.getBerliozPath();
    PSMLFile psml = PSMLConfig.getContentFile(pathInfo);
    if (!psml.exists()) return null;
    File f = psml.file();
    return Long.toString(f.lastModified());
  }

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {

    // Identify the file
    String pathInfo = req.getBerliozPath();
    PSMLFile psml = PSMLConfig.getContentFile(pathInfo);
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
