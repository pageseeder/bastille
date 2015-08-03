/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.pageseeder.bastille.log;

import java.io.File;
import java.util.List;

import org.pageseeder.xmlwriter.XMLWritable;

/**
 * A interface centralizing all common functionalities for the log implementations.
 *
 * @author Christophe Lauret
 *
 * @version Bastille 0.8.6 - 6 February 2013
 * @since Bastille 0.8.5
 */
public interface LogInfo {

  /**
   * Performs any necessary initialisation.
   */
  void init();

  /**
   * Indicates whether the logging framework supports the listing of log directories.
   *
   * @return <code>true</code> if recent logs are supported;
   *         <code>false</code> otherwise.
   */
  boolean supportsListLogDirectories();

  /**
   * Returns a list of directories where log files are being stored.
   *
   * <p>Implementations must return an empty list rather than <code>null</code>.
   *
   * @return Always a list (never <code>null</code>)
   */
  List<File> listLogDirectories();

  /**
   * Indicates whether the logging framework supports the recent log functionality.
   *
   * @return <code>true</code> if recent logs are supported;
   *         <code>false</code> otherwise.
   */
  boolean supportsRecentEvents();

  /**
   * Set the threshold for the recent logs.
   *
   * @param level The threshold for keeping recent log events including that level.
   */
  void setRecentEventThreshold(LogLevel level);

  /**
   * Returns the threshold for the recent logs.
   *
   * @return The threshold for keeping recent log events including that level.
   */
  LogLevel getRecentEventThreshold();

  /**
   * Returns a list of the recent log entries as XMLWritable.
   *
   * <p>Implementations must return an empty list rather than <code>null</code>.
   *
   * @return Always a list (never <code>null</code>)
   */
  List<? extends XMLWritable> listRecentEvents();

}
