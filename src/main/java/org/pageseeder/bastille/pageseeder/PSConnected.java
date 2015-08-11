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
package org.pageseeder.bastille.pageseeder;

/**
 * Use this annotation to indicate whether a generator will connect to PageSeeder.
 *
 * <p>This annotation has no effect on the annotated class.
 *
 * @author Christophe Lauret
 * @version 0.6.8 - 8 June 2011
 * @since 0.6.8
 */
public @interface PSConnected {

  /**
   * Indicates whether the connection requires the user to be connected.
   *
   * <p>Only set to <code>true</code> if a PageSeeder User is required in the session. If the
   * connection requires a PageSeeder user but is set by the generator, that is does not require
   * to be stored in the session, this flag should be set to <code>false</code>.
   *
   * <p>Defaults to <code>false</code>.
   */
  boolean login() default false;

}
