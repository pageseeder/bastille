/*
 * Copyright (c) 2011 weborganic systems pty. ltd.
 */
package com.weborganic.bastille.flint.helpers;

import org.weborganic.flint.content.ContentType;

/**
 * A content type provided for convenience that corresponds to a local file.
 * 
 * @author Christophe Lauret
 * @version 0.6.0 - 2 June 2010
 * @since 0.6.0
 */
public final class FileContentType implements ContentType {

  /**
   * Sole instance.
   */
  public static final FileContentType SINGLETON = new FileContentType();

  /**
   * Always returns "LocalFile".
   * @return Always "<code>LocalFile</code>".
   */
  @Override public String toString() {
    return "LocalFile";
  }

}
