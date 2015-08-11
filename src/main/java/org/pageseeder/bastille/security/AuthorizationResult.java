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
 * An enumeration for the results of authorization functions.
 *
 * @author Christophe Lauret
 * @version 0.6.2 - 8 April 2011
 * @since 0.6.2
 */
public enum AuthorizationResult {

  /**
   * The user is authorised to access the resource.
   */
  AUTHORIZED,

  /**
   * The user is not authorised to access the resource due to lack of credentials.
   * (for example, if the user is not logged in).
   */
  UNAUTHORIZED,

  /**
   * The user is not authorised to access the resource due to lack of credentials.
   * (The user is logged in, but he is not allowed to access the resource)
   */
  FORBIDDEN;

}
