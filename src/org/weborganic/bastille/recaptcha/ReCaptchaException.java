/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.weborganic.bastille.recaptcha;

/**
 * Class of exceptions thrown by the ReCaptcha.
 *
 * @author Christophe Lauret
 * @version Bastille 0.8.4 - 1 Feb 2013
 */
public final class ReCaptchaException extends Exception {

  /** As per requirement for serializable */
  private static final long serialVersionUID = -9142801370192994186L;

  /**
   * @param message An explanation for the error
   */
  public ReCaptchaException(String message) {
    super(message);
  }

  /**
   * @param message An explanation for the error
   * @param t       The offending error
   */
  public ReCaptchaException(String message, Throwable t) {
    super(message, t);
  }

}
