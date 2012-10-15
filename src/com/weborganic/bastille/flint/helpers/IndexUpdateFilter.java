/*
 * This file is part of the Bastille library.
 *
 * Available under a commercial licence, contact Weborganic.
 *
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
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
   * The default filter to use when no filter is specified.
   *
   * <p>Ignores hidden files and files starting with "."
   */
  private static final FileFilter DEFAULT_FILTER = new FileFilter() {

    @Override
    public boolean accept(File f) {
      return !f.isHidden() & !f.getName().startsWith(".");
    }
  };

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
   * Used to filter out the files not supposed to be indexed.
   */
  private final FileFilter _innerFilter;

  /**
   * Creates a new filter.
   *
   * @param since The date form which files are included.
   */
  public IndexUpdateFilter(long since) {
    this._since = since;
    this._innerFilter = DEFAULT_FILTER;
  }

  /**
   * Creates a new filter.
   *
   * @param since   The date form which files are included.
   * @param indexed The list of files to process.
   */
  public IndexUpdateFilter(long since, List<File> indexed) throws IOException {
    this._since = since;
    for (File i : indexed) {
      String path = i.getCanonicalPath();
      this._files.put(path, i);
      this._actions.put(path, Action.DELETE);
    }
    this._innerFilter = DEFAULT_FILTER;
  }

  /**
   * Creates a new filter.
   *
   * @param since   The date form which files are included.
   * @param indexed The list of files to process.
   * @param filter  Used to filter out the files not supposed to be indexed.
   */
  public IndexUpdateFilter(long since, List<File> indexed, FileFilter filter) throws IOException {
    this._since = since;
    for (File i : indexed) {
      String path = i.getCanonicalPath();
      this._files.put(path, i);
      this._actions.put(path, Action.DELETE);
    }
    this._innerFilter = filter != null? filter : DEFAULT_FILTER;
  }

  /**
   * Accepts only files modified after the date.
   *
   * {@inheritDoc}
   */
  @Override
  public boolean accept(File f) {
    // make sure this file is supposed to be indexed
    if (this._innerFilter != null && !this._innerFilter.accept(f))
      return false;
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
