/*
 * This file is part of the Bastille library.
 *
 * Available under a commercial licence, contact Weborganic.
 *
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package com.weborganic.bastille.flint.helpers;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.weborganic.berlioz.util.FileUtils;
import org.weborganic.flint.content.Content;
import org.weborganic.flint.content.DeleteRule;

/**
 * Content sourced from a file.
 *
 * @author Christophe Lauret
 * @version 0.6.0 - 2 June 2010
 * @since 0.6.0
 */
public final class FileContent implements Content {

  /**
   * The wrapped file to index or delete.
   */
  private final File _f;

  /**
   * Creates a new content from a given file.
   *
   * @param f The file
   */
  public FileContent(File f) {
    this._f = f;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getMediaType() {
    return FileUtils.getMediaType(this._f);
  }

  /**
   * Always <code>null</code>.
   *
   * {@inheritDoc}
   */
  @Override
  public String getConfigID() {
    return null;
  }

  /**
   * Returns a new buffered <code>FileInputStream</code>.
   *
   * {@inheritDoc}
   */
  @Override
  public InputStream getSource() {
    if (!this._f.exists()) return null;
    try {
      return new BufferedInputStream(new FileInputStream(this._f));
    } catch (Exception ex) {
      ex.printStackTrace();
      return null;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isDeleted() {
    return !this._f.exists();
  }

  /**
   * Returns a delete rule based on the path.
   *
   * {@inheritDoc}
   */
  @Override
  public DeleteRule getDeleteRule() {
    return new DeleteRule("path", FilePathRule.toPath(this._f));
  }

}
