/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.weborganic.bastille.web;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weborganic.bastille.util.WebBundleTool;
import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.GlobalSettings;
import org.weborganic.berlioz.content.Cacheable;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentRequest;
import org.weborganic.berlioz.content.Environment;
import org.weborganic.berlioz.content.Service;
import org.weborganic.berlioz.servlet.HttpContentRequest;

import com.topologi.diffx.xml.XMLWriter;

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
 * @version 0.7.11 - 3 December 2012
 * @since 0.6.0
 */
public final class GetWebBundles implements ContentGenerator, Cacheable {

  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(GetWebBundles.class);

  /** Where the bundled scripts should be located. */
  private static final String DEFAULT_BUNDLED_SCRIPTS = "/script/_/";

  /** Where the bundled styles should be located. */
  private static final String DEFAULT_BUNDLED_STYLES = "/style/_/";

  /** The default bundle config. */
  private static final String[] DEFAULT_BUNDLE_CONFIG = new String[]{ "global", "group", "service" };

  /** The default JavaScript bundle. */
  private static final Map<String, BundleConfig> DEFAULT_JS_BUNDLE = new HashMap<String, BundleConfig>();
  static {
    DEFAULT_JS_BUNDLE.put("global",  new BundleConfig("global",  "global",    "/script/global.js"));
    DEFAULT_JS_BUNDLE.put("group",   new BundleConfig("group",   "{GROUP}",   "/script/{GROUP}.js"));
    DEFAULT_JS_BUNDLE.put("service", new BundleConfig("service", "{SERVICE}", "/script/{GROUP}/{SERVICE}.js"));
  }

