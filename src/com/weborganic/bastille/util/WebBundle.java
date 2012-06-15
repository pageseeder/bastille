/*
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package com.weborganic.bastille.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.weborganic.berlioz.util.ISO8601;
import org.weborganic.berlioz.util.MD5;

/**
 * A bundle of files to serve.
 *
 * @author Christophe Lauret
 * @version 17 February 2012
 */
public final class WebBundle {

  /**
   * The type of bundling required.
   */
  public enum Type {

    /** For JavaScript (simple concatenation). */
    SCRIPT(".js"),

    /** For CSS Styles (expands imports rules). */
    STYLE(".css");

    /** extension corresponding to this type. */
    private final String _ext;

    /**
     * Creates a new type for the specified extension.
     *
     * @param ext The extension.
     */
    private Type(String ext) {
      this._ext = ext;
    }

    /**
     * @param ext The extension to match.
     * @return <code>true</code> if the argument matches the extension; <code>false</code> otherwise.
     */
    boolean matches(String ext) {
      return this._ext.equalsIgnoreCase(ext);
    }

  }

  /**
   * The name of the bundle.
   */
  private final String _name;

  /**
   * The id of this bundle.
   */
  private final String _id;

  /**
   * The list of  of the bundle.
   */
  private final List<File> _files;

  /**
   * The name of the bundle.
   */
  private final List<File> _imported;

  /**
   * Whether the bundle is minimized.
   */
  private boolean _minimized;

  /**
   * The hash values for this bundle.
   */
  private volatile String _etag;

  /**
   * Creates a new bundles of files.
   *
   * @param name      The name of the bundle.
   * @param files     The files to bundle.
   * @param minimized Whether the files are minimized.
   */
  public WebBundle(String name, List<File> files, boolean minimized) {
    this._name = name;
    this._files = Collections.unmodifiableList(files);
    this._id = id(files);
    this._imported = new ArrayList<File>();
    this._minimized = minimized;
  }

  /**
   * Returns the name of this bundle.
   * @return the name of this bundle.
   */
  public String name() {
    return this._name;
  }

  /**
   * Returns the list of files to bundle.
   * @return the list of files to bundle.
   */
  public List<File> files() {
    return this._files;
  }

  /**
   * Returns an ID for that bundle based on the names list of files.
   *
   * <p>The ID of the bundle remain constant.
   *
   * @return an ID for that bundle based on the list of files.
   */
  public String id() {
    return this._id;
  }

  /**
   * Clears the list of imported files.
   */
  public void clearImport() {
    this._imported.clear();
  }

  /**
   * Adds a file to consider as an import.
   * @param f the file to import.
   */
  public void addImport(File f) {
    this._imported.add(f);
  }

  /**
   * Returns the etag for that bundle.
   *
   * <p>The etag changes when any of the file changes.
   *
   * @param refresh <code>true</code> to calculate the etag; <code>false</code> otherwise.
   * @return the etag for that bundle.
   */
  public String getETag(boolean refresh) {
    if (this._etag == null || refresh) {
      this._etag = calculateEtag(this._files, this._imported);
    }
    return this._etag;
  }

  /**
   * Calculates whether the bundles is still fresh by comparing the etag.
   *
   * @return <code>true</code> if still fresh;
   *         <code>false</code> otherwise.
   */
  public boolean isFresh() {
    String etag = calculateEtag(this._files, this._imported);
    return etag.equals(this._etag);
  }

  /**
   * Returns the filename of this bundle.
   *
   * <p>The filename is: <code>[name]-[isodate]-[etag-suffix].[extension]</code>.
   * <p>Or <code>[name]-[isodate]-[etag-suffix].min.[extension]</code> if minimized.
   *
   * @return the filename of this bundle.
   */
  public String getFileName() {
    StringBuilder filename = new StringBuilder(this._name);
    filename.append('-');
    filename.append(ISO8601.CALENDAR_DATE.format(getMostRecent(this._files)));
    String etag = getETag(false);
    filename.append('-').append(etag.substring(etag.length()-4));
    String ext = getExtension(this._files.get(0));
    if (this._minimized) filename.append(".min");
    if (ext != null) filename.append(ext);
    return filename.toString();
  }

  // Static methods -------------------------------------------------------------------------------

  /**
   * Returns an ID for this bundle.
   *
   * @param files the files included in the bundle.
   * @return An ID based on the hash value of the concatenation.
   */
  public static String id(List<File> files) {
    StringBuilder id = new StringBuilder();
    for (File f : files) {
      id.append(f.getAbsolutePath());
    }
    return MD5.hash(id.toString());
  }

  /**
   * Calculate the etag for the specified lists of files based on the absolute path, length
   * and last modified date.
   *
   * @param files    the list of files.
   * @param imported the list of imported files (CSS only).
   *
   * @return an MD5 value.
   */
  private static String calculateEtag(List<File> files, List<File> imported) {
    StringBuilder key = new StringBuilder();
    for (File f : files) { appendKey(f, key); }
    // Also include files that are imported (@import rules in CSS)
    for (File f : imported) { appendKey(f, key); }
    return MD5.hash(key.toString());
  }

  /**
   * Returns the extension of the specified file including the dot.
   *
   * @param file the file which extension is needed.
   * @return the extension or <code>null</code> if none available.
   */
  private static String getExtension(File file) {
    int dot = file.getName().lastIndexOf('.');
    return dot >= 0? file.getName().substring(dot) : null;
  }

  /**
   * Appends the key based on absolute path, length and last modified date for for one file.
   *
   * @param f   The file which
   * @param key Key to append.
   */
  private static void appendKey(File f, StringBuilder key) {
    key.append(f.getAbsolutePath());
    key.append(f.length()).append('>');
    key.append(f.lastModified()).append('|');
  }

  /**
   * Returns the date of the most recent file.
   * @param files the list of files.
   * @return the date of the most recent.
   */
  private static long getMostRecent(List<File> files) {
    long mostRecent = 0;
    for (File f : files) {
      if (f.lastModified() > mostRecent)
        mostRecent = f.lastModified();
    }
    return mostRecent;
  }
}
