/*
 * Copyright (c) 2011 weborganic systems pty. ltd.
 */
package com.weborganic.bastille.web;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.GlobalSettings;
import org.weborganic.berlioz.content.Cacheable;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentRequest;
import org.weborganic.berlioz.content.Environment;
import org.weborganic.berlioz.content.Service;
import org.weborganic.berlioz.servlet.HttpContentRequest;

import com.topologi.diffx.xml.XMLWriter;
import com.weborganic.bastille.util.WebBundleTool;

/**
 * This generator returns the XML for the Footer.
 *
 * <h3>Configuration</h3>
 *
 *
 * <h3>Parameters</h3>
 *
 *
 * <h3>Returned XML</h3>
 *
 *
 * <h4>Error handling</h4>
 *
 * <h3>Usage</h3>
 *
 * <h3>ETag</h3>
 *
 * @author Christophe Lauret
 * @version 0.6.5 - 31 May 2010
 * @since 0.6.0
 */
public final class GetWebBundles implements ContentGenerator, Cacheable {

  /** Logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(GetWebBundles.class);

  /** Where the bundled scripts should be located */
  private static final String DEFAULT_BUNDLED_SCRIPTS = "/script/bundle/";

  /** Where the bundled styles should be located */
  private static final String DEFAULT_BUNDLED_STYLES = "/style/bundle/";

  /** The default JavaScript bundle. */
  private static final Properties DEFAULT_JS_BUNDLE = new Properties();
  static {
    DEFAULT_JS_BUNDLE.setProperty("global",    "/script/global.js");
    DEFAULT_JS_BUNDLE.setProperty("{GROUP}",   "/script/{GROUP}.js");
    DEFAULT_JS_BUNDLE.setProperty("{SERVICE}", "/script/{GROUP}/{SERVICE}.js");
  }

  /** The default CSS bundle. */
  private static final Properties DEFAULT_CSS_BUNDLE = new Properties();
  static {
    DEFAULT_JS_BUNDLE.setProperty("global",    "/style/global.css");
    DEFAULT_JS_BUNDLE.setProperty("{GROUP}",   "/style/{GROUP}.css");
    DEFAULT_JS_BUNDLE.setProperty("{SERVICE}", "/style/{GROUP}/{SERVICE}.css");
  }

  /**
   * The tool used for bundling JS.
   */
  private WebBundleTool jstool = null;

  /**
   * The tool used for bundling CSS.
   */
  private WebBundleTool csstool = null;

  @Override
  public String getETag(ContentRequest req) {
    return null;
  }

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {
    HttpContentRequest hreq = (HttpContentRequest)req;
    Service service = hreq.getService();
    Environment env = req.getEnvironment();
    init(env);

    // Parameters
    boolean doBundle = !"false".equals(req.getParameter("berlioz-bundle", "true"));

    // Scripts
    Properties js = GlobalSettings.getNode("bastille.jsbundler.bundles");
    String jsbundles = GlobalSettings.get("bastille.jsbundler.location", DEFAULT_BUNDLED_SCRIPTS);
    if (js == null || js.isEmpty()) js = DEFAULT_JS_BUNDLE;
    for (Entry<Object, Object> bundle : js.entrySet()) {
      String name = replaceTokens(bundle.getKey().toString(), service);
      if (doBundle) {
        List<File> files = getFiles(bundle.getValue().toString(), service, env);
        bundleScripts(this.jstool, files, name, xml);
      } else {
        List<String> paths = getPathsOnly(bundle.getValue().toString(), service, env);
        for (String path : paths) {
          xml.openElement("script", false);
          xml.attribute("src", path);
          xml.closeElement();
        }
      }
    }

    // Styles
    Properties css = GlobalSettings.getNode("bastille.cssbundler.bundles");
    if (css == null || css.isEmpty()) css = DEFAULT_CSS_BUNDLE;
    for (Entry<Object, Object> bundle : css.entrySet()) {
      String name = replaceTokens(bundle.getKey().toString(), service);
      if (doBundle) {
        List<File> files = getFiles(bundle.getValue().toString(), service, env);
        bundleStyles(this.csstool, files, name, xml);
      } else {
        List<String> paths = getPathsOnly(bundle.getValue().toString(), service, env);
        for (String path : paths) {
          xml.openElement("style", false);
          xml.attribute("src", path);
          xml.closeElement();
        }
      }
    }
  }

