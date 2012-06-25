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
 * This generator returns the list of timestamped scripts and styles for a given service.
 *
 * <p>It will assemble and minimize the scripts and styles together as much as possible and return the
 * list of bundles. Because the name each bundle includes unique stamp, they can be cached for a long periods.
 * When files included in a bundle are modified, this generator will automatically produce a new bundle
 * with a new stamp so that it results in a different URL.
 *
 * <h4>Bundling</h4>
 * <p>Scripts are simply concatenated. Styles are concatenated and import rules will automatically include
 * the imported styles into the main file.
 *
 * <h4>Minimization</h4>
 * <p>Both styles and scripts can be minimised after the bundling. Minimized bundles will be saved using the
 * following extensions <code>.min.js</code> and <code>.min.css</code>. If files to be bundled already use the
 * <code>*.min.*</code> extension, it will be considered to be already minimised and won't be minimised again.
 *
 * <h4>File naming</h4>
 * <p>Bundled files are automatically named as:</p>
 * <pre>[bundlename]-[date]-[etag].[ext]</pre>
 * <p>The <i>bundle name</i> is specified in the configuration; the <i>date</i> is the creation date of the
 * bundle; the etag is the 4-character alphanumerical stamp; and the extension depends on the MIME type and
 * minimization options.
 *
 *
 * <h3>Configuration</h3>
 * <p>This generator is highly configurable and the configuration properties are specific (but similar)
 * for styles and scripts.
 *
 * <p>Properties pertaining to scripts and styles are prefixed by respectively <code>bastille.jsbundler</code>
 * and <code>bastille.cssbundler</code>.
 *
 * <p>The <code>minimize</code> property can be used to control minimization of the code.
 *
 * <p>The <code>location</code> property can be used to define where the bundled files should be stored.
 *
 * <p>Each bundle is specified using the <code>bundles</code> property node where the bundle names are mapped
 * to the comma separated list of paths to include.
 *
 * <h4>Default configuration</h4>
 * <p>The default configuration is the equivalent of:</p>
 * <p><i>(Use curly brackets instead of square brackets)</i></p>
 * <pre>{@code
 * <node name="bastille.cssbundler">
 *   <map>
 *     <entry key="minimize"  value="true"/>
 *     <entry key="location"  value="/style/_/"/>
 *   </map>
 *   <node name="bundles">
 *     <map>
 *        <entry key="global"    value="/style/global.css"/>
 *        <entry key="[GROUP]"   value="/style/[GROUP].css"/>
 *        <entry key="[SERVICE]" value="/style/[GROUP]/[SERVICE].css"/>
 *      </map>
 *   </node>
 * </node>
 * <node name="bastille.jsbundler">
 *   <map>
 *     <entry key="minimize"  value="true"/>
 *     <entry key="location"  value="/script/_/"/>
 *   </map>
 *   <node name="bundles">
 *   <map>
 *     <entry key="global"    value="/script/global.js"/>
 *     <entry key="[GROUP]"   value="/script/[GROUP].js"/>
 *     <entry key="[SERVICE]" value="/script/[GROUP]/[SERVICE].js"/>
 *   </map>
 * </node>
 * }</pre>
 *
 *
 * <h3>Parameters</h3>
 * <p>No parameters are required for this generator, but the bundling can be disabled by setting the
 * <code>berlioz-bundle</code> parameter to <code>true</code>
 *
 * <h3>Returned XML</h3>
 * <p>The XML returns the scripts and styles in the order in which they are defined.
 * <pre>{@code
 * <script src="[jslocation]/[bundle].js" bundled="[true|false]" minimized="[true|false]" />
 * ...
 * <style  src="[csslocation]/[bundle].css" bundled="[true|false]" minimized="[true|false]" />
 * ...
 * }</pre>
 *
 * <h4>Error handling</h4>
 *
 * <h3>Usage</h3>
 *
 * <h3>ETag</h3>
 *
 * @author Christophe Lauret
 * @version 0.6.40 - 25 June 2012
 * @since 0.6.0
 */
public final class GetWebBundles implements ContentGenerator, Cacheable {

  /** Logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(GetWebBundles.class);

  /** Where the bundled scripts should be located */
  private static final String DEFAULT_BUNDLED_SCRIPTS = "/script/_/";

