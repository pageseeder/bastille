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
package org.pageseeder.bastille.flint.config;

import java.io.File;
import java.util.List;

import org.apache.lucene.document.Document;
import org.pageseeder.flint.IndexConfig;

/**
 * An interface for the flint configurations.
 *
 * <p>This API is used by Bastille to know how it should use Flint.
 *
 * <p>Implement this interface, if you're using a non-standard configuration.
 *
 * @author Christophe Lauret
 * @version 28 February 2013
 */
public interface IFlintConfig {

  /**
   * @return the root directory containing the index or indexes.
   */
  File getDirectory();

  /**
   * Indicates whether flint is configured for multiple indexes.
   *
   * @return <code>true</code> if Flint is configured for multiple indexes;
   *         <code>false</code> for a single index.
   */
  boolean hasMultiple();

  /**
   * @return the list of templates to generate iXML.
   */
  List<File> getTemplates();

  /**
   * @return the list of index names.
   */
  List<String> getIndexNames();

  /**
   * @param name The name of the index
   * @return the index configuration for the specified index.
   */
  IndexConfig getIndexConfig(String name);

  /**
   * Causes the config to be reloaded.
   */
  void reload();

  /**
   * Returns the file corresponding to the specified document.
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
