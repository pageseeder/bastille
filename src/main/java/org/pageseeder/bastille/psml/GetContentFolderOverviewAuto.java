/*
 * Copyright 2015 Allette Systems (Australia)
 * http://www.allette.com.au
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pageseeder.bastille.psml;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

import org.pageseeder.berlioz.content.Cacheable;
import org.pageseeder.berlioz.content.ContentStatus;
import org.pageseeder.berlioz.content.Request;
import org.pageseeder.berlioz.content.Response;
import org.pageseeder.berlioz.content.XmlGenerator;
import org.pageseeder.berlioz.xml.XmlWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Returns an overview of the folder by Berlioz path.
 *
 * <p>The overview is generated from the first header and the first paragraph from each PageSeeder XML.
 *
 * @author Christophe Lauret
 * @version 0.7.5 - 25 October 2012
 * @since 0.7.5
 */
public final class GetContentFolderOverviewAuto implements XmlGenerator, Cacheable {

  /**
   * Logger for this generator.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(GetContentFolderOverviewAuto.class);

  @Override
  public String getETag(Request req) {
    PSMLFile folder = PSMLConfig.getContentFolder(req.getBerliozPath());
    List<File> files = PSMLOverviews.getContents(folder.file());
    long mostrecent = PSMLOverviews.lastModified(files);
    return folder.path() + '_' + mostrecent;
  }

  @Override
  public Response generate(Request req, XmlWriter xml) {
    LOGGER.debug(req.getBerliozPath());

    // Get all the files
    PSMLFile folder = PSMLConfig.getContentFolder(req.getBerliozPath());
    LOGGER.debug("Retrieving overview for {}", folder);

    // If the PSML does not exist
    Response response = Response.ok();
    if (!folder.exists()) {
      response = Response.status(ContentStatus.NOT_FOUND);
    }

    // Get all the files
    File dir = folder.file();
    if (dir.exists() && dir.isDirectory()) {
      String data;
      try {
        data = PSMLOverviews.getOverview(folder);
      } catch (IOException ex) {
        throw new UncheckedIOException(ex);
      }
      xml.xml(data);
    }

    return response;
  }

}
