package com.weborganic.bastille.psml;

import java.io.File;

/**
 *
 * @author Christophe Lauret
 * @version 6 October 2012
 */
public final class PSMLFile {

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
   * @param path
   * @param file
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
