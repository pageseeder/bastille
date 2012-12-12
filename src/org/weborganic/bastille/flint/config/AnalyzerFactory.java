/*
 * This file is part of the Bastille library.
 *
 * Available under a commercial licence, contact Weborganic.
 *
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package org.weborganic.bastille.flint.config;

import org.apache.lucene.analysis.Analyzer;

/**
 * This class provides an Analyzer used to index and create search queries.
 *
 * @author Christophe Lauret
 * @version 19 October 2012
 */
public interface AnalyzerFactory {

  /**
   * @return A new Analyzer used to index and create search queries.
   */
  Analyzer getAnalyzer();

}
