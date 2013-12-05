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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weborganic.bastille.util.WebBundleTool;
import org.weborganic.bastille.web.BundleConfig.Type;
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
 * <p>A bundle config defines the list of bundles to create. The "default" config is made of three bundles
 * 'global', 'group', and 'service'.
 *
 * <p>Each bundle is specified using the <code>bundles</code> property each bundle name is mapped to the list
 * of files to bundle, the {GROUP} and {SERVICE} values are automatically replaced by the Berlioz
 * service/group name in use.
 *
 * <h4>Default configuration</h4>
 * <p>The default configuration is the equivalent of:</p>
 *
 * <pre>{@code
 * <bastille>
 *   <cssbundler minimize="true" location="/style/_/">
 *     <configs default="global,group,service"/>
 *     <bundles global="/style/global.css"
 *               group="/style/{GROUP}.css"
 *             service="/style/{GROUP}/{SERVICE}.css"/>
 *     <datauris threshold="4096"/>
 *   </cssbundler>
 *   <jsbundler minimize="true" location="/script/_/">
 *     <configs default="global,group,service"/>
 *     <bundles global="/style/global.js"
 *               group="/script/{GROUP}.js"
 *             service="/script/{GROUP}/{SERVICE}.js"/>
 *   </jsbundler>
 * </bastille>
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
 * @version 0.8.18 - 5 December 2013
 * @since 0.6.0
 */
public final class GetWebBundles implements ContentGenerator, Cacheable {

  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(GetWebBundles.class);

  /**
   * The CSS configs.
   */
  private static final Map<String, BundleConfig> CSS_CONFIGS = new HashMap<String, BundleConfig>();

  /**
   * The JS configs.
   */
  private static final Map<String, BundleConfig> JS_CONFIGS = new HashMap<String, BundleConfig>();

  /**
   * Indicates whether the bundle can be written..
   */
  private static volatile Boolean isWritable = null;

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
    String config = req.getParameter("config", "default");
    init(env, config);
    boolean doBundle = canBundle(req);
    if (doBundle) {
      try {
        long etag = 0L;
        BundleConfig js = getConfig(config, Type.JS);
        List<BundleInstance> jsbundles = js.instantiate(service, env);
        for (BundleInstance bundle : jsbundles) {
          List<File> files = bundle.files(env);
          File b = this.jstool.getBundle(files, bundle.name(), js.minimize());
          if (b != null && b.lastModified() > etag) etag = b.lastModified();
        }
        BundleConfig css = getConfig(config, Type.CSS);
        List<BundleInstance> cssbundles = js.instantiate(service, env);
        for (BundleInstance bundle : cssbundles) {
          List<File> files = bundle.files(env);
          File b = this.csstool.getBundle(files, bundle.name(), css.minimize());
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
    boolean doBundle = canBundle(req);
    String config = req.getParameter("config", "default");

    // Scripts
    BundleConfig js = getConfig(config, Type.JS);
    List<BundleInstance> jsbundles = js.instantiate(service, env);
    for (BundleInstance bundle : jsbundles) {
      if (doBundle) {
        List<File> files = bundle.files(env);
        bundleScripts(this.jstool, js, files, bundle.name(), xml);
      } else {
        List<String> paths = bundle.paths(env);
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
    BundleConfig css = getConfig(config, Type.CSS);
    List<BundleInstance> cssbundles = js.instantiate(service, env);
    for (BundleInstance bundle : cssbundles) {
      if (doBundle) {
        List<File> files = bundle.files(env);
        bundleStyles(this.csstool, css, files, bundle.name(), xml);
      } else {
        List<String> paths = bundle.paths(env);
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
   * @param config  The bundle config
   * @param files   The files to bundle.
   * @param name    The name of the bundle.
   * @param xml     The XML output
   *
   * @throws IOException Should an IO error occur
   */
  private void bundleScripts(WebBundleTool bundler, BundleConfig config, List<File> files, String name, XMLWriter xml) throws IOException {
    if (files.isEmpty()) return;
    boolean min = config.minimize();
    File bundle = bundler.bundle(files, name, min);
    LOGGER.debug("{} -> {}", files, bundle.getName());
    String location = config.location();
    xml.openElement("script", false);
    xml.attribute("src", location+bundle.getName());
    xml.attribute("bundled", "true");
    xml.attribute("minimized", Boolean.toString(min));
    xml.closeElement();
  }

  /**
   * Bundles the scripts using the specified bundler
   *
   * @param bundler The bundler to use.
   * @param config  The bundle config
   * @param files   The files to bundle.
   * @param name    The name of the bundle.
   * @param xml     The XML output
   *
   * @throws IOException Should an IO error occur
   */
  private void bundleStyles(WebBundleTool bundler, BundleConfig config, List<File> files, String name, XMLWriter xml) throws IOException {
    if (files.isEmpty()) return;
    boolean min = config.minimize();
    File bundle = bundler.bundle(files, name, min);
    LOGGER.debug("{} -> {}", files, bundle.getName());
    String location = config.location();
    xml.openElement("style", false);
    xml.attribute("src", location+bundle.getName());
    xml.attribute("bundled", "true");
    xml.attribute("minimized", Boolean.toString(min));
    xml.closeElement();
  }

  /**
   * Initialises the bundlers.
   *
   * <p>We check that the we can write bundles.
   *
   *
   * @param env The environment.
   */
  private synchronized void init(Environment env, String config) {
    // Initialise the JavaScript bundling tool
    BundleConfig js = getConfig(config, Type.JS);
    String jsbundles = js.location();
    File bundles = env.getPublicFile(jsbundles);
    if (!bundles.exists()) bundles.mkdirs();
    if (isWritable == null) {
      isWritable = Boolean.valueOf(bundles.exists() && bundles.canWrite());
    }
    if (this.jstool == null)
      this.jstool = new WebBundleTool(bundles);
    // Initialise the CSS bundling tool
    BundleConfig css =  getConfig(config, Type.CSS);
    String cssbundles = css.location();
    int threshold = GlobalSettings.get("bastille.cssbundler.datauris.threshold", 4096);
    File bundles2 = env.getPublicFile(cssbundles);
    if (!bundles2.exists()) bundles2.mkdirs();
    if (this.csstool == null) {
      this.csstool = new WebBundleTool(bundles2);
      this.csstool.setDataURIThreshold(threshold);
    }
  }

  /**
   * @param req content request
   * @return <code>true</code> if bundles folder is writable and "bundle-bundle" parameter is set to "false"
   */
  private static boolean canBundle(ContentRequest req) {
    return isWritable == Boolean.TRUE
        && !"false".equals(req.getParameter("berlioz-bundle", "true"));
  }

  /**
   *
   */
  private BundleConfig getConfig(String name, Type type) {
    Map<String, BundleConfig> configs = type == Type.JS? JS_CONFIGS : CSS_CONFIGS;
    BundleConfig config = configs.get(name);
    if (config == null) {
      config = BundleConfig.newInstance(name, type);
      configs.put(name, config);
    }
    return config;
  }

}
