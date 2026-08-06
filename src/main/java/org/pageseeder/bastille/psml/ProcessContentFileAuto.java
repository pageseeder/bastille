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

import java.io.IOException;
import java.io.UncheckedIOException;

import org.jspecify.annotations.Nullable;
import org.pageseeder.berlioz.content.Cacheable;
import org.pageseeder.berlioz.content.ContentStatus;
import org.pageseeder.berlioz.content.Request;
import org.pageseeder.berlioz.content.Response;
import org.pageseeder.berlioz.content.XmlGenerator;
import org.pageseeder.berlioz.xml.XmlWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christophe Lauret
 * @version 0.7.7 - 25 October 2012
 * @since 0.7.7
 */
public final class ProcessContentFileAuto implements XmlGenerator, Cacheable {

  /**
   * Logger for debugging
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(ProcessContentFileAuto.class);

  @Override
  public @Nullable String getETag(Request req) {
    String pathInfo = req.getBerliozPath();
    PSMLFile psml = PSMLConfig.getContentFile(pathInfo);
    return PSMLLinkProcessor.getEtag(psml);
  }

  @Override
  public Response generate(Request req, XmlWriter xml) {

    // Identify the file
    String pathInfo = req.getBerliozPath();
    PSMLFile psml = PSMLConfig.getContentFile(pathInfo);
    LOGGER.debug("Processing {}", psml);

    // If the PSML does not exist
    Response response = Response.ok();
    if (!psml.exists()) {
      response = Response.status(ContentStatus.NOT_FOUND);
    }

    // Grab the data
    String data;
    try {
      data = PSMLLinkProcessor.process(psml);
    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    }

    // Write on the output
    xml.xml(data);

    return response;
  }

}
