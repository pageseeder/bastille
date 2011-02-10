package com.weborganic.bastille.flint.helpers;

import org.weborganic.flint.content.ContentType;

/**
 * A content type provided for convenience that corresponds to a local file.
 * 
 * @author Christophe Lauret
 * @version 2 June 2010
 */
public final class FileContentType implements ContentType {

  /**
   * Sole instance.
   */
  public static final FileContentType SINGLETON = new FileContentType();

  /**
   * Always returns "LocalFile".
   */
  @Override public String toString() {
    return "LocalFile";
  }

}