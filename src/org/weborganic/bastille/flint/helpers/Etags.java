/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.weborganic.bastille.flint.helpers;

import java.io.File;
import java.io.FileFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weborganic.bastille.flint.config.FlintConfig;
import org.weborganic.flint.local.LocalIndex;

/**
 * A utility class to generate etags for the generators.
 *
 * @author Christophe Lauret
 *
 * @version 0.8.6 - 8 February 2013
 * @since 0.8.6
 */
public final class Etags {

  /**
   * Logger for debugging
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(Etags.class);

  /** Utility class. */
  private Etags() {
  }

  /**
   * To list only folders
   */
  private static final FileFilter FOLDERS_ONLY = new FileFilter() {
    @Override
    public boolean accept(File d) {
      return d.isDirectory();
    }
  };

  /**
   * Returns the etag for the given index name based on the last modified date of the index.
   *
   * <p>If there is a single index, the name is ignored.
   *
   * <p>If there are multiple indexes, no name is
   *
   * @param name The name of the index (optional)
   *
   * @return The corresponding etag
   */
  public static String getETag(String name) {
    StringBuilder etag = new StringBuilder();
    File root = FlintConfig.directory();
    if (FlintConfig.hasMultiple()) {
      // Multiple index
      if (name != null && IndexNames.isValid(name)) {
        long modified = LocalIndex.getLastModified(new File(root, name));
        etag.append(name).append('-').append(modified);
      } else {
        File[] indexes = root.listFiles(FOLDERS_ONLY);
        if (indexes != null) {
          for (File index : indexes) {
            long modified = LocalIndex.getLastModified(index);
            etag.append(name).append('-').append(modified);
          }
        }
      }
    } else {
      // Single index
      long modified = LocalIndex.getLastModified(root);
      etag.append(root.getName()).append('-').append(modified);
    }
    return etag.length() > 0? etag.toString() : null;
  }

}
