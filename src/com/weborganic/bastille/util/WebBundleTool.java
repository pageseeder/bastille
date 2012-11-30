/*
 * This file is part of the Bastille library.
 *
 * Available under a commercial licence, contact Weborganic.
 *
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package com.weborganic.bastille.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.weborganic.bastille.util.WebBundle.Type;

/**
 * This class is used to bundles resources together as one in order to minimise the number of resources to request.
 *
 * @author Christophe Lauret
 * @version 30 November 2012
 */
public final class WebBundleTool {

  /**
   * Logger for this class.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(WebBundleTool.class);

  /**
   * All I/O operations requiring encoding/decoding should use utf8.
   */
  private static final Charset UTF8 = Charset.forName("utf-8");

  /**
   * Matches URL references in CSS.
   */
  private static final Pattern CSS_URL = Pattern.compile("url\\(([^)]*)\\)");

  /**
   * Stores bundles instances to check for freshness.
   */
  private static Map<String, WebBundle> instances = new Hashtable<String, WebBundle>();

  /**
   * The maximum size for turning the content of an image into a data URI.
   */
  private static long DATA_URI_MAX_SIZE = 4096L;

  // class attributes
  // ----------------------------------------------------------------------------------------------

  /**
   * Where the bundles should be saved
   */
  private final File _bundles;

  /**
   * The virtual location of the bundles (to calculate the relative path).
   */
  private File _virtual;

  /**
   * Creates a new Resource Bundler saving the bundles in the specified location.
   *
   * @param bundles Where the bundles should be saved (must exist and be a directory).
   *
   * @throws IllegalArgumentException If the specified file is <code>null</code>, does not exist or is not a directory.
   */
  public WebBundleTool(File bundles) throws IllegalArgumentException {
    if (bundles == null)
      throw new IllegalArgumentException("The location where bundles are saved cannot be null.");
    if (!bundles.exists())
      throw new IllegalArgumentException("The location where bundles are saved must exist: "+bundles);
    if (!bundles.isDirectory())
      throw new IllegalArgumentException("The location where bundles are saved must be a directory: "+bundles);
    this._bundles = bundles;
    this._virtual = bundles;
  }

  /**
   * Set the virtual location of the bundles.
   *
   * @param virtual the virtual location of the bundles.
   */
  public void setVirtual(File virtual) {
    this._virtual = virtual;
  }

  /**
   * @return Where the bundles are being stored.
   */
  public File getBundlesDir() {
    return this._bundles;
  }

  /**
   * Return the file bundling the specified list of files.
   *
   * @param files    The list of files to bundle together.
   * @param prefix   The prefix for the bundle.
   * @param minimize Whether to minimise the files.
   *
   * @return The file corresponding to the generated bundle.
   *
   * @throws IOException should an error occur while reading the files or writing the bundle.
   */
  public File getBundle(List<File> files, String prefix, boolean minimize) throws IOException {
    if (files.isEmpty()) return null;
    String filename = new WebBundle(prefix, files, minimize).getFileName();
    return new File(this._bundles, filename);
  }

  /**
   * Bundles the specified files together, this method automatically detects the type.
   *
   * @param files The list of files to bundle together.
   * @param name  The name of the bundle.
   * @param minimize Whether to minimise the files.
   *
   * @return The file corresponding to the generated bundle.
   *
   * @throws IOException should an error occur while reading the files or writing the bundle.
   */
  public File bundle(List<File> files, String name, boolean minimize) throws IOException {
    if (files.isEmpty()) return null;
    String ext = getExtension(files.get(0));
    for (Type t : Type.values()) {
      if (t.matches(ext)) return bundle(files, name, t, minimize);
    }
    // no matching type
    return null;
  }

  /**
   * Bundles the specified files together.
   *
   * @param files The list of files to bundle together.
   * @param name  The name of the bundle.
   * @param type  The type of bundling.
   * @param minimize Whether to minimise the files.
   *
   * @return The file corresponding to the generated bundle.
   *
   * @throws IOException should an error occur while reading the files or writing the bundle.
   */
  public File bundle(List<File> files, String name, Type type, boolean minimize) throws IOException {
    switch (type) {
      case SCRIPT : return bundleScripts(files, name, minimize);
      case STYLE : return bundleStyles(files, name, minimize);
      default : return null;
    }
  }

  /**
   * Bundles the specified files together.
   *
   * @param files    The list of files to bundle together.
   * @param name     The name of the bundle.
   * @param minimize Whether to minimize the scripts.
   *
   * @return The file corresponding to the generated bundle.
   *
   * @throws IOException should an error occur while reading the files or writing the bundle.
   */
  public File bundleScripts(List<File> files, String name, boolean minimize) throws IOException {
    if (files.isEmpty()) return null;
    // Generate the hash value based on the filename, length and last modified date
    File bundle = getBundle(files, name, minimize);
    // concatenate the content if the file does not already exist
    if (!bundle.exists()) {
      concatenate(files, bundle, minimize);
      bundle.deleteOnExit();
    }
    return bundle;
  }

