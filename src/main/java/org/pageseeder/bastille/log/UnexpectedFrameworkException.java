/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.pageseeder.bastille.log;

/**
 * Exception thrown when a logging implementation fails because a different is in use.
 *
 * <p>This is an unchecked exception because implementations would generally be used after
 * being detected.
 *
 * @author Christophe Lauret
 * @version Bastille 0.8.5 - 5 February 2013
 */
public final class UnexpectedFrameworkException extends RuntimeException {

  /** As per requirement. */
  private static final long serialVersionUID = 4746009795250556257L;

  /**
   * @param message The message
   * @param cause   And the causing error (generally a <code>ClassCastException</code>)
   */
  public UnexpectedFrameworkException(String message, Throwable cause) {
    super(message, cause);
  }

}
