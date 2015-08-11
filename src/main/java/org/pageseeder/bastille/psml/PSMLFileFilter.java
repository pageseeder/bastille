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
package org.pageseeder.bastille.psml;

import java.io.File;
import java.io.FileFilter;

/**
 * Filters files which extension is ".psml".
 *
 * @author Christophe Lauret
 * @version 6 October 2012
 */
public final class PSMLFileFilter implements FileFilter {

  /**
   * Creates new file filter.
   */
  public PSMLFileFilter() {
  }

  @Override
  public boolean accept(File file) throws NullPointerException {
    if (file == null) throw new NullPointerException("The specified file is null.");
    String name = file.getName();
    int dot = name.lastIndexOf('.');
    if (dot == -1) return false;
    String ext = name.substring(dot + 1);
    return "psml".equals(ext);
  }
}
