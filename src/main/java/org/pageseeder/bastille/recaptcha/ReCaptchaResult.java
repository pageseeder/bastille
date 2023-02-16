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
package org.pageseeder.bastille.recaptcha;

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
 * @deprecated The ReCaptcha API is no longer accessible
 *
 * @author Christophe Lauret
 * @version 1 February 2013
 */
@Deprecated
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
    if (response == null) return new ReCaptchaResult(false, "recaptcha-no-response");

    // Let's parse
    String[] a = response.split("\r?\n");
    if (a.length < 1) return new ReCaptchaResult(false, "recaptcha-empty-response");
    boolean valid = "true".equals(a[0]);
    String message = null;
    if (!valid) {
      if (a.length > 1) {
        message = a[1];
      } else {
        message = "recaptcha-no-message";
      }
    }

    return new ReCaptchaResult(valid, message);
  }

}
