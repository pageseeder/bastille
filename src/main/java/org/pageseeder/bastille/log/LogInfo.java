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

import java.io.File;
import java.util.List;

import org.pageseeder.xmlwriter.XMLWritable;

/**
 * An interface centralizing all common functionalities for the log implementations.
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