  /** Where the bundled styles should be located */
  private static final String DEFAULT_BUNDLED_STYLES = "/style/_/";

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
    HttpContentRequest hreq = (HttpContentRequest)req;
    Service service = hreq.getService();
    Environment env = req.getEnvironment();
    init(env);
    boolean doBundle = !"false".equals(req.getParameter("berlioz-bundle", "true"));
    if (doBundle) {
      try {
        long etag = 0L;
        Properties js = getJSConfig();
        for (Entry<Object, Object> bundle : js.entrySet()) {
          boolean min = GlobalSettings.get("bastille.jsbundler.minimize", true);
          String name = replaceTokens(bundle.getKey().toString(), service);
          List<File> files = getFiles(bundle.getValue().toString(), service, env);
          File b = this.jstool.getBundle(files, name, min);
          if (b != null && b.lastModified() > etag) etag = b.lastModified();
        }
        Properties css = getCSSConfig();
        for (Entry<Object, Object> bundle : css.entrySet()) {
          boolean min = GlobalSettings.get("bastille.cssbundler.minimize", true);
          String name = replaceTokens(bundle.getKey().toString(), service);
          List<File> files = getFiles(bundle.getValue().toString(), service, env);
          File b = this.csstool.getBundle(files, name, min);
          if (b != null && b.lastModified() > etag) etag = b.lastModified();
        }
        return Long.toString(etag);
      } catch (IOException ex) {
        LOGGER.warn("Unable to generate ETag", ex);
      }
    }
    return null;
  }

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {
    HttpContentRequest hreq = (HttpContentRequest)req;
    Service service = hreq.getService();
    Environment env = req.getEnvironment();

    // Parameters
    boolean doBundle = !"false".equals(req.getParameter("berlioz-bundle", "true"));

    // Scripts
    Properties js = getJSConfig();
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
          xml.attribute("bundled", "false");
          xml.attribute("minimized", "false");
          xml.closeElement();
        }
      }
    }

    // Styles
    Properties css = getCSSConfig();
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
          xml.attribute("bundled", "false");
          xml.attribute("minimized", "false");
          xml.closeElement();
        }
      }
    }
  }

  // Private helpers
  // ----------------------------------------------------------------------------------------------

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
    String jsbundles = GlobalSettings.get("bastille.jsbundler.location", DEFAULT_BUNDLED_SCRIPTS);
    xml.openElement("script", false);
    xml.attribute("src", jsbundles+bundle.getName());
    xml.attribute("bundled", "true");
    xml.attribute("minimized", Boolean.toString(min));
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
    boolean min = GlobalSettings.get("bastille.cssbundler.minimize", true);
    File bundle = bundler.bundle(files, name, min);
    LOGGER.debug("{} -> {}", files, bundle.getName());
    String cssbundles = GlobalSettings.get("bastille.cssbundler.location", DEFAULT_BUNDLED_STYLES);
    xml.openElement("style", false);
    xml.attribute("src", cssbundles+bundle.getName());
    xml.attribute("bundled", "true");
    xml.attribute("minimized", Boolean.toString(min));
    xml.closeElement();
  }

  /**
   * Initialises the bundlers.
   *
   * @param env The environment.
   */
  private void init(Environment env) {
    // Initialise the JavaScript bundling tool
    String jsbundles = GlobalSettings.get("bastille.jsbundler.location", DEFAULT_BUNDLED_SCRIPTS);
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
      // only one path
      String p = replaceTokens(paths, service);
      File file = env.getPublicFile(p);
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

  /**
   * @return the configuration for the JavaScript bundle.
   */
  private static Properties getJSConfig() {
    Properties js = GlobalSettings.getNode("bastille.jsbundler.bundles");
    if (js == null || js.isEmpty()) js = DEFAULT_JS_BUNDLE;
    return js;
  }

  /**
   * @return the configuration for the CSS bundle.
   */
  private static Properties getCSSConfig() {
    Properties css = GlobalSettings.getNode("bastille.cssbundler.bundles");
    if (css == null || css.isEmpty()) css = DEFAULT_CSS_BUNDLE;
    return css;
  }

}
