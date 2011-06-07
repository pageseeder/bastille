/*
 * Copyright (c) 2011 weborganic systems pty. ltd.
 */
package com.weborganic.bastille.flint.helpers;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Filter to only select files that have been modified since a specified date.
 * 
 * @author Christophe Lauret
 * @version 0.6.0 - 2 June 2010
 * @since 0.6.0
 */
public final class IndexUpdateFilter implements FileFilter {

  /**
   * Possible action for each file.
   */
  public enum Action {INSERT, UPDATE, DELETE, IGNORE};

  /**
   * Files modified after this date will be accepted.
   */
  private final long _since;

  /**
   * Maps the canonical path to each file instance.
   */
  private final Map<String, File> _files = new HashMap<String, File>();

  /**
   * The list of files to consider
   */
  private final Map<String, Action> _actions = new HashMap<String, Action>();

  /**
   * Creates a new filter.
   * 
   * @param since The date form which files are included.
   */
  public IndexUpdateFilter(long since) {
    this._since = since;
  }

  /**
   * Creates a new filter.
   * 
   * @param since The date form which files are included.
   */
  public IndexUpdateFilter(long since, List<File> indexed) throws IOException {
    this._since = since;
    System.err.println("INIT FILTER: ! "+indexed.size());
    for (File i : indexed){
      String path = i.getCanonicalPath();
      System.err.println("INIT FILTER: "+path);
      this._files.put(path, i);
      this._actions.put(path, Action.DELETE);
    }
  }

  /**
   * Accepts only files modified after the date.
   * 
   * {@inheritDoc}
   */
  public boolean accept(File f) {
    try {
      String canonical = f.getCanonicalPath();
      // A file already indexed: UPDATE or IGNORE
      if (this._files.containsKey(canonical)) {
        if (f.lastModified() > this._since) {
          this._actions.put(canonical, Action.UPDATE);
        } else {
          this._actions.put(canonical, Action.IGNORE);          
        }

      // A new file: register and INSERT
      } else if (!f.isDirectory()) {
        this._files.put(canonical, f);
        this._actions.put(canonical, Action.INSERT);
      }
    } catch (IOException ex) {
      ex.printStackTrace();
    }
    return true;
  }

  /**
   * Returns the list of deletable files after this filter has been used.
   * 
   * @return the list of deletable files.
   */
  public List<File> getDeletable() {
    List<File> deletable = new ArrayList<File>();
    for (Entry<String, Action> e :  this._actions.entrySet()) {
      if (e.getValue() == Action.DELETE)
        deletable.add(this._files.get(e.getKey()));
    }
    return deletable;
  }

  /**
   * Returns the list of deletable files after this filter has been used.
   * 
   * @return the list of deletable files.
   */
  public List<File> getIndexable() {
    List<File> indexable = new ArrayList<File>();
    for (Entry<String, Action> e :  this._actions.entrySet()) {
      if (e.getValue() != Action.IGNORE)
        indexable.add(this._files.get(e.getKey()));
    }
    return indexable;
  }

  /**
   * Returns a map giving the action for each file.
   * 
   * @return a map giving the action for each file.
   */
  public Map<File, Action> getActions() {
    Map<File, Action> indexable = new HashMap<File, Action>(this._actions.size());
    for (Entry<String, Action> e :  this._actions.entrySet()) {
      indexable.put(this._files.get(e.getKey()), e.getValue());
    }
    return indexable;
  }

}
