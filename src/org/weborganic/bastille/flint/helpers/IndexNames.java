/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.weborganic.bastille.flint.helpers;

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
