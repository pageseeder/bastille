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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.content.Cacheable;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentRequest;
import org.weborganic.berlioz.content.ContentStatus;

import com.topologi.diffx.xml.XMLWriter;

/**
 * Returns an overview of the folder by Berlioz path.
 *
 * <p>The overview is generated from the first header and the first paragraph from each PageSeeder XML.
 *
 * @author Christophe Lauret
 * @version 0.7.5 - 25 October 2012
 * @since 0.7.5
 */
public final class GetContentFolderOverviewAuto implements ContentGenerator, Cacheable {

  /**
   * Logger for this generator.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(GetContentFolderOverviewAuto.class);

  @Override
  public String getETag(ContentRequest req) {
    PSMLFile folder = PSMLConfig.getContentFolder(req.getBerliozPath());
    List<File> files = PSMLOverviews.getContents(folder.file());
    long mostrecent = PSMLOverviews.lastModified(files);
    return folder.path() + '_' + mostrecent;
  }

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {
    LOGGER.debug(req.getBerliozPath());

    // Get all the files
    PSMLFile folder = PSMLConfig.getContentFolder(req.getBerliozPath());
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
