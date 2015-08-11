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

import org.pageseeder.flint.IndexJob;
import org.pageseeder.flint.api.IndexListener;
import org.slf4j.Logger;

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
