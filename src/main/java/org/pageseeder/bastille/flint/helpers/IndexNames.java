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

/**
 * A utility class to handle index names.
 *
 * @author Christophe Lauret
 * @version 0.8.9 - 25 February 2013
 * @since 0.8.9
 */
public final class IndexNames {

  /**
   * The maximum length of the filename for an index.
   */
  private static final int MAX_INDEX_NAME_LENGTH = 255;

  /** Utility class */
  private IndexNames() {
  }

  /**
   * Indicates whether this specified name is a valid index name.
   *
   * <p>A valid name can only contain the following characters:
   * <ul>
   *   <li><code>[A-Z]</code></li>
   *   <li><code>[a-z]</code></li>
   *   <li><code>[0-9]</code></li>
   *   <li><code>$</code></li>
   *   <li><code>@</code></li>
   *   <li><code>-</code></li>
   *   <li><code>_</code></li>
   *   <li><code>.</code></li>
   * </ul>
   *
   * @param name The name of the index.
   * @return <code>true</code> if the name is considered valid and can be used as an index name;
   *         <code>false</code> otherwise.
   */
  public static boolean isValid(String name) {
    if (name == null) return false;
    if (name.length() > MAX_INDEX_NAME_LENGTH) return false;
    return name.matches("[\\w\\-\\@\\$\\.]+");
  }

}
