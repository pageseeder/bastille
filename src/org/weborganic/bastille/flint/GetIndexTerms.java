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
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weborganic.bastille.flint.config.FlintConfig;
import org.weborganic.bastille.flint.helpers.IndexMaster;
import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.content.Cacheable;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentRequest;
import org.weborganic.berlioz.content.Environment;
import org.weborganic.flint.IndexException;

import com.topologi.diffx.xml.XMLWriter;

/**
 * List the terms from the index.
 *
 * @author Christophe Lauret
 * @author Jean-Baptiste Reure
 *
 * @version 0.7.4 - 19 October 2012
 * @since 0.6.0
 */
public final class GetIndexTerms implements ContentGenerator, Cacheable {

  /**
   * Logger for debugging
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(GetIndexTerms.class);

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
    Environment env = req.getEnvironment();
    StringBuilder etag = new StringBuilder();
    try {
      File index = env.getPrivateFile("index");
      FSDirectory directory = FSDirectory.open(index);
      if (IndexReader.indexExists(directory)) {
        long modified = IndexReader.lastModified(directory);
        etag.append(env.getPrivateFile("index").getName()).append('-').append(modified);
      } else {
        String indexName = req.getParameter("index");
        if (indexName != null) {
          FSDirectory fsd = FSDirectory.open(index);
          if (IndexReader.indexExists(fsd))
            etag.append(indexName).append('-').append(IndexReader.lastModified(fsd));
        } else {
          File[] indexes = index.listFiles(FOLDERS_ONLY);
          if (indexes != null) {
            for (File indexDir : indexes) {
              FSDirectory fsd = FSDirectory.open(index);
              if (IndexReader.indexExists(fsd))
                etag.append(indexDir.getName()).append('-').append(IndexReader.lastModified(fsd));
            }
          }
        }
      }
    } catch (IOException ex) {
      LOGGER.debug("Error while trying to get last modified date of index", ex);
    }
    return etag.length() > 0? etag.toString() : null;
  }

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {
    // Getting the index
    File indexRoot = FlintConfig.directory();
    FSDirectory directory = FSDirectory.open(indexRoot);

    if (IndexReader.indexExists(directory)) {
      // single index, output it
      termsToXML(null, xml);

    } else {
      String indexName = req.getParameter("index");
      if (indexName != null) {
        termsToXML(indexName, xml);
      } else {
        // multiple indexes maybe
        File[] dirs = indexRoot.listFiles(FOLDERS_ONLY);
        if (dirs != null && dirs.length > 0) {
          for (File d : dirs) {
            termsToXML(d.getName(), xml);
          }
        } else {
          xml.openElement("terms");
          xml.attribute("error", "No index");
          xml.closeElement();
        }
      }
    }
  }

  /**
   * Output the given terms in the index as XML
   *
   * @param name The name of the index.
   * @param xml  The XML to write on.
   *
   * @throws IOException Should any IO error occur.
   */
  private void termsToXML(String name, XMLWriter xml) throws IOException {
    xml.openElement("terms");
    IndexReader reader = null;
    IndexMaster master = FlintConfig.getMaster(name);
    try {
      reader = master.grabReader();
    } catch (IndexException ex) {
      xml.attribute("error", "Failed to load reader: "+ex.getMessage());
    }
    if (reader != null) {
      try {
        TermEnum e = reader.terms();
        while (e.next()) {
          Term t = e.term();
          xml.openElement("term");
          xml.attribute("field", t.field());
          xml.attribute("text", t.text());
          xml.attribute("doc-freq", e.docFreq());
          xml.closeElement();
        }
      } catch (IOException ex) {
        LOGGER.error("Error while extracting term statistics", ex);
      } finally {
        master.releaseSilently(reader);
      }
    } else {
      xml.attribute("error", "Reader is null");
    }
    xml.closeElement();
  }
}
