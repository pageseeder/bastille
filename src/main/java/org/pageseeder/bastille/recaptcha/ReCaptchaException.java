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
