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
import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weborganic.bastille.flint.config.FlintConfig;

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
    try {
      File index = FlintConfig.directory();
      FSDirectory directory = FSDirectory.open(index);
      if (IndexReader.indexExists(directory)) {
        long modified = IndexReader.lastModified(directory);
        etag.append(index.getName()).append('-').append(modified);
      } else {
        if (name != null) {
          FSDirectory fsd = FSDirectory.open(index);
          if (IndexReader.indexExists(fsd))
            etag.append(name).append('-').append(IndexReader.lastModified(fsd));
        } else {
          File[] indexes = index.listFiles(FOLDERS_ONLY);
          if (indexes != null) {
            for (File indexDir : indexes) {
              FSDirectory fsd = FSDirectory.open(index);
              if (IndexReader.indexExists(fsd))
                etag.append(indexDir.getName()).append('-').append(IndexReader.lastModified(fsd));
            }
          }
        }
      }
      // TODO Close directories??
    } catch (IOException ex) {
      LOGGER.debug("Error while trying to get last modified date of index", ex);
    }
    return etag.length() > 0? etag.toString() : null;
  }

  /**
   * Returns the last modified date of the specified index.
   *
   *
   * @param name The name of the index (optional)
   *
   * @return The corresponding etag

  public static long getLastModified(String name) {
    long modified = -1;
    try {
      File index = FlintConfig.directory();
      FSDirectory directory = FSDirectory.open(index);
      if (IndexReader.indexExists(directory)) {
        modified = IndexReader.lastModified(directory);
      }
      directory.close();
    } catch (IOException ex) {
      LOGGER.debug("Error while trying to get last modified date of index", ex);
    }
    return modified;
  }
   */
}
