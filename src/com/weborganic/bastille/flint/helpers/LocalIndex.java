package com.weborganic.bastille.flint.helpers;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.weborganic.flint.Index;

/**
 * Represents the local index. 
 * 
 * @author Christophe Lauret
 * @version 27 May 2010
 */
public final class LocalIndex implements Index {

  /**
   * The location of the index.
   */
  private final File _location;

  /**
   * Create a new local index.
   * 
   * @param location The location of the local index.
   */
  protected LocalIndex(File location) {
    this._location = location;
  }

  /**
   * {@inheritDoc}
   */
  public String getIndexID() {
    return "default";
  }

  /**
   * {@inheritDoc}
   */
  public Directory getIndexDirectory() {
    try {
      if (!this._location.exists())
        this._location.mkdirs();
      return FSDirectory.open(this._location);
    } catch (IOException ex) {
      ex.printStackTrace();
      return null;
    }
  }

  /**
   * {@inheritDoc}
   */
  public Analyzer getAnalyzer() {
    return new StandardAnalyzer(Version.LUCENE_30);
  }

  @Override
  public String toString() {
    return this.getIndexID();
  }

}
