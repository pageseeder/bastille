/*
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package com.weborganic.bastille.util;

/**
 * Class of exceptions occurring while minimising content.
 *
 * @author Christophe Lauret
 * @version 17 February 2012
 */
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
