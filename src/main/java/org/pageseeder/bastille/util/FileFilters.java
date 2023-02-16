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
package org.pageseeder.bastille.util;

import java.io.File;
import java.io.FileFilter;

/**
 * A utility class providing some common file filters.
 *
 * @deprecated Will be remove in 0.12.0 (unused)
 *
 * @author Christophe Lauret
 * @version Bastille 0.6.7
 */
@Deprecated
public final class FileFilters {

  /**
   * Only accepts folders (<code>isDirectory</code> returns <code>true</code>)
   */
  private static final FileFilter FOLDERS = d -> d.isDirectory();

  /**
   * Only accepts XML files (ending with ".xml")
   */
  private static final FileFilter XML = f -> f.isFile() && f.getName().endsWith(".xml");

  /**
   * Only accepts XSLT files (ending with ".xsl")
   */
  private static final FileFilter XSLT = f -> f.isFile() && f.getName().endsWith(".xsl");

  /**
   * Utility class.
   */
  private FileFilters() {
  }

  /**
   * @return A file filter which only accepts folders (<code>isDirectory</code> returns <code>true</code>)
   */
  public static FileFilter getFolders() {
    return FOLDERS;
  }

  /**
   * @return A file filter which only accepts XML files (ending with ".xml")
   */
  public static FileFilter getXMLFiles() {
    return XML;
  }

  /**
   * @return A file filter which only accepts XSLT files (ending with ".xsl")
   */
  public static FileFilter getXSLTFiles() {
    return XSLT;
  }

}
