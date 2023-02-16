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
 * <p>This generator returns the content of a PSML file using specified <code>path</code> parameter from the PSML
 *  <i>content</i> folder.</p>
 *
 * <p>The content folder is the folder named 'content' under the PSML root folder.</p>
 *
 * <p>The purpose of the content folder is to include the files which can be viewed as part of the main content
 * of the Website, that is could be addressable or viewed on their own on the site.</p>
 *
 * <h3>Configuration</h3>
 * <p>No configuration is required.</p>
 * <p>The root of the PSML folder can be configured using the global property <code>bastille.psml.root</code> which
 *  can be either an absolute path or a relative path from the global repository.</p>
 * <p>By default, the PSML root is set to "psml" which usually corresponds to the <code>/WEB-INF/psml</code> folder
 *  of your Web application.</p>
 * <h3>Parameters</h3>
 * <p>This generator requires a <code>path</code> parameter. The path parameter <b>must</b> be a relative path from
 * within the psml content folder to a PSML file without the extension.</p>
 *
 * <h3>Returned XML</h3>
 * <p>Like most PSML generators returning a PSML file, this generator will wrap the content of the file with the
 *  <code>&lt;psml-file&gt;</code> element.</p>
 * <p>The <code>base</code> attribute is the relative path from the PSML root to the folder, since references
 *  (xrefs and image sources) in PSML are relative, this attribute can be used to construct the path to images
 *  or cross-referenced documents.</p>
 *
 * <h3>Error handling</h3>
 * <p>If the file cannot be found or read, the status of this generator will be set to 'NOT_FOUND'.
 *
 * @ps.note If the path resolves to a location outside the PSML folder, this generator will not read the file.
 * @ps.note It is perfectly possible to store and access files outside the /content folder as part of the main content.
 *          But you will need to use the <code>GetFile</code> generator instead.
 *
 * @author Christophe Lauret
 * @version 0.7.5 - 25 October 2012
 * @since 0.7.0
 */
public final class GetContentFile implements ContentGenerator, Cacheable {

  /**
   * Logger for debugging
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(GetContentFile.class);

  @Override
  public String getETag(ContentRequest req) {
    String path = req.getParameter("path");
    if (path == null) return null;
    PSMLFile psml = PSMLConfig.getContentFile(path);
    if (!psml.exists()) return null;
    File f = psml.file();
    return Long.toString(f.lastModified());
  }

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws IOException {

    // Identify the file
    String path = req.getParameter("path");
    if (path == null) {
      Errors.noParameter(req, xml, "path");
      return;
    }

    PSMLFile psml = PSMLConfig.getContentFile(path);
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
