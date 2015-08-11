/*
 * Copyright 2015 Allette Systems (Australia)
 * http://www.allette.com.au
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pageseeder.bastille.flint.helpers;

import java.io.File;
import java.io.FileFilter;

import org.pageseeder.bastille.flint.config.FlintConfig;
import org.pageseeder.flint.local.LocalIndex;

/**
 * A utility class to generate etags for the generators.
 *
 * @author Christophe Lauret
 *
 * @version 0.8.6 - 8 February 2013
 * @since 0.8.6
 */
public final class Etags {

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
            etag.append(index.getName()).append('-').append(modified);
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
