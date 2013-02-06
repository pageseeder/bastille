/*
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package org.weborganic.bastille.flint.helpers;

import org.slf4j.Logger;
import org.weborganic.flint.IndexJob;
import org.weborganic.flint.log.FlintListener;

/**
 * A Flint listener which reports less events than the default.
 *
 * @author Christophe Lauret
 *
 * @version 0.8.6 - 6 February 2013
 * @since 0.8.6
 */
public final class QuietListener implements FlintListener {

  /**
   * The format string used for all SLF4J.
   */
  private static final String FORMAT_STRING = "{} [Job:{}]";

  /**
   * Sole instance.
   */
  private final Logger _logger;

  /**
   * Creates a new logger for the specified Logger.
   *
   * @param logger The underlying logger to use.
   */
  public QuietListener(Logger logger) {
    this._logger = logger;
  }

  @Override
  public void debug(String message) {
    this._logger.debug(message);
  }

  @Override
  public void debug(String debug, Throwable throwable) {
    this._logger.debug(debug, throwable);
  }

  @Override
  public void info(String message) {
    this._logger.info(message);
  }

  @Override
  public void info(String info, Throwable throwable) {
    this._logger.info(info, throwable);
  }

  @Override
  public void warn(String message) {
    this._logger.warn(message);
  }

  @Override
  public void warn(String warn, Throwable throwable) {
    this._logger.warn(warn, throwable);
  }

  @Override
  public void error(String message) {
    error(message, null);
  }

  @Override
  public void error(String message, Throwable throwable) {
    this._logger.error(message, throwable);
  }

  @Override
  public void debug(IndexJob job, String message) {
    this._logger.debug(FORMAT_STRING, message, job.toString());
  }

  @Override
  public void debug(IndexJob job, String message, Throwable throwable) {
    this._logger.debug(FORMAT_STRING, message, job.toString());
    this._logger.debug(message, throwable);
  }

  @Override
  public void info(IndexJob job, String message) {
    this._logger.info(FORMAT_STRING, message, job.toString());
  }

  @Override
  public void info(IndexJob job, String message, Throwable throwable) {
    this._logger.info(FORMAT_STRING, message, job.toString());
    this._logger.info(message, throwable);
  }

  @Override
  public void warn(IndexJob job, String message) {
    this._logger.warn(FORMAT_STRING, message, job.toString());
  }

  @Override
  public void warn(IndexJob job, String message, Throwable throwable) {
    this._logger.warn(FORMAT_STRING, message, job.toString());
    this._logger.warn(message, throwable);
  }

  @Override
  public void error(IndexJob job, String message) {
    this._logger.error(FORMAT_STRING, message, job.toString());
  }

  @Override
  public void error(IndexJob job, String message, Throwable throwable) {
    this._logger.error(FORMAT_STRING, message, job.toString());
    this._logger.error(message, throwable);
  }

  @Override
  public void startJob(IndexJob job) {
    if (this._logger.isDebugEnabled()) {
      this._logger.info("Starting [Job:{}]", job.toString());
    }
  }

  @Override
  public void finishJob(IndexJob job) {
    if (this._logger.isDebugEnabled()) {
      this._logger.info("Done! [Job:{}]", job.toString());
    }
  }

}
