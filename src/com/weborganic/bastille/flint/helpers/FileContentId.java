/*
 * Copyright (c) 2011 weborganic systems pty. ltd.
 */
package com.weborganic.bastille.flint.helpers;

import java.io.File;
import java.io.IOException;

import org.weborganic.flint.content.ContentId;
import org.weborganic.flint.content.ContentType;

/**
 * A basic implementation of the Flint's ContentId interface for uses by local system files.
 * 
 * <p>The ID for each file is their canonical path.
 * 
 * @author Christophe Lauret
 * @version 0.6.0 - 16 July 2010
 * @since 0.6.0
 */
public final class FileContentId implements ContentId {

  /**
   * The ID for the file.
   */
  private final String _id;

  /**
   * The file.
   */
  private final File _file;

  /**
   * Creates a new File Content Id for the specified File.
   * 
   * @param file The file to index.
   */
  public FileContentId(File f) {
    this._id = toID(f);
    this._file = f;
  }

  /**
   * {@inheritDoc}
   */
  public ContentType getContentType() {
    return FileContentType.SINGLETON;
  }

  /**
   * {@inheritDoc}
   */
  public String getID() {
    return this._id;
  }

  /**
   * Returns the file.
   * 
   * @return the underlying file.
   */
  public File file() {
    return this._file;
  }

  @Override
  public String toString() {
    return this._id;
  }

  /**
   * Returns a unique identifier for the specified file.
   * 
   * @param f The file
   * @return The canonical path as its ID.
   */
  private static String toID(File f) {
    String id = null;
    try {
      id = f.getCanonicalPath();
    } catch (IOException ex) {
      id = f.getAbsolutePath();
    }
    return id;
  }

}
