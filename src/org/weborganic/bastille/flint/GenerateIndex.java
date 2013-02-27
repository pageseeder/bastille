/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.weborganic.bastille.flint;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weborganic.bastille.flint.config.FlintConfig;
import org.weborganic.bastille.flint.config.IFlintConfig;
import org.weborganic.bastille.flint.helpers.IndexMaster;
import org.weborganic.bastille.flint.helpers.IndexUpdateFilter;
import org.weborganic.bastille.flint.helpers.IndexUpdateFilter.Action;
import org.weborganic.bastille.psml.PSMLConfig;
import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentRequest;
import org.weborganic.berlioz.content.Environment;
import org.weborganic.berlioz.util.FileUtils;
import org.weborganic.berlioz.util.ISO8601;
import org.weborganic.flint.util.FileCollector;

import com.topologi.diffx.xml.XMLWriter;

/**
 * List the files corresponding to the specified directory.
 *
 * <p>If there is a <code>index</code> parameter, then only the index with this ID will be created.
 * <p>If there is a <code>folder</code> parameter, only XML files that are descendants of this folder are indexed.
 *
 * <p>This content generator is not cacheable because it causes the index to be updated using a
 * separate thread.
 *
 * <p>The index must be located in the '/index' directory.
 * The IXML stylesheet must be 'ixml/default.xsl'.
 *
 * <p>Note: access to this is generator should be made secured in the Web descriptor.
 *
 * @author Christophe Lauret
 *
 * @version 0.8.8 - 25 February 2013
 * @since 0.6.0
 */
public final class GenerateIndex implements ContentGenerator  {

  /**
   * Logger for debugging
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(GenerateIndex.class);

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {

    // Getting the index
    final Environment env = req.getEnvironment();

    // Index template
    String index = req.getParameter("index");
    String folder = req.getParameter("folder");

    // retrieve it from the multiple indexes
    IFlintConfig config = FlintConfig.get();
    IndexMaster master = FlintConfig.getMaster(index);

    long modified = master.lastModified();
    List<File> indexed = new ArrayList<File>();
    indexed.addAll(master.list());

    // Identify the directory
    File root;
    File psml = PSMLConfig.getRoot(false);
    if (psml.exists() && psml.isDirectory()) {
      root = folder == null ? psml : new File(psml, folder);
    } else {
      root = folder == null ? env.getPrivateFile("xml") : new File(env.getPrivateFile("xml"), folder);
    }

    LOGGER.debug("Scanning directory {} for files updated since {}", root.getPath(), ISO8601.DATETIME.format(modified));

    // Force index all
    if ("true".equals(req.getParameter("all"))) {
      config.reload();
      modified = 0;
    }

    // Identify the files
    IndexUpdateFilter filter = new IndexUpdateFilter(modified, indexed);
    int updated = FileCollector.list(root, filter).size();
    Map<File, Action> files = filter.getActions();

    // Send the files for indexing
    xml.openElement("index-job", true);
    if (index != null) xml.attribute("index", index);
    xml.attribute("last-modified", ISO8601.format(modified, ISO8601.DATETIME));
    for (Entry<File, Action> entry : files.entrySet()) {
      File f = entry.getKey();
      Action action = entry.getValue();
      String path = config.toPath(entry.getKey());
      toXML(xml, path, ISO8601.format(f.lastModified(), ISO8601.DATETIME), action.toString());
      // Parameters send to iXML
      if (action == Action.INSERT || action == Action.UPDATE) {
        Map<String, String> p = new HashMap<String, String>();
        p.put("path", path);
        p.put("visibility", "private");
        p.put("mediatype", FileUtils.getMediaType(f));
        p.put("last-modified", ISO8601.format(f.lastModified(), ISO8601.DATETIME));
        master.index(f, p);
      } else if (action == Action.DELETE) {
        Map<String, String> p = Collections.emptyMap();
        master.index(f, p);
      }
    }
    LOGGER.debug("{} files queued for indexing", updated);
    xml.closeElement();
  }

  /**
   * XML for a file to be indexed.
   *
   * @param xml      the XML writer.
   * @param modified when the file was last modified.
   * @param path     the path to the file.
   * @param action   the action the indexer should take.
   *
   * @throws IOException If thrown by the xml writer
   */
  public void toXML(XMLWriter xml, String path, String modified, String action) throws IOException {
    xml.openElement("file");
    xml.attribute("path", path);
    xml.attribute("last-modified", modified);
    xml.attribute("action", action);
    xml.closeElement();
  }

}
