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
package org.pageseeder.bastille.flint.helpers;

import java.io.File;

import org.pageseeder.bastille.flint.config.IFlintConfig;
import org.pageseeder.berlioz.util.FileUtils;
import org.pageseeder.flint.api.Content;
import org.pageseeder.flint.content.DeleteRule;
import org.pageseeder.flint.local.LocalFileContent;


/**
 * Content from a file.
 *
 * <p>This class provides a basic implementations of the {@link Content} interface for use by Bastille.
 *
 * @author Christophe Lauret
 * @version 0.7.3 - 17 October 2012
 * @since 0.6.0
 */
public final class FileContent extends LocalFileContent implements Content {

  /**
   * The flint configuration used for this file.
   */
  private final IFlintConfig _config;

  /**
   * Creates a new content from a given file.
   *
   * @param f      The file
   * @param config The flint configuration for this file.
   */
  public FileContent(File f, IFlintConfig config) {
    super(f);
    this._config = config;
  }

  @Override
  public String getMediaType() {
    return FileUtils.getMediaType(file());
  }

  /**
   * Returns a delete rule based on the path.
   *
   * {@inheritDoc}
   */
  @Override
  public DeleteRule getDeleteRule() {
    return new DeleteRule("path", this._config.toPath(file()));
  }

}
