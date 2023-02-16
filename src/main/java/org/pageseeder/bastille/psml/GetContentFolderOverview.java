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
 * <h3>Deployment </h3>
 * <pre>{@code
 * <generator class="org.pageseeder.bastille.xml.GetFolderOverview" name="overview" target="main" />
 * }</pre>
 *
 * @author Christophe Lauret
 * @version 0.7.5 - 25 October 2012
 * @since 0.7.0
 */
public final class GetContentFolderOverview implements ContentGenerator, Cacheable {

  /**
   * Logger for this generator.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(GetContentFolderOverview.class);

  @Override
  public String getETag(ContentRequest req) {
    String path = req.getParameter("path");
    if (path == null) return null;
    PSMLFile folder = PSMLConfig.getContentFolder(path);
    List<File> files = PSMLOverviews.getContents(folder.file());
    long mostrecent = PSMLOverviews.lastModified(files);
    return folder.path() + '_' + mostrecent;
  }

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws IOException {
    LOGGER.debug(req.getBerliozPath());

    String path = req.getParameter("path");
    if (path == null) {
      Errors.noParameter(req, xml, "path");
      return;
    }

    // Get all the files
    PSMLFile folder = PSMLConfig.getContentFolder(path);
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
