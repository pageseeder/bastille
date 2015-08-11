/*
 * Copyright 2015 Allette Systems (Australia)
 * http://www.allette.com.au
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pageseeder.bastille.flint;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.pageseeder.bastille.flint.config.FlintConfig;
import org.pageseeder.bastille.flint.config.IFlintConfig;
import org.pageseeder.bastille.flint.helpers.IndexMaster;
import org.pageseeder.bastille.flint.helpers.IndexUpdateFilter;
import org.pageseeder.bastille.flint.helpers.IndexUpdateFilter.Action;
import org.pageseeder.bastille.psml.PSMLConfig;
import org.pageseeder.berlioz.BerliozException;
import org.pageseeder.berlioz.content.ContentGenerator;
import org.pageseeder.berlioz.content.ContentRequest;
import org.pageseeder.berlioz.content.Environment;
import org.pageseeder.berlioz.util.FileUtils;
import org.pageseeder.berlioz.util.ISO8601;
import org.pageseeder.flint.util.FileCollector;
import org.pageseeder.xmlwriter.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  /**
   * The max number of documents to return.
   */
  private static final int MAX_FILES_RETURNED = 1000;

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
    boolean indexAll = "true".equals(req.getParameter("all"));
    if (indexAll) {
      config.reload();
      modified = 0;
    }

    // Identify the files
    IndexUpdateFilter filter = new IndexUpdateFilter(modified, indexed);
    int updated = FileCollector.list(root, filter).size(); // TODO this could consume much less memory
    Map<File, Action> files = filter.getActions();

    // Actually counts the number of files returned in the interface
    int count=0;

    // Send the files for indexing
    xml.openElement("index-job", true);
    if (index != null) {
      xml.attribute("index", index);
    }
    xml.attribute("last-modified", ISO8601.format(modified, ISO8601.DATETIME));
    for (Entry<File, Action> entry : files.entrySet()) {
      File f = entry.getKey();
      Action action = entry.getValue();
      String path = config.toPath(entry.getKey());
      if (count < MAX_FILES_RETURNED) {
        // XXX: This is a pretty cheap way of fixing this, but we'll refactor this soon
        toXML(xml, path, ISO8601.format(f.lastModified(), ISO8601.DATETIME), action.toString());
        count++;
      }
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
