/*
 * This file is part of the Bastille library.
 *
 * Available under a commercial licence, contact Weborganic.
 *
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package com.weborganic.bastille.flint.helpers;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;

import org.weborganic.flint.content.ContentTranslator;
import org.weborganic.flint.content.ContentTranslatorFactory;
import org.weborganic.flint.content.SourceForwarder;

import com.weborganic.bastille.psml.PSMLConfig;

/**
 * A translator factory for PSML.
 *
 * @author Christophe Lauret
 * @version 17 October 2012
 */
public final class PSMLTranslatorFactory implements ContentTranslatorFactory {

  /**
   * The translator for XML files: a source forwarder
   */
  private final ContentTranslator _translator;

  /**
   * Creates a new instance.
   */
  public PSMLTranslatorFactory() {
    this._translator = new SourceForwarder(PSMLConfig.MEDIATYPE, Charset.forName("utf-8"));
  }

  @Override
  public Collection<String> getMimeTypesSupported() {
    return Collections.singleton(PSMLConfig.MEDIATYPE);
  }

  @Override
  public ContentTranslator createTranslator(String mimeType) {
    return this._translator;
  }

}
