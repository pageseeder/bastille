/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.weborganic.bastille.flint;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weborganic.bastille.flint.config.FlintConfig;
import org.weborganic.bastille.flint.helpers.Etags;
import org.weborganic.bastille.flint.helpers.IndexMaster;
import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.content.Cacheable;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentRequest;
import org.weborganic.berlioz.util.ISO8601;
import org.weborganic.flint.IndexException;
import org.weborganic.flint.local.LocalIndex;

import com.topologi.diffx.xml.XMLWriter;

/**
 * Print some information about the index.
 *
 * @author Christophe Lauret
 * @author Jean-Baptiste Reure
 *
 * @version 0.7.4 - 19 October 2012
 * @since 0.6.0
 */
public final class GetIndexStats implements ContentGenerator, Cacheable {

  /**
   * Logger for debugging
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(GetIndexStats.class);

  /**
   * To list only folders
   */
  private static final FileFilter FOLDERS_ONLY = new FileFilter() {
    @Override
    public boolean accept(File d) {
      return d.isDirectory();
    }
  };

  @Override
  public String getETag(ContentRequest req) {
    return Etags.getETag(req.getParameter("index"));
  }

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {
    // Getting the index
    xml.openElement("index-stats");
    File root = FlintConfig.directory();

    if (LocalIndex.exists(root)) {
      // single index, output it
      indexToXML(null, true, xml);

    } else {
      String indexName = req.getParameter("index");
      if (indexName != null) {
        indexToXML(indexName, false, xml);
      } else {
        // multiple indexes maybe
        File[] dirs = root.listFiles(FOLDERS_ONLY);
        if (dirs != null && dirs.length > 0) {
          for (File d : dirs) {
            indexToXML(d.getName(), false, xml);
          }
        } else {
          xml.openElement("index");
          xml.attribute("exists", "false");
          xml.closeElement();
        }
      }
    }
    xml.closeElement();
  }

  /**
   * Output the given index statistics as XML
   *
   * @param name  The name of the index.
   * @param terms <code>true</code> to include the terms in the index.
   * @param xml   The XML to write on.
   *
   * @throws IOException Should any IO error occur.
   */
  private void indexToXML(String name, boolean terms, XMLWriter xml) throws IOException {
    xml.openElement("index");
    if (name != null) xml.attribute("name", name);
    IndexMaster master = FlintConfig.getMaster(name);
    IndexReader reader = null;
    try {
      reader = master.grabReader();
    } catch (IndexException ex) {
      xml.attribute("error", "Failed to load reader: "+ex.getMessage());
    }
    if (reader != null) {
      try {
        xml.attribute("last-modified", ISO8601.DATETIME.format(IndexReader.lastModified(reader.directory())));
        xml.attribute("current", Boolean.toString(reader.isCurrent()));
        xml.attribute("optimized", Boolean.toString(reader.isOptimized()));
        // list docs
        xml.openElement("documents");
        xml.attribute("count", reader.numDocs());
        xml.attribute("max", reader.maxDoc());
        xml.closeElement();
        // terms
        if (terms) {
          xml.openElement("terms");
          TermEnum e = reader.terms();
          while (e.next()) {
            Term t = e.term();
            xml.openElement("term");
            xml.attribute("field", t.field());
            xml.attribute("text", t.text());
            xml.attribute("doc-freq", e.docFreq());
            xml.closeElement();
          }
          xml.closeElement();
        }
      } catch (IOException ex) {
        LOGGER.error("Error while extracting index statistics", ex);
      } finally {
        master.releaseSilently(reader);
      }
    } else {
      xml.attribute("error", "Null reader");
    }
    xml.closeElement();
  }

}
