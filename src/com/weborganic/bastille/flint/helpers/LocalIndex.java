/*
 * This file is part of the Bastille library.
 *
 * Available under a commercial licence, contact Weborganic.
 *
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package com.weborganic.bastille.flint.helpers;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.weborganic.flint.Index;

/**
 * A basic implementation of a local index.
 *
 * @author Christophe Lauret
 * @version 0.6.0 - 27 May 2010
 * @since 0.6.0
 */
public final class LocalIndex implements Index {

  /**
   * The location of the index.
   */
  private final File _location;

  /**
   * This index's analyser
   * If this is modified, the method toQuery in IndexMaster will have to be modified to use the correct analyser.
   */
  private final Analyzer _analyzer;

  /**
   * Create a new local index.
   *
   * @param location The location of the local index.
   */
  protected LocalIndex(File location) {
    this._location = location;
    this._analyzer = IndexMaster.getNewAnalyzer();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getIndexID() {
    return "default";
  }

  /**
   * {@inheritDoc}
   */
  @Override
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
  @Override
  public Analyzer getAnalyzer() {
    return this._analyzer;
  }

  @Override
  public String toString() {
    return this.getIndexID();
  }

}
