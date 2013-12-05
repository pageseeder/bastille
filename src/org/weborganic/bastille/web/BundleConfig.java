/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.weborganic.bastille.web;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weborganic.bastille.util.WebBundleTool;
import org.weborganic.berlioz.GlobalSettings;
import org.weborganic.berlioz.content.Environment;
import org.weborganic.berlioz.content.Service;

/**
 * The configuration for the bundling for a given type.
 *
 * <p>Stores the bundles definitions and instantiate bundles.
 *
 * @author Christophe Lauret
 * @version 5 December 2013
 */
public class BundleConfig implements Serializable {

  /** Serializable */
  private static final long serialVersionUID = 5709906856099064344L;

  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(BundleConfig.class);

  /**
   * The default bundle config.
   */
  private static final String[] DEFAULT_BUNDLE_CONFIG = new String[]{ "global", "group", "service" };

  /**
   * The default JavaScript bundle definitions
   */
  private static final Map<String, BundleDefinition> DEFAULT_JS_BUNDLE = new HashMap<String, BundleDefinition>();
  static {
    DEFAULT_JS_BUNDLE.put("global",  new BundleDefinition("global",  "global",    "/script/global.js"));
    DEFAULT_JS_BUNDLE.put("group",   new BundleDefinition("group",   "{GROUP}",   "/script/{GROUP}.js"));
    DEFAULT_JS_BUNDLE.put("service", new BundleDefinition("service", "{SERVICE}", "/script/{GROUP}/{SERVICE}.js"));
  }

  /**
   * The default CSS bundle definitions
   */
  private static final Map<String, BundleDefinition> DEFAULT_CSS_BUNDLE = new HashMap<String, BundleDefinition>();
  static {
    DEFAULT_CSS_BUNDLE.put("global",  new BundleDefinition("global",  "global",    "/style/global.css"));
    DEFAULT_CSS_BUNDLE.put("group",   new BundleDefinition("group",   "{GROUP}",   "/style/{GROUP}.css"));
    DEFAULT_CSS_BUNDLE.put("service", new BundleDefinition("service", "{SERVICE}", "/style/{GROUP}/{SERVICE}.css"));
  }

  /** Where the bundled scripts should be located. */
  private static final String DEFAULT_BUNDLED_SCRIPTS = "/script/_/";

  /** Where the bundled styles should be located. */
  private static final String DEFAULT_BUNDLED_STYLES = "/style/_/";

  // TODO Use Web Bundle type
  public enum Type {JS, CSS};

  // Class attributes
  // ----------------------------------------------------------------------------------------------

  /**
   * The list of definitions in this configuration.
   */
  private final List<BundleDefinition> _definitions;

  /**
   * The type of bundle config.
   */
  private final Type _type;

  /**
   * Whether the code should be minimized as part of bundling.
   */
  private final boolean _minimize;

  /**
   * Whether the code should be minimized as part of bundling.
   */
  private final String _location;

  /**
   * The root of the web application.
   */
  private final File _root;

  /**
   * The tool used for bundling JS.
   */
  private WebBundleTool bundler = null;

  /**
   * Create a new config - use factory method instead.
   */
  private BundleConfig(List<BundleDefinition> definitions, Type type, boolean minimize, String location, File root) {
    this._definitions = null;
    this._type = type;
    this._minimize = minimize;
    this._location = location;
    this._root = root;
    // Initialise the bundler
    initBundler();
  }

  /**
   * @return bundle definitions.
   */
  public List<BundleDefinition> definitions() {
    return this._definitions;
  }

  /**
   * @return the type of bundle.
   */
  public Type type() {
    return this._type;
  }

  /**
   * @return <code>true</code> to minimize the code; <code>false</code> otherwise.
   */
  public boolean minimize() {
    return this._minimize;
  }

  /**
   * @return Path to where the bundles should be stored.
   */
  public String location() {
    return this._location;
  }

  /**
   * Creates new instance of a bundle configuration for the specific service.
   *
   * @return the corresponding configuration.
   */
  public List<BundleInstance> instantiate(Service service, Environment env) {
    List<BundleInstance> instances = new ArrayList<BundleInstance>();
    for (BundleDefinition def : this._definitions) {
      BundleInstance instance = BundleInstance.instantiate(this, def, service, env);
      instances.add(instance);
    }
    return instances;
  }

  /**
   * Creates new instance of a bundle configuration.
   *
   * @param name The name of the config.
   * @param type The type "js" or "css".
   * @param root The root of the web application.
   *
   * @return the corresponding configuration.
   */
  public static BundleConfig newInstance(String name, Type type, File root) {
    String lctype = type.name().toLowerCase();
    String[] names = getBundleNames("bastille."+lctype+"bundler.configs."+name);
    LOGGER.debug("Config:{} ("+type+") => {}", name, names);
    Map<String, BundleDefinition> defaults = Type.JS == type? DEFAULT_JS_BUNDLE : DEFAULT_CSS_BUNDLE;
    List<BundleDefinition> definitions = toBundleConfigs(names, "bastille."+lctype+"bundler.bundles.", defaults);
    boolean minimize = GlobalSettings.get("bastille."+lctype+"bundler.minimize", true);
    String defaultLocation = getDefaultLocation(type);
    String location = GlobalSettings.get("bastille."+lctype+"bundler.location", defaultLocation);
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Loading bundle config:{} ({})", name, type);
      LOGGER.debug("Bundler settings minimize:{} location:{}", minimize, location);
      for (BundleDefinition d : definitions) {
        LOGGER.debug("{} -> {} ({})", d.name(), d.filename(), d.paths());
      }
    }
    return new BundleConfig(definitions, type, minimize, location, root);
  }

  public static String getDefaultLocation(Type type) {
    return Type.JS == type? DEFAULT_BUNDLED_SCRIPTS : DEFAULT_BUNDLED_STYLES;
  }

  // private helpers
  // ----------------------------------------------------------------------------------------------

  private void initBundler() {
    // Initialise the bundler
    this.bundler = new WebBundleTool(new File(this._root, this._location));
    if (this._type == Type.CSS) {
       int threshold = GlobalSettings.get("bastille.cssbundler.datauris.threshold", 4096);
       this.bundler.setDataURIThreshold(threshold);
    }
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
  private static List<BundleDefinition> toBundleConfigs(String[] names, String prefix, Map<String, BundleDefinition> defaults) {
    List<BundleDefinition> bundles = new ArrayList<BundleDefinition>();
    for (String name : names) {
      BundleDefinition bc = defaults.get(name);
      // Same as the name if the 'filename' sub-property isn't defined
      String filename = GlobalSettings.get(prefix + name + ".filename", name);
      // The value of the property if the 'paths' sub-property isn't defined.
      String paths = GlobalSettings.get(prefix + name + ".include", GlobalSettings.get(prefix + name));
      if (paths != null) {
        bc = new BundleDefinition(name, filename, paths);
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

}
