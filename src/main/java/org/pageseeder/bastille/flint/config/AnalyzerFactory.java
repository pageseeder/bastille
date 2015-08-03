/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.pageseeder.bastille.flint.config;

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
