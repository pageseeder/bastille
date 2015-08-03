/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.pageseeder.bastille.psml;

import java.io.File;
import java.io.Serializable;

/**
 * A path/file value pair for a PSML document.
 *
 * @author Christophe Lauret
 * @version 20 November 2012
 */
public final class PSMLFile implements Serializable {

  /**
   * As required by the <code>Serializable</code> interface.
   */
  private static final long serialVersionUID = -5680591958703581444L;

  /**
   * The path to the file from the PSML root folder.
   */
  private final String _path;

  /**
   * The actual file.
   */
  private final File _file;

  /**
   * Creates a new PSML File.
   *
   * @param path The path to the file from the root directory
   * @param file The actual file corresponding to this path.
   */
  protected PSMLFile(String path, File file) {
    this._path = path;
    this._file = file;
  }

  /**
   * The path to the file from the PSML root folder.
   *
   * @return The path to the file from the PSML root folder.
   */
  public String path() {
    return this._path;
  }

  /**
   * The files corresponding to the path.
   *
   * @return The actual file.
   */
  public File file() {
    return this._file;
  }


  /**
   * Returns the base path for the specified PSML.
   *
   * <p>The base path is the path to the folder for the given PSML file.
   *
   * @return the base path for the specified PSML folder
   *
   * @throws NullPointerException if the psml folder is <code>null</code>.
   */
  public String getBase() {
    return "/" + this._path.substring(0, this._path.length() - this._file.getName().length());
  }

  /**
   * Indicates whether this resource exists.
   *
   * @return <code>true</code> if the file is not <code>null</code> and exists on the file system;
   *         <code>false</code> otherwise.
   */
  public boolean exists() {
    return this._file != null && this._file.exists();
  }

  @Override
  public String toString() {
    return this._path+(exists()? " OK" : "!NOT_FOUND");
  }

}
