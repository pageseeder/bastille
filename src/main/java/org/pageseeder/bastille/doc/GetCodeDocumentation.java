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
package org.pageseeder.bastille.doc;

import java.io.File;
import java.io.StringWriter;

import org.jspecify.annotations.Nullable;
import org.pageseeder.berlioz.content.Cacheable;
import org.pageseeder.berlioz.content.ContentStatus;
import org.pageseeder.berlioz.content.Environment;
import org.pageseeder.berlioz.content.Request;
import org.pageseeder.berlioz.content.Response;
import org.pageseeder.berlioz.content.XmlGenerator;
import org.pageseeder.berlioz.error.HttpException;
import org.pageseeder.berlioz.error.InvalidParameterException;
import org.pageseeder.berlioz.error.ProblemDetails;
import org.pageseeder.berlioz.xml.XmlWriter;
import org.pageseeder.cobble.CobbleException;
import org.pageseeder.cobble.XMLGenerator;

/**
 * Returns the XSLT documentation using the Cobble format
 *
 * @author Christophe Lauret
 * @version 0.13.0
 */
public final class GetCodeDocumentation implements XmlGenerator, Cacheable {

  @Override
  public @Nullable String getETag(Request req) {
    return null;
  }

  @Override
  public Response generate(Request req, XmlWriter xml) {

    String path = req.parameter("path").asString().required();

    Environment env = req.getEnvironment();
    File code = env.getPrivateFile(path);

    if (!XMLGenerator.isSupported(path) || !code.exists()) {
      throw InvalidParameterException.constraintFailed("path", path, "must reference a supported, existing file");
    }

    // Generate the document
    XMLGenerator docgen = new XMLGenerator(code);
    try {
      StringWriter w = new StringWriter();
      docgen.generate(w);
      xml.xml(w.toString());
    } catch (CobbleException ex) {
      throw HttpException.of(ProblemDetails.of(ContentStatus.INTERNAL_SERVER_ERROR).detail(ex.getMessage()));
    }

    return Response.ok();
  }

}
