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
import java.io.FileFilter;

import org.jspecify.annotations.Nullable;
import org.pageseeder.bastille.util.Paths;
import org.pageseeder.berlioz.content.Cacheable;
import org.pageseeder.berlioz.content.Request;
import org.pageseeder.berlioz.content.Response;
import org.pageseeder.berlioz.content.XmlGenerator;
import org.pageseeder.berlioz.util.FileUtils;
import org.pageseeder.berlioz.xml.XmlWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Returns information about a file in the WEB-INF/psml based on the specified by the path info.
 *
 * <p>If the file is a directory, lists the files corresponding to the specified directory.
 *
 * @author Christophe Lauret
 * @version 0.13.0
 */
public final class GetContentFolderInfoAuto implements XmlGenerator, Cacheable {

  /**
   * Filters XML files only.
   */
  private static final FileFilter DIRECTORIES_OR_PSML_FILES = file -> file.isDirectory() || file.getName().endsWith(PSMLConfig.DEFAULT_PSML_EXTENSION);

  /**
   * Logger for debugging
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(GetContentFolderInfoAuto.class);

  @Override
  public @Nullable String getETag(Request req) {
    String pathInfo = req.getBerliozPath();
    PSMLFile psml = PSMLConfig.getContentFolder(pathInfo);
    if (!psml.exists()) return null;
    File f = psml.file();
    return Long.toString(f.lastModified());
  }

  @Override
  public Response generate(Request req, XmlWriter xml) {

    File ancestor = PSMLConfig.getContentRoot();

    // Identify the folder
    File folder = new File(ancestor, req.getBerliozPath());

    if (FileUtils.contains(ancestor, folder)) {
      LOGGER.info("Retrieving content folder information for {}", req.getBerliozPath());
      Paths.toXml(ancestor, folder, DIRECTORIES_OR_PSML_FILES, xml);
    } else {
      LOGGER.warn("Attempted to access unauthorizes private file {}", req.getBerliozPath());
    }

    return Response.ok();
  }

}
