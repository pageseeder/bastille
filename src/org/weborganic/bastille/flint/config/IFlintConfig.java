/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.weborganic.bastille.flint.config;

import java.io.File;

import org.apache.lucene.document.Document;
import org.weborganic.flint.IndexConfig;

/**
 * An interface for the flint configurations.
 *
 * @author Christophe Lauret
 * @version 19 October 2012
 */
public interface IFlintConfig {

  /**
   * @return the directory containing the index.
   */
  File getDirectory();

  /**
   * @param mediatype The media type to match.
   *
   * @return the templates to generate iXML.
   */
  File getIXMLTemplates(String mediatype);

  /**
   * Indicates whether flint is configured for multiple indexes.
   *
   * @return <code>true</code> if Flint is configured for multiple indexes;
   *         <code>false</code> for a single index.
   */
  boolean hasMultiple();

  /**
   * Configures the IndexConfiguration from Flint.
   *
   * @param iconfig the index configuration.
   */
  void configure(IndexConfig iconfig);

  /**
   * Returns the file corresponding to the specified document.'
   *
   * <p>This method is used to determine whether changes in some files require reindexing and is typically used
   * by the indexing process.
   *
   * @param doc The Lucene document
   *
   * @return The file that was used to generate it.
   */
  File toFile(Document doc);

  /**
   * Returns the value of the path field for the specified file.
   *
   * @param f for the specified file.
   * @return the corresponding path or "" if an error occurs
   */
  String toPath(File f);

}
