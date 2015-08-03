/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.pageseeder.bastille.security;

import java.security.Principal;

import org.pageseeder.xmlwriter.XMLWritable;

/**
 * An interface to represent a user.
 *
 * <p>To ease interoperability, the XML returned should match:
 * <pre>{@code <user type="[type]"> ... </user>}</pre>
 *
 * <p>All <code>User</code> implementations must be {@link java.io.Serializable}.
 *
 * @author Christophe Lauret
 * @version 0.6.2
 * @since 0.6.2
 */
public interface User extends XMLWritable, Principal {

  /**
   * @return the name of the user.
   */
  @Override
  String getName();

}