  /** The default CSS bundle. */
  private static final Map<String, BundleConfig> DEFAULT_CSS_BUNDLE = new HashMap<String, BundleConfig>();
  static {
    DEFAULT_CSS_BUNDLE.put("global",  new BundleConfig("global",  "global",    "/style/global.css"));
    DEFAULT_CSS_BUNDLE.put("group",   new BundleConfig("group",   "{GROUP}",   "/style/{GROUP}.css"));
    DEFAULT_CSS_BUNDLE.put("service", new BundleConfig("service", "{SERVICE}", "/style/{GROUP}/{SERVICE}.css"));
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
      String config = req.getParameter("config", "default");
      try {
        long etag = 0L;
        List<BundleConfig> js = getJSConfig(config);
        for (BundleConfig bundle : js) {
          boolean min = GlobalSettings.get("bastille.jsbundler.minimize", true);
          String name = replaceTokens(bundle.filename(), service);
          List<File> files = getFiles(bundle.paths(), service, env);
          File b = this.jstool.getBundle(files, name, min);
          if (b != null && b.lastModified() > etag) etag = b.lastModified();
        }
        List<BundleConfig> css = getCSSConfig(config);
        for (BundleConfig bundle : css) {
          boolean min = GlobalSettings.get("bastille.cssbundler.minimize", true);
          String name = replaceTokens(bundle.filename(), service);
          List<File> files = getFiles(bundle.paths(), service, env);
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
    String config = req.getParameter("config", "default");

    // Scripts
    List<BundleConfig> js = getJSConfig(config);
    for (BundleConfig bundle : js) {
      String name = replaceTokens(bundle.filename(), service);
      if (doBundle) {
        List<File> files = getFiles(bundle.paths(), service, env);
        bundleScripts(this.jstool, files, name, xml);
      } else {
        List<String> paths = getPathsOnly(bundle.paths(), service, env);
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
    List<BundleConfig> css = getCSSConfig(config);
    for (BundleConfig bundle : css) {
      String name = replaceTokens(bundle.filename(), service);
      if (doBundle) {
        List<File> files = getFiles(bundle.paths(), service, env);
        bundleStyles(this.csstool, files, name, xml);
      } else {
        List<String> paths = getPathsOnly(bundle.paths(), service, env);
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
    int threshold = GlobalSettings.get("bastille.cssbundler.datauris.threshold", 4096);
    File bundles2 = env.getPublicFile(cssbundles);
    if (!bundles2.exists()) bundles2.mkdirs();
    if (this.csstool == null) {
      this.csstool = new WebBundleTool(bundles2);
      this.csstool.setDataURIThreshold(threshold);
    }
  }

  /**
   * Returns the files in the bundle filtering out files which do not exist and automatically replacing tokens.
   *
   * @param paths   The list of paths
   * @param service The service
   * @param env     The environment
   * @return the list of files to bundles.
   */
  private static List<String> getPathsOnly(String[] paths, Service service, Environment env) {
    if (paths == null) return Collections.emptyList();
    if (paths.length > 1) {
      // multiple paths specified
      List<String> existing = new ArrayList<String>();
      for (String path : paths) {
        String p = replaceTokens(path, service);
        File file = env.getPublicFile(p);
        if (file.exists()) existing.add(p);
      }
      return existing;
    } else if (paths.length == 1) {
      // only one path
      String p = replaceTokens(paths[0], service);
      File file = env.getPublicFile(p);
      if (file.exists()) return Collections.singletonList(p);
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
  private static List<File> getFiles(String[] paths, Service service, Environment env) {
    if (paths == null) return Collections.emptyList();
    if (paths.length > 1) {
      // multiple paths specified
      List<File> files = new ArrayList<File>();
      for (String path : paths) {
        String p = replaceTokens(path, service);
        File file = env.getPublicFile(p);
        if (file.exists()) files.add(file);
      }
      return files;
    } else if (paths.length == 1) {
      // only one paths
      String p = replaceTokens(paths[0], service);
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
   * @param name the name of the JavaScript bundles to load for the specified configuration name.
   * @return the configuration for the JavaScript bundle.
   */
  private static List<BundleConfig> getJSConfig(String name) {
    String[] names = getBundleNames("bastille.jsbundler.configs."+name);
    LOGGER.debug("Config:{} (JS) => {}", name, names);
    List<BundleConfig> js = toBundleConfigs(names, "bastille.jsbundler.bundles.", DEFAULT_JS_BUNDLE);
    return js;
  }

  /**
   * @param name the name of the CSS bundles to load for the specified configuration name.
   * @return the configuration for the CSS bundle.
   */
  private static List<BundleConfig> getCSSConfig(String name) {
    String[] names = getBundleNames("bastille.cssbundler.configs."+name);
    LOGGER.debug("Config:{} (CSS) => {}", name, names);
    List<BundleConfig> css = toBundleConfigs(names, "bastille.cssbundler.bundles.", DEFAULT_CSS_BUNDLE);
    return css;
  }

  /**
   * Returns The configuration for the specified property.
   *
   * @param property the name of the property in the global config.
   *
   * @return The corresponding bundle names.
   */
  private static String[] getBundleNames(String property) {
    String names = GlobalSettings.get(property);
    return names != null? names.split(",") : DEFAULT_BUNDLE_CONFIG;
  }

  /**
   * Returns the list of bundle configurations for the specified names from the global settings
   * and falling back on the defaults defined in this class.
   *
   * @param names    the names of the bundle configuration to get.
   * @param prefix   the prefix in the global properties
   * @param defaults the default bundle configurations.
   *
   * @return The corresponding list.
   */
  private static List<BundleConfig> toBundleConfigs(String[] names, String prefix, Map<String, BundleConfig> defaults) {
    List<BundleConfig> bundles = new ArrayList<BundleConfig>();
    for (String name : names) {
      BundleConfig bc = defaults.get(name);
      // Same as the name if the 'filename' sub-property isn't defined
      String filename = GlobalSettings.get(prefix + name + ".filename", name);
      // The value of the property if the 'paths' sub-property isn't defined.
      String paths = GlobalSettings.get(prefix + name + ".include", GlobalSettings.get(prefix + name));
      if (paths != null) {
        bc = new BundleConfig(name, filename, paths);
      }
      if (bc != null) {
        bundles.add(bc);
      } else {
        LOGGER.warn("Bundle '{}' is undefined", name);
      }
    }
    return bundles;
  }

  /**
   * Holds the basic configurations for a bundle.
   */
  private static final class BundleConfig implements Serializable {

    /** As per requirement for <code>Serializable</code> */
    private static final long serialVersionUID = -663743071617576797L;

    /**
     * The name of the bundle.
     */
    private final String _name;

    /**
     * The name to use of the filename of the bundle.
     */
    private final String _filename;

    /**
     * The list of paths to include in the bundle.
     */
    private final String[] _paths;

    /**
     * @param name     The name of the bundle.
     * @param filename The name to use of the filename of the bundle.
     * @param paths    The list of paths to include in the bundle.
     */
    public BundleConfig(String name, String filename, String paths) {
      this._name = name;
      this._filename = filename;
      this._paths = paths.split(",");
    }

    /**
     * @return The name of the bundle.
     */
    public String name() {
      return this._name;
    }

    /**
     * @return The name to use of the filename of the bundle.
     */
    public String filename() {
      return this._filename;
    }

    /**
     * @return The list of paths to include in the bundle.
     */
    public String[] paths() {
      return this._paths;
    }
  }

}
