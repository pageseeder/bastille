/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
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
  OFF;

}
