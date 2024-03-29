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
package org.pageseeder.bastille.log;

/**
 * Exception thrown when a logging implementation fails because a different is in use.
 *
 * <p>This is an unchecked exception because implementations would generally be used after
 * being detected.
 *
 * @author Christophe Lauret
 * @version Bastille 0.8.5
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
