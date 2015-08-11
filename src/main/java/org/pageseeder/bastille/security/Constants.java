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
package org.pageseeder.bastille.security;

/**
 * A collection of constants related to security.
 *
 * @author Christophe Lauret
 * @version 0.6.2 - 7 April 2011
 * @since 0.6.2
 */
public final class Constants {

  /** Utility class */
  private Constants() {
  }

  /**
   * The name of the attribute that contains the User currently logged in.
   */
  public static final String SESSION_USER_ATTRIBUTE = "org.pageseeder.bastille.security.User";

  /**
   * The name of the attribute that contains the request to a protected resource.
   */
  public static final String SESSION_REQUEST_ATTRIBUTE = "org.pageseeder.bastille.security.HttpRequest";

}
