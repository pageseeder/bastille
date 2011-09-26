/*
 * Copyright (c) 2011 weborganic systems pty. ltd.
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
import org.weborganic.berlioz.content.ContentGeneratorBase;
import org.weborganic.berlioz.content.ContentRequest;
import org.weborganic.berlioz.content.Environment;
import org.weborganic.berlioz.util.ISO8601;
import org.weborganic.flint.IndexException;

import com.topologi.diffx.xml.XMLWriter;
import com.weborganic.bastille.flint.helpers.MultipleIndex;
import com.weborganic.bastille.flint.helpers.SingleIndex;

/**
 * Print some information about the index.
 * 
 * @author Christophe Lauret 
 * @version 0.6.0 - 31 May 2010
 * @since 0.6.0
 */
public class GetIndexStats extends ContentGeneratorBase implements ContentGenerator, Cacheable {

  /**
   * Logger for debugging
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(GetIndexStats.class);

  /**
   * To list only folders
   */
  private static final FileFilter FOLDERS_ONLY = new FileFilter() {
    public boolean accept(File d) {
      return d.isDirectory();
    }
  };
  
  /**
   * {@inheritDoc} 
   */
  public String getETag(ContentRequest req) {
    Environment env = req.getEnvironment();
    String etag = null;
    try {
      File index = env.getPrivateFile("index");
      FSDirectory directory = FSDirectory.open(index);
      if (IndexReader.indexExists(directory)) {
        long modified = IndexReader.lastModified(directory);
        etag = env.getPrivateFile("index").getName()+"-"+modified;
      } else {
        File[] indexes = index.listFiles(FOLDERS_ONLY);
        if (indexes != null) {
          etag = "";
          for (File indexDir : indexes) {
            FSDirectory fsd = FSDirectory.open(index);
            if (IndexReader.indexExists(fsd))
              etag = indexDir.getName()+"-"+IndexReader.lastModified(fsd);
          }
          if (etag.length() == 0) etag = null;
        }
      }
    } catch (IOException ex) {
      LOGGER.debug("Error while trying to get last modified date of index", ex);
      return etag = null;
    }
    return etag;
  }

  /**
   * {@inheritDoc}
   */
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {

    // Getting the index
    xml.openElement("index-stats");
    File xslt = req.getEnvironment().getPrivateFile("ixml/default.xsl");
    File indexRoot = req.getEnvironment().getPrivateFile("index");
    FSDirectory directory = FSDirectory.open(indexRoot);
    if (IndexReader.indexExists(directory)) {
      // single index, output it
      indexToXML(indexRoot, xslt, null, xml);
    } else {
      // multiple indexes maybe
      File[] dirs = indexRoot.listFiles(FOLDERS_ONLY);
      if (dirs != null && dirs.length > 0) {
        for (File d : dirs)
          indexToXML(indexRoot, xslt, d.getName(), xml);
      } else {
        xml.openElement("index");
        xml.attribute("exists", "false");
        xml.closeElement();
      }
    }
    xml.closeElement();
  }
  /**
   * Output the given index as XML
   * @param env
   * @param name
   * @param xml
   * @throws IOException
   * @throws IndexException 
   */
  private void indexToXML(File indexRoot, File xsl, String name, XMLWriter xml) throws IOException {
    xml.openElement("index");
    if (name != null) xml.attribute("name", name);
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
      xml.attribute("last-modified", ISO8601.DATETIME.format(IndexReader.lastModified(reader.directory())));
      xml.attribute("current", Boolean.toString(reader.isCurrent()));
      xml.attribute("optimized", Boolean.toString(reader.isOptimized()));
      // list docs
      xml.openElement("documents");
      xml.attribute("count", reader.numDocs());
      xml.closeElement();
      xml.openElement("terms");
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
      xml.closeElement();
    }
    xml.closeElement();

  }
}
