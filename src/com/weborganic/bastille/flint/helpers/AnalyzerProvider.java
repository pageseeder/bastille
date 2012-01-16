/**
 * 
 */
package com.weborganic.bastille.flint.helpers;

import org.apache.lucene.analysis.Analyzer;

/**
 * This class provides an Analyzer used to index and create search queries.
 * 
 * @author Jean-Baptiste Reure
 * @version 13 January 2012
 *
 */
public interface AnalyzerProvider {

  /**
   * @return A new Analyzer used to index and create search queries.
   */
  public Analyzer getAnalyzer();
  
}