  /**
   * Bundles the scripts using the specified bundler
   *
   * @param bundler The bundler to use.
   * @param files   The files to bundle.
   * @param name    The name of the bundle.
   * @param xml     The XML output
   *
   * @throws IOException Should an IO error occur
   */
  private void bundleScripts(WebBundleTool bundler, List<File> files, String name, XMLWriter xml) throws IOException {
    if (files.isEmpty()) return;
    boolean min = GlobalSettings.get("bastille.jsbundler.minimize", true);
    File bundle = bundler.bundle(files, name, min);
    LOGGER.debug("{} -> {}", files, bundle.getName());
    String jsbundles = GlobalSettings.get("bastille.jsbundler.location", DEFAULT_BUNDLED_STYLES);
    xml.openElement("script", false);
    xml.attribute("src", jsbundles+bundle.getName());
    xml.closeElement();
  }

  /**
   * Bundles the scripts using the specified bundler
   *
   * @param bundler The bundler to use.
   * @param files   The files to bundle.
   * @param name    The name of the bundle.
   * @param xml     The XML output
   *
   * @throws IOException Should an IO error occur
   */
  private void bundleStyles(WebBundleTool bundler, List<File> files, String name, XMLWriter xml) throws IOException {
    if (files.isEmpty()) return;
    boolean min = GlobalSettings.get("bastille.jsbundler.minimize", true);
    File bundle = bundler.bundle(files, name, min);
    LOGGER.debug("{} -> {}", files, bundle.getName());
    String cssbundles = GlobalSettings.get("bastille.cssbundler.location", DEFAULT_BUNDLED_STYLES);
    xml.openElement("style", false);
    xml.attribute("src", cssbundles+bundle.getName());
    xml.closeElement();
  }

  /**
   * Initialises the bundlers.
   *
   * @param env The environment.
   */
  private void init(Environment env) {
    // Initialise the JavaScript bundling tool
    String jsbundles = GlobalSettings.get("bastille.jsbundler.location", DEFAULT_BUNDLED_STYLES);
    File bundles = env.getPublicFile(jsbundles);
    if (!bundles.exists()) bundles.mkdirs();
    if (this.jstool == null)
      this.jstool = new WebBundleTool(bundles);
    // Initialise the CSS bundling tool
    String cssbundles = GlobalSettings.get("bastille.cssbundler.location", DEFAULT_BUNDLED_STYLES);
    File bundles2 = env.getPublicFile(cssbundles);
    if (!bundles2.exists()) bundles2.mkdirs();
    if (this.csstool == null)
      this.csstool = new WebBundleTool(bundles2);
  }

  /**
   * Returns the files in the bundle filtering out files which do not exist and automatically replacing tokens.
   *
   * @param paths   The list of paths
   * @param service The service
   * @param env     The environment
   * @return the list of files to bundles.
   */
  private static List<String> getPathsOnly(String paths, Service service, Environment env) {
    if (paths.indexOf(',') >= 0) {
      // multiple paths specified
      List<String> existing = new ArrayList<String>();
      for (String path : paths.split(",")) {
        String p = replaceTokens(path, service);
        File file = env.getPublicFile(p);
        if (file.exists()) existing.add(p);
      }
      return existing;
    } else {
      // only one paths
      File file = env.getPublicFile(paths);
      if (file.exists()) return Collections.singletonList(paths);
    }
    return Collections.emptyList();
  }

  /**
   * Returns the files in the bundle filtering out files which do not exist and automatically replacing tokens.
   *
   * @param paths   The list of paths
   * @param service The service
   * @param env     The environment
   * @return the list of files to bundles.
   */
  private static List<File> getFiles(String paths, Service service, Environment env) {
    if (paths.indexOf(',') >= 0) {
      // multiple paths specified
      List<File> files = new ArrayList<File>();
      for (String path : paths.split(",")) {
        String p = replaceTokens(path, service);
        File file = env.getPublicFile(p);
        if (file.exists()) files.add(file);
      }
      return files;
    } else {
      // only one paths
      String p = replaceTokens(paths, service);
      File file = env.getPublicFile(p);
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
