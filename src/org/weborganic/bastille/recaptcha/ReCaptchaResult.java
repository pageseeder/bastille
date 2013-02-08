/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.weborganic.bastille.recaptcha;

/**
 * Returns the response from a ReCaptcha verification API call.
 *
 * <p>The fields correspond to the response returned by the verification API.
 *
 * <p>The messages are subject to change, and may be:
 * <ul>
 *   <li><code>invalid-site-public-key</code></li>
 *   <li><code>invalid-site-private-key</code></li>
 *   <li><code>invalid-request-cookie</code></li>
 *   <li><code>incorrect-captcha-sol</code></li>
 *   <li><code>verify-params-incorrect</code></li>
 *   <li><code>recaptcha-not-reachable</code></li>
 * </ul>
 *
 * @see <a href="https://developers.google.com/recaptcha/docs/verify">ReCaptcha - Verify</a>
 *
 * @author Christophe Lauret
 * @version 1 February 2013
 */
public final class ReCaptchaResult {

  /**
   * Indicates whether the challenge was passed.
   *
   * <p>The according to the verification API server
   */
  private final boolean _valid;

  /**
   * The error message returned by the server (<code>null</code> if OK)
   */
  private final String _message;

  /**
   * Create a new reCaptcha response.
   *
   * @param valid   If the response is marked as valid by the server
   * @param message Any accompanying message
   */
  protected ReCaptchaResult(boolean valid, String message) {
    this._valid = valid;
    this._message = message;
  }

  /**
   * Indicates whether the challenge was passed.
   *
   * <p>The according to the verification API server
   *
   * @return <code>true</code> if challenge was passed; <code>false</code> otherwise.
   */
  public boolean isValid() {
    return this._valid;
  }

  /**
   * The reCaptcha error message.
   *
   * @return The reCaptcha error message returned by the server.
   */
  public String message() {
    return this._message;
  }


  /**
   * Parses the response from the reCaptcha server.
   *
   * @param response The response returned by the server
   *
   * @return the corresponding response object
   */
  public static ReCaptchaResult parse(String response) {
    // No response from server?
    if (response == null) {
      return new ReCaptchaResult(false, "recaptcha-no-response");
    }

    // Let's parse
    String[] a = response.split("\r?\n");
    if (a.length < 1) {
      return new ReCaptchaResult(false, "recaptcha-empty-response");
    }
    boolean valid = "true".equals(a[0]);
    String message = null;
    if (!valid) {
      if (a.length > 1)
        message = a[1];
      else
        message = "recaptcha-no-message";
    }

    return new ReCaptchaResult(valid, message);
  }

}
