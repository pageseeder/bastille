/*
 * This file is part of the Bastille library.
 *
 * Available under a commercial licence, contact Weborganic.
 *
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package com.weborganic.bastille.flint;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.content.Cacheable;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentRequest;
import org.weborganic.berlioz.content.Environment;
import org.weborganic.flint.IndexException;

import com.topologi.diffx.xml.XMLWriter;
import com.weborganic.bastille.flint.helpers.MultipleIndex;
import com.weborganic.bastille.flint.helpers.SingleIndex;

/**
 * List the terms from the index.
 *
 * @author Christophe Lauret
 * @author Jean-Baptiste Reure
 *
 * @version 0.6.20 - 27 September 2011
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

  /**
   * {@inheritDoc}
   */
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

  /**
   * {@inheritDoc}
   */
  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {
    // Getting the index
    File xslt = req.getEnvironment().getPrivateFile("ixml/default.xsl");
    File indexRoot = req.getEnvironment().getPrivateFile("index");
    FSDirectory directory = FSDirectory.open(indexRoot);

    if (IndexReader.indexExists(directory)) {
      // single index, output it
      termsToXML(indexRoot, xslt, null, xml);

    } else {
      String indexName = req.getParameter("index");
      if (indexName != null) {
        termsToXML(indexRoot, xslt, indexName, xml);
      } else {
        // multiple indexes maybe
        File[] dirs = indexRoot.listFiles(FOLDERS_ONLY);
        if (dirs != null && dirs.length > 0) {
          for (File d : dirs) {
            termsToXML(indexRoot, xslt, d.getName(), xml);
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
   * Output the given index as XML
   * @param env
   * @param name
   * @param xml
   * @throws IOException
   * @throws IndexException
   */
  private void termsToXML(File indexRoot, File xsl, String name, XMLWriter xml) throws IOException {
    xml.openElement("terms");
    File root = null;
    IndexReader reader = null;
    try {
      if (name == null) {
        root = indexRoot;
        reader = SingleIndex.master().grabReader();
      } else {
        root = new File(indexRoot, name);
        reader = MultipleIndex.getMaster(root).grabReader();
      }
    } catch (IndexException e) {
      xml.attribute("error", "Failed to load reader: "+e.getMessage());
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
      } catch (Exception ex) {
        LOGGER.error("Error while extracting term statistics", ex);
      } finally {
        if (name == null) {
          SingleIndex.master().releaseSilently(reader);
        } else if (root != null) {
          MultipleIndex.getMaster(root).releaseSilently(reader);
        }
      }
    } else {
      xml.attribute("error", "Reader is null");
    }
    xml.closeElement();
  }
}
