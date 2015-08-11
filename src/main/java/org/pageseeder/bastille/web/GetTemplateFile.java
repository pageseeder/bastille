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
package org.pageseeder.bastille.web;

import java.io.File;
import java.io.IOException;

import org.pageseeder.berlioz.BerliozException;
import org.pageseeder.berlioz.content.Cacheable;
import org.pageseeder.berlioz.content.ContentGenerator;
import org.pageseeder.berlioz.content.ContentRequest;
import org.pageseeder.xmlwriter.XMLWriter;

/**
 * This generator returns the XML for a template file.
 *
 * <p>It is designed to be used as a base class, it cannot be instantiated without arguments.
 *
 * @deprecated Use {@link org.pageseeder.bastille.psml.GetConfigFile} instead.
 *
 * @author Christophe Lauret
 * @version 0.6.0 - 31 May 2010
 * @since 0.6.0
 */
@Deprecated
public abstract class GetTemplateFile implements ContentGenerator, Cacheable {

  /**
   * The template name (eg. "header", "footer", etc...)
   */
  private final String _name;

  /**
   * Creates a new template file generator for the given template name.
   *
   * @param name the template name (eg. "header", "footer", etc...)
   */
  public GetTemplateFile(String name) {
    this._name = name;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final String getETag(ContentRequest req) {
    boolean reload = "true".equals(req.getParameter("reload-conf-properties"));
    return TemplateFile.getETag(this._name, reload);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {
    boolean reload = "true".equals(req.getParameter("reload-conf-properties"));
    File f = TemplateFile.getFile(this._name, reload);
    TemplateFile.write(xml, f);
  }
}
