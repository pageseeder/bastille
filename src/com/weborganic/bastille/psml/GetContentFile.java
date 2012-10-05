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
 * This generator returns the content of a PSML file using the Berlioz path.
 *
 * <p>For example, if the Berlioz servlet is mapped to '/html/*', 'html/Ping/pong' will try to
 * look for XML file 'Ping/pong.xml' in the XML folder.
 *
 * <p>For example, if the Berlioz servlet is mapped to '*.html', 'Ping/pong.html' will try to
 * look for XML file 'Ping/pong.xml' in the XML folder.
 *
 * <h3>Configuration</h3>
 * <p>The root PSML folder can be configured globally using the Berlioz configuration:
 * <p>For example:
 * <pre>{@code
 * <node name="bastille">
 *   <map/>
 *   <node name="psml">
 *     <map>
 *       <entry key="root" value="psml"/>
 *     </map>
 *   </node>
 * </node>
 * }</pre>
 *
 * <p>To define the location of the XML folder, use the Berlioz config property:
 * <code>bastille.psml.root</code>.
 *
 * @author Christophe Lauret
 * @version 0.7.0 - 6 October 2012
 * @since 0.7.0
 */
public final class GetContentFile implements ContentGenerator, Cacheable {

  /**
   * Logger for debugging
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(GetContentFile.class);

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
