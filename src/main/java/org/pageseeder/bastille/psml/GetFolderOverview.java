/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.pageseeder.bastille.psml;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.pageseeder.bastille.util.Errors;
import org.pageseeder.berlioz.BerliozException;
import org.pageseeder.berlioz.content.Cacheable;
import org.pageseeder.berlioz.content.ContentGenerator;
import org.pageseeder.berlioz.content.ContentRequest;
import org.pageseeder.berlioz.content.ContentStatus;
import org.pageseeder.xmlwriter.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Returns an overview of the folder by Berlioz path.
 *
 * <p>The overview is generated from the first header and the first paragraph from each PageSeeder XML.
 *
 * <h3>Configuration</h3>
 * <p>No configuration required for this generator.</p>
 *
 * <h3>Parameters</h3>
 * <p>No parameters necessary.</p>
 *
 * <h3>Returned XML</h3>
 * <pre>{@code
 *  <overview folder="[folder]">
 *    <entry file="[filename]">
 *      <title>[firsttitle]</title>
 *      <summary>[firstpara]</summary>
 *    </entry>
 *    ...
 *  <overview>
 * }</pre>
 *
 * <h3>Deployment </h3>
 * <pre>{@code
 * <generator class="org.pageseeder.bastille.xml.GetFolderOverview" name="overview" target="main" />
 * }</pre>
 *
 * @author Christophe Lauret
 * @version 0.6.35 - 21 May 2012
 * @since 0.6.33
 */
public final class GetFolderOverview implements ContentGenerator, Cacheable {

  /**
   * Logger for this generator.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(GetFolderOverview.class);

  @Override
  public String getETag(ContentRequest req) {
    String path = req.getParameter("path");
    if (path == null) return null;
    PSMLFile psml = PSMLConfig.getFolder(path);
    List<File> files = PSMLOverviews.getContents(psml.file());
    long mostrecent = PSMLOverviews.lastModified(files);
    return psml.path() + '_' + mostrecent;
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
    PSMLFile folder = PSMLConfig.getFolder(path);
    LOGGER.debug("Retrieving overview for {}", folder);

    // If the PSML does not exist
    if (!folder.exists()) {
      req.setStatus(ContentStatus.NOT_FOUND);
    }

    // Get all the files
    File dir = folder.file();
    if (dir.exists() && dir.isDirectory()) {
      String data = PSMLOverviews.getOverview(folder);
      xml.writeXML(data);
    }
  }

}
