/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
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
