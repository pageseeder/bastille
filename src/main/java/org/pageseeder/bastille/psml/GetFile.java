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

import org.jspecify.annotations.Nullable;
import org.pageseeder.berlioz.content.Cacheable;
import org.pageseeder.berlioz.content.ContentStatus;
import org.pageseeder.berlioz.content.Request;
import org.pageseeder.berlioz.content.Response;
import org.pageseeder.berlioz.content.XmlGenerator;
import org.pageseeder.berlioz.error.ProblemDetails;
import org.pageseeder.berlioz.xml.XmlWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Returns a file from the path.
 *
 * @author Christophe Lauret
 * @version 0.13.0
 * @since 0.7.0
 */
public final class GetFile implements XmlGenerator, Cacheable {

  /**
   * Logger for debugging
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(GetFile.class);

  @Override
  public @Nullable String getETag(Request req) {
    String path = req.getParameter("path");
    if (path == null) return null;
    PSMLFile psml = PSMLConfig.getFile(path);
    if (!psml.exists()) return null;
    File f = psml.file();
    return Long.toString(f.lastModified());
  }

  @Override
  public Response generate(Request req, XmlWriter xml) {
    // Check that the path has been specified
    String path = req.parameter("path").asString().required();

    // Grab the file
    PSMLFile psml = PSMLConfig.getFile(path);
    LOGGER.debug("Retrieving {}", psml);

    // If the PSML does not exist
    Response response = Response.ok();
    if (!psml.exists()) {
      response = Response.status(ContentStatus.NOT_FOUND);
    }

    // Grab the data
    String data;
    try {
      data = PSMLCache.getContent(psml);
    } catch (IOException ex) {
      LOGGER.warn("Unable to load {}", psml, ex);
      return Response.problem(ProblemDetails.of(ContentStatus.INTERNAL_SERVER_ERROR)
          .detail("Unable to load PSML file: " + psml.path())
          .diagnostic(ex));
    }

    // Write on the output
    xml.xml(data);

    return response;
  }

}
