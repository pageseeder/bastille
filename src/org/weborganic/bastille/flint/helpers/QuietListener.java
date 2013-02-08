/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.weborganic.bastille.flint.helpers;

import org.slf4j.Logger;
import org.weborganic.flint.IndexJob;
import org.weborganic.flint.IndexListener;

/**
 * A Flint listener which reports less events than the default.
 *
 * @author Christophe Lauret
 *
 * @version 0.8.6 - 6 February 2013
 * @since 0.8.6
 */
public final class QuietListener implements IndexListener {

  /**
   * The format string used for all SLF4J.
   */
  private static final String FORMAT_STRING = "{} [Job:{}]";

  /**
   * Sole instance.
   */
  private final Logger _logger;

  /**
   * When the last batch started.
   */
  private long _started = 0;

  /**
   * The number of indexed documents
   */
  private volatile int _indexed = 0;

  /**
   * Creates a new logger for the specified Logger.
   *
   * @param logger The underlying logger to use.
   */
  public QuietListener(Logger logger) {
    this._logger = logger;
  }

  @Override
  public void startJob(IndexJob job) {
    this._logger.debug("Started {}", job);
  }

  @Override
  public void warn(IndexJob job, String message) {
    this._logger.warn(FORMAT_STRING, message, job.toString());
  }

  @Override
  public void error(IndexJob job, String message, Throwable throwable) {
    this._logger.error(FORMAT_STRING, message, new Object[]{job, throwable});
  }

  @Override
  public void endJob(IndexJob job) {
    this._indexed++;
    this._logger.debug("Finished {}", job);
  }

  @Override
  public void startBatch() {
    this._logger.info("Started indexing documents.");
    this._started = System.nanoTime();
  }

  @Override
  public void endBatch() {
    this._logger.info("Indexed {} documents in {} ms", this._indexed, (System.nanoTime() - this._started) / 1000000);
    this._indexed = 0;
  }

}