  /**
   * Bundles the specified files together.
   *
   * @param files    The list of files to bundle together.
   * @param name     The name of the bundle
   * @param minimize <code>true</code> to minimize the javascript;
   *                 <code>false</code> to simply concatenate.
   *
   * @return The file corresponding to the generated bundle.
   *
   * @throws IOException should an error occur while reading the files or writing the bundle.
   */
  public File bundleStyles(List<File> files, String name, boolean minimize) throws IOException {
    if (files.isEmpty()) return null;
    String key = WebBundle.id(files);
    WebBundle bundle = instances.get(key);

    boolean stale = false;
    // Bundle has never been processed or it isn't fresh
    if (bundle == null) {
      bundle = new WebBundle(name, files, minimize);
      stale = true;
    // Create and process the new bundle
    } else if (!bundle.isFresh()) {
      stale = true;
    }
    String filename = bundle.getFileName();
    File file = new File(this._bundles, filename);

    // concatenate the content if the file does not already exist
    if (stale || !file.exists()) {

      // Write to the file
      bundle.clearImport();
      StringWriter writer = new StringWriter();
      expandStyles(bundle, writer, new File(this._virtual, file.getName()));
      bundle.getETag(true);
      filename = bundle.getFileName();
      instances.put(key, bundle);

      // Write to the file
      StringReader reader = new StringReader(writer.toString());
      file = new File(this._bundles, filename);
      if (minimize) {
        CSSMin.minimize(reader, new PrintStream(file));
      } else {
        IOUtils.copy(reader, new FileOutputStream(file), "utf8");
      }
      file.deleteOnExit();
    }
    return file;
  }

  // Private helpers
  // ----------------------------------------------------------------------------------------------

  /**
   * Concatenate the contents of each file in the bundle.
   *
   * @param files    The list of files to concatenate.
   * @param bundle   The bundle to write to.
   * @param minimize <code>true</code> to minimize the script;
   *                 <code>false</code> otherwise.
   *
   * @throws IOException if an input/output error occurs.
   */
  protected static void concatenate(List<File> files, File bundle, boolean minimize) throws IOException {
    FileOutputStream out = new FileOutputStream(bundle);

    // Copy the input stream to the output stream
    try {
      for (File f : files) {
        if (minimize && !f.getName().endsWith(".min.js"))
          minimizeAndCopyTo(f, out);
        else
          copyTo(f, out);
      }
    } finally {
      IOUtils.closeQuietly(out);
    }
  }

  /**
   * Concatenate the contents of each file in the bundle.
   *
   * @param bundle  The list of files to concatenate.
   * @param writer  The bundle to write to.
   * @param virtual The virtual location of the bundle.
   *
   * @throws IOException if an input/output error occurs.
   */
  protected static void expandStyles(WebBundle bundle, Writer writer, File virtual) throws IOException {

    // Copy the input stream to the output stream
    IOException exception = null;
    List<File> processed = new ArrayList<File>();
    for (File f : bundle.files()) {
      exception = expandStylesTo(bundle, f, virtual, writer, processed);
      writer.write('\n'); // insert new line
    }

    // Re-throw any exception that has occurred
    if (exception != null) throw exception;
  }

  /**
   * Copy the contents of the specified input stream to the specified output stream, and ensure that both streams are
   * closed before returning (even in the face of an exception).
   *
   * @param file The file to read.
   * @param out  Where the output goes to.
   *
   * @throws IOException if an input/output error occurs
   */
  private static void minimizeAndCopyTo(File file, OutputStream out) throws IOException {
    FileInputStream input = new FileInputStream(file);
    try {
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      JSMin minimizer = new JSMin(input, buffer);
      minimizer.jsmin();
      byte[] min = buffer.toByteArray();
      out.write(min);
      out.write('\n');
      out.flush();
    } catch (ParsingException ex) {
      LOGGER.warn("Unable to minimize {}: {}", file.getName(),  ex.getMessage());
      IOUtils.closeQuietly(input);
      copyTo(file, out);
    } finally {
      IOUtils.closeQuietly(input);
    }
  }

  /**
   * Copy the contents of the specified input stream to the specified output stream, and ensure that both streams are
   * closed before returning (even in the face of an exception).
   *
   * @param file The file to read.
   * @param out  Where the output goes to.
   *
   * @throws IOException if an input/output error occurs
   */
  private static void copyTo(File file, OutputStream out) throws IOException {
    FileInputStream input = new FileInputStream(file);
    try {
      InputStreamReader reader = new InputStreamReader(new FileInputStream(file), UTF8);
      OutputStreamWriter writer = new OutputStreamWriter(out);
      LineIterator lines = IOUtils.lineIterator(reader);
      while (lines.hasNext()) {
        writer.append(lines.nextLine());
        writer.append('\n');
      }
      writer.flush();
      writer = null;
    } finally {
      IOUtils.closeQuietly(input);
    }
  }

