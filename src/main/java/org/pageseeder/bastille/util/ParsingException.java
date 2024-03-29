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

/**
 * Class of exceptions occurring while minimising content.
 *
 * @deprecated Will be remove in 0.12.0 (now part of Berlioz bundler)
 *
 * @author Christophe Lauret
 * @version 17 February 2012
 */
@Deprecated
public class ParsingException extends Exception {

  /**
   * As per requirement for the Serializable interface.
   */
  private static final long serialVersionUID = -8753921226709324155L;

  /**
   * The line number.
   */
  private final int _line;

  /**
   * The column number.
   */
  private final int _column;

  /**
   * Creates a new minimizer exception.
   *
   * @param message The message.
   * @param line    The line number.
   * @param column  The column number.
   */
  public ParsingException(String message, int line, int column) {
    super(message);
    this._line = line;
    this._column = column;
  }

  /**
   * @return The affected column number or -1 if unknown.
   */
  public final int getColumn() {
    return this._column;
  }

  /**
   * @return The affected line number or -1 if unknown.
   */
  public final int getLine() {
    return this._line;
  }

  @Override
  public String getMessage() {
    return super.getMessage()+" at line "+this._line+" and column "+this._column;
  }
}
