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
 * A logging level common to all frameworks for use by local interfaces.
 *
 * @author Christophe Lauret
 * @version Bastille 0.8.6 - 6 February 2013
 */
public enum LogLevel {

  /** For Debugging. */
  DEBUG,

  /** Default level. */
  INFO,

  /** For warnings. */
  WARN,

  /** for errors. */
  ERROR,

  /** To disable/ignore all levels. */
  OFF

}