  /**
   * Returns the extension of the specified file.
   *
   * @param file the file which extension is needed.
   * @return the extension or <code>null</code> if none available.
   */
  private static String getExtension(File file) {
    int dot = file.getName().lastIndexOf('.');
    return dot >= 0? file.getName().substring(dot) : null;
  }

  /**
   * Copy the contents of the specified input stream to the specified output stream, and ensure that both streams are
   * closed before returning (even in the face of an exception).
   *
   * @param bundle    The bundle being processed.
   * @param file      The file to read.
   * @param virtual   The location of the bundle (virtual).
   * @param out       Writes the content for the bundle.
   * @param processed The list of files already processed to prevent circular references.
   *
   * @return IOException if an input/output error occurs
   *
   * @throws IOException if unable to read file.
   */
  private static IOException expandStylesTo(WebBundle bundle, File file, File virtual, Writer out, List<File> processed)
      throws IOException {
    IOException exception = null;
    // prevent circular references
    if (processed.contains(file)) return exception;
    processed.add(file);
    // start processing
    InputStreamReader reader = new InputStreamReader(new FileInputStream(file), UTF8);
    BufferedReader b = new BufferedReader(reader);
    try {
      String line = b.readLine();
      while (line != null) {
        Matcher m = CSS_URL.matcher(line);
        if (m.find()) {
          // Expand the file
          if (line.trim().toLowerCase().startsWith("@import")) {
            String path = unquote(m.group(1));
            File imported = new File(file.getParentFile(), path);
            if (imported.exists()) {
              out.write("/* START @import "+path+" */\n");
              bundle.addImport(imported);
              expandStylesTo(bundle, imported, virtual, out, processed);
              out.write("/* END @import "+path+ " */\n");
            } else {
              out.write("/* ERROR Unable to @import */\n");
              LOGGER.warn("Unable to find referenced CSS file: {}", path);
              out.write(line);
              out.write('\n');
            }
          } else {
            // Replace all URL links to the new relative location
            m.reset();
            StringBuffer sb = new StringBuffer();
            while (m.find()) {
              String url = unquote(m.group(1));
              String query = "";
              int q = url.indexOf('?');
              if (q > 0) {
                query = url.substring(q);
                url = url.substring(0, q);
              }
              // TODO: data URIs
              m.appendReplacement(sb, "url("+getLocation(file, virtual, url)+query+")");
            }
            m.appendTail(sb);
            out.write(sb.toString());
          }

        } else {
          // Just copy the line
          out.write(line);
        }
        // Always end with a new line
        out.write('\n');
        line = b.readLine();
      }
      b.close();
    } catch (IOException ex) {
      exception = ex;
    }
    reader.close();
    return exception;
  }

  /**
   * Recalculates the specified path from the original file (source) to the new file (target).
   *
   * @param source The source file.
   * @param target The target file.
   * @param path   The location based on the source file.
   *
   * @return The location based on the target file.
   */
  protected static String getLocation(File source, File target, String path) {
    // Ignore data URIs and absolute paths
    if (path.startsWith("data:") || path.startsWith("/")) { return path; }
    StringBuilder location = new StringBuilder();
    try {
      // Locate the referenced URL
      File ftarget = new File(source.getParentFile(), path);
      boolean isImage = path.endsWith(".png") || path.endsWith(".jpg") || path.endsWith(".gif");
      if (isImage && ftarget.exists() && ftarget.length() < DATA_URI_MAX_SIZE) {
        // Replace short images by data uri
        location.append("data:image/").append(path.substring(path.lastIndexOf('.')+1)).append(";base64,");
        location.append(Base64.encodeFromFile(ftarget));
      } else {
        String csource = ftarget.getCanonicalPath();
        // Check difference with bundle file
        String ctarget = target.getCanonicalPath();
        int x = common(csource, ctarget);
        // Start path to return to common base
        String rbundle = ctarget.substring(x);
        for (int i = 0; i < rbundle.length(); i++) {
          if (rbundle.charAt(i) == File.separatorChar) location.append("../");
        }
        // Continue with remaining path
        location.append(csource.substring(x).replace('\\', '/'));
      }
    } catch (IOException ex) {
      LOGGER.warn("Error while calculating location", ex);
    }
    return location.toString();
  }

  /**
   * Returns the number of common characters between the two strings.
   *
   * @param a The first string to compare.
   * @param b The second string.
   * @return The number of characters in common; 0 if none.
   */
  private static int common(String a, String b) {
    int i = 0;
    while (i < a.length() && i < b.length() && a.charAt(i) == b.charAt(i)) {
      i++;
    }
    return i;
  }

  /**
   * Returns the number of common characters between the two strings.
   *
   * @param url Removes the quotes.
   *
   * @return The number of characters in common; 0 if none.
   */
  private static String unquote(String url) {
    if (url.length() < 2) return url;
    char first = url.charAt(0);
    char last  = url.charAt(url.length()-1);
    if ((first == '\'' && last == '\'') || first == '"' && last == '"') {
      // quoted
      return url.substring(1, url.length()-1);
    } else {
      // unquoted
      return url;
    }
  }

}
