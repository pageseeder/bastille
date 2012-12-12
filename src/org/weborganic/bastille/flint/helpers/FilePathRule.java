/*
 * This file is part of the Bastille library.
 *
 * Available under a commercial licence, contact Weborganic.
 *
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package org.weborganic.bastille.flint.helpers;

import java.io.File;

import org.apache.lucene.document.Document;
import org.weborganic.bastille.flint.config.LegacyConfig;


/**
 * This class is used to map the path to the actual indexed file.
 *
 * <p>This version uses a simple mapping between the path and the source file.
 *
 * <p>Files are divided in two groups using the visibility field:
 * - private: all files within the "WEB-INF/xml" folder
 * - public: any file directly accessible via the Web
 *
 * @deprecated Use <code>LegacyConfig</code> or the appropriate <code>FlintConfig</code> instead.
 *
 * @author Christophe Lauret
 * @version 0.7.4 - 18 October 2012
 * @since 0.6.0
 */
@Deprecated
public final class FilePathRule {

  /**
   * Utility class.
   */
  private FilePathRule() {
  }

  /**
   * Returns the relative web path for the specified file.
   *
   * <p>This method returns:
   * <ul>
   *   <li>for private files, the relative path from the '/WEB-INF/xml' without the '.xml' extension.
   *   <li>for public files, the relative path from the Web application root.
   * </ul>
   *
   * @deprecated Use {@link LegacyConfig#toPath(File)} instead
   *
   * @param f for the specified file.
   * @return the corresponding path or "" if an error occurs
   */
  @Deprecated
  public static String toPath(File f) {
    return LegacyConfig.asPath(f);
  }

  /**
   * Returns the file corresponding to the specified Lucene document.
   *
   * <p>This method looks at the field named "visibility" to determine whether it is a public or
   * private file.
   *
   * @deprecated Use {@link LegacyConfig#toFile(Document)} instead
   *
   * @param doc The Lucene document.
   * @return The corresponding file.
   */
  @Deprecated
  public static File toFile(Document doc) {
    return LegacyConfig.asFile(doc);
  }
}
