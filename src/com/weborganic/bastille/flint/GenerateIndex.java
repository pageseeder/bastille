/*
 * Copyright (c) 2011 weborganic systems pty. ltd.
 */
package com.weborganic.bastille.flint;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.lucene.index.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentGeneratorBase;
import org.weborganic.berlioz.content.ContentRequest;
import org.weborganic.berlioz.content.Environment;
import org.weborganic.berlioz.util.ISO8601;
import org.weborganic.flint.util.FileCollector;

import com.topologi.diffx.xml.XMLWriter;
import com.weborganic.bastille.flint.helpers.FilePathRule;
import com.weborganic.bastille.flint.helpers.IndexMaster;
import com.weborganic.bastille.flint.helpers.IndexUpdateFilter;
import com.weborganic.bastille.flint.helpers.IndexUpdateFilter.Action;
import com.weborganic.bastille.flint.helpers.MultipleIndex;
import com.weborganic.bastille.flint.helpers.SingleIndex;

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
 * @version 0.6.0 - 26 July 2010
 * @since 0.6.0
 */
public class GenerateIndex extends ContentGeneratorBase implements ContentGenerator  {

  /**
   * Logger for debugging
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(GenerateIndex.class);

  /**
   * {@inheritDoc}
   */
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {

    // Getting the index
    final Environment env = req.getEnvironment();
    IndexMaster master;
    long modified = 0;
    List<File> indexed = new ArrayList<File>();

    String index = req.getParameter("index");
    String folder = req.getParameter("folder");
    if (index == null) {
      master = SingleIndex.setupMaster(env.getPrivateFile("ixml/default.xsl"));
      modified = master.lastModified();
      indexed.addAll(master.list(new Term("visibility", "private")));
    } else {
      // retrieve it from the multiple indexes
      File indexDir = env.getPrivateFile("index/"+index);
      master = MultipleIndex.setupMaster(indexDir, env.getPrivateFile("ixml/default.xsl"));
      modified = master.lastModified();
      indexed.addAll(master.list(new Term("visibility", "private")));
    }

    // Scanning the directory
    File root = folder == null ? env.getPrivateFile("xml") : new File(env.getPrivateFile("xml"), folder);
    LOGGER.debug("Scanning XML directory {}", root);

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
      String path = FilePathRule.toPath(entry.getKey());
      toXML(xml, path, ISO8601.format(f.lastModified(), ISO8601.DATETIME), action.toString());
      // Parameters send to iXML
      if (action == Action.INSERT || action == Action.UPDATE) {
        Map<String, String> p = new HashMap<String, String>();
        p.put("path", path);
        p.put("visibility", "private");
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
   * XML for a file to be indexed 
   * @param xml  the XML writer
   * @param path the path to the file.
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
