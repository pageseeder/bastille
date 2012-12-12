/*
 * This file is part of the Bastille library.
 *
 * Available under a commercial licence, contact Weborganic.
 *
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package org.weborganic.bastille.flint.helpers;

import java.io.File;

import org.weborganic.bastille.flint.config.IFlintConfig;
import org.weborganic.berlioz.util.FileUtils;
import org.weborganic.flint.content.Content;
import org.weborganic.flint.content.DeleteRule;
import org.weborganic.flint.local.LocalFileContent;


/**
 * Content from a file.
 *
 * <p>This class provides a basic implementations of the {@link Content} interface for use by Bastille.
 *
 * @author Christophe Lauret
 * @version 0.7.3 - 17 October 2012
 * @since 0.6.0
 */
public final class FileContent extends LocalFileContent implements Content {

  /**
   * The flint configuration used for this file.
   */
  private final IFlintConfig _config;

  /**
   * Creates a new content from a given file.
   *
   * @param f      The file
   * @param config The flint configuration for this file.
   */
  public FileContent(File f, IFlintConfig config) {
    super(f);
    this._config = config;
  }

  @Override
  public String getMediaType() {
    return FileUtils.getMediaType(file());
  }

  /**
   * Returns a delete rule based on the path.
   *
   * {@inheritDoc}
   */
  @Override
  public DeleteRule getDeleteRule() {
    return new DeleteRule("path", this._config.toPath(file()));
  }

}
