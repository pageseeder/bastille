/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.weborganic.bastille.psml;

import java.io.File;
import java.io.FileFilter;

/**
 * Filters files which extension is ".psml".
 *
 * @author Christophe Lauret
 * @version 6 October 2012
 */
public final class PSMLFileFilter implements FileFilter {

  /**
   * Creates new file filter.
   */
  public PSMLFileFilter() {
  }

  @Override
  public boolean accept(File file) throws NullPointerException {
    if (file == null) throw new NullPointerException("The specified file is null.");
    String name = file.getName();
    int dot = name.lastIndexOf('.');
    if (dot == -1) return false;
    String ext = name.substring(dot + 1);
    return "psml".equals(ext);
  }
}