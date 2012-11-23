/*
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package com.weborganic.bastille.util;

/**
 * A utility class to manipulate paths.
 *
 * @author Christophe Lauret
 * @version 25 November 2012.
 */
public final class Paths {

  /**
   * Utility class.
   */
  private Paths() {
  }

  /**
   * Filters and normalizes the value in the path informations.
   *
   * @param path The path to normalize.
   * @return The same path without an '/' at the end.
   */
  public static String normalize(String path) {
    String normalized = path;
    // trailing '/'
    if (path.endsWith("/") && path.length() > 1) {
      normalized = path.substring(0, path.length()-1);
    }
    // double '//'
    if (normalized.indexOf("//") >= 0) {
      normalized = normalized.replaceAll("//+", "/");
    }
    // self
    if (normalized.indexOf("./") == 0) {
      normalized = normalized.substring(2);
    }
    if (normalized.indexOf("/./") >= 0) {
      normalized = normalized.replaceAll("/\\./", "/");
    }
    // parent
    while (normalized.indexOf("/../") > normalized.indexOf('/')+1) {
      normalized = normalized.replaceFirst("/[^/.]+/\\.\\./", "/");
    }
    if (normalized.indexOf("/../") > 1) {
      normalized = normalized.replaceFirst("^[^/.]+/\\.\\./", "");
    }
    return normalized;
  }

  /**
   * Computes the path from a path to another.
   *
   * <pre>
   * /          to /      => .
   * /a         to /a     => .
   * /a         to /b     => ../b
   * /a/a/../c  to /a     => ../../a
   * /          to /b     => b
   * /          to /b/c   => b/c
   * </pre>
   *
   * @param from The path to start from
   * @param to   The path to arrive at
   *
   * @return the path from a to b
   */
  public static String path(String from, String to) {
    String f = Paths.normalize('/'+from);
    String t = Paths.normalize('/'+to);
    if (f.equals(t)) {
      return ".";
    }
    StringBuilder path = new StringBuilder();
    int i = -1;
    while (f.length() > 1 && (i = f.indexOf('/', i+1)) >= 0) {
      if (path.length() > 0) path.append('/');
      path.append("..");
    }
    if (path.length() == 0 && t.length() > 0) {
      path.append(t.substring(1));
    } else {
      path.append(t);
    }
    return path.toString();
  }

//  public static void main(String[] args) {
//    String[][] pairs = new String[][]{
//      {"/",    "/"},    // "."
//      {"/a",   "/a"},   // "."
//      {"/a",   "/b"},   // "../b"
//      {"/a/a/../c", "/a"},   // ".."
//      {"/",    "/b"},   // "b"
//      {"/",    "b/c"},  // "b/c"
//      {"a",    "a"},   // "."
//      {"a",    "b"},   // "../b"
//      {"a/a/../c", "a"},   // ".."
//      {"",     "b"},   // "b"
//      {"",     "b/c"}  // "b/c"
//    };
//
//    for (String[] p : pairs) {
//      System.err.println(p[0]+" \tto "+p[1]+" \t=> "+Paths.normalize(p[0])+" \tto "+Paths.normalize(p[1])+" \t=>"+path(p[0], p[1]));
//    }
//  }

}
