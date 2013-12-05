package org.weborganic.bastille.web;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.weborganic.berlioz.content.Environment;
import org.weborganic.berlioz.content.Service;

final class BundleInstance {

  private final String _name;

  private final String[] _ipaths;

  private BundleInstance(String name, String[] ipaths) {
    this._name = name;
    this._ipaths = ipaths;
  }

  public String name() {
    return this._name;
  }

  public List<String> paths(Environment env) {
    return computePaths(this._ipaths, env);
  }

  public List<File> files(Environment env) {
    return getFiles(this._ipaths, env);
  }

  public static BundleInstance instantiate(BundleConfig config, BundleDefinition definition, Service service, Environment env) {
    String name = replaceTokens(definition.filename(), service);
    final int count = definition.paths().length;
    String[] paths = new String[count];
    for (int i = 0; i < count; i++) {
      paths[i] = replaceTokens(definition.paths()[i], service);
    }
    return new BundleInstance(name, paths);
  }

  /**
   * Returns the files in the bundle filtering out files which do not exist and automatically replacing tokens.
   *
   * @param paths   The list of paths
   * @param service The service
   * @param env     The environment
   * @return the list of files to bundles.
   */
  private static List<String> computePaths(String[] paths, Environment env) {
    if (paths == null) return Collections.emptyList();
    if (paths.length > 1) {
      // multiple paths specified
      List<String> existing = new ArrayList<String>(paths.length);
      for (String p : paths) {
        File file = env.getPublicFile(p);
        if (file.exists()) existing.add(p);
      }
      return existing;
    } else if (paths.length == 1) {
      // only one path
      File file = env.getPublicFile(paths[0]);
      if (file.exists()) return Collections.singletonList(paths[0]);
    }
    return Collections.emptyList();
  }

  /**
   * Returns the files in the bundle filtering out files which do not exist and automatically replacing tokens.
   *
   * @param paths   The list of paths
   * @param env     The environment
   * @return the list of files to bundle.
   */
  private static List<File> getFiles(String[] paths, Environment env) {
    if (paths == null) return Collections.emptyList();
    if (paths.length > 1) {
      // multiple paths specified
      List<File> files = new ArrayList<File>(paths.length);
      for (String p : paths) {
        File file = env.getPublicFile(p);
        if (file.exists()) files.add(file);
      }
      return files;
    } else if (paths.length == 1) {
      // only one paths
      File file = env.getPublicFile(paths[0]);
      if (file.exists()) return Collections.singletonList(file);
    }
    return Collections.emptyList();
  }

  /**
   * Replaces the tokens in the string.
   *
   * @param value   The value containing tokens to be replaced.
   * @param service The service.
   *
   * @return The corresponding value with all tokens replaced.
   */
  private static String replaceTokens(String value, Service service) {
    String out = value;
    while (out.contains("{GROUP}"))   {
      out = out.replace("{GROUP}", service.group());
    }
    while (out.contains("{SERVICE}")) {
      out = out.replace("{SERVICE}", service.id());
    }
    return out;
  }

}
