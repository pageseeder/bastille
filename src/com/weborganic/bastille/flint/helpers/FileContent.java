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
 * @version 2 June 2010
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
  public String getMediaType() {
    return FileUtils.getMediaType(_f);
  }

  /**
   * Always <code>null</code>.
   * 
   * {@inheritDoc}
   */
  public String getConfigID() {
    return null;
  }

  /**
   * Returns a new buffered <code>FileInputStream</code>.
   * 
   * {@inheritDoc}
   */
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
  public boolean isDeleted() {
    return !this._f.exists();
  }

  /**
   * Returns a delete rule based on the path.
   * 
   * {@inheritDoc}
   */
  public DeleteRule getDeleteRule() {
    return new DeleteRule("path", FilePathRule.toPath(this._f));
  }

}
