package org.weborganic.bastille.log;

import java.io.File;
import java.util.List;

import com.topologi.diffx.xml.XMLWritable;

/**
 * A interface centralizing all common functionalities for the log implementations.
 *
 * @author Christophe Lauret
 * @version Bastille 0.8.5 - 5 February 2013
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
   * Returns a list of the recent log entries as XMLWritable.
   *
   * <p>Implementations must return an empty list rather than <code>null</code>.
   *
   * @return Always a list (never <code>null</code>)
   */
  List<? extends XMLWritable> listRecentEvents();

}
