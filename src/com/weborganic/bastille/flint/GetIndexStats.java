/*
 * Copyright (c) 2011 weborganic systems pty. ltd.
 */
package com.weborganic.bastille.flint;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.content.Cacheable;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentGeneratorBase;
import org.weborganic.berlioz.content.ContentRequest;
import org.weborganic.berlioz.content.Environment;

import com.topologi.diffx.xml.XMLWriter;

/**
 * Print some information about the index.
 * 
 * @author Christophe Lauret 
 * @version 0.6.0 - 31 May 2010
 * @since 0.6.0
 */
public class GetIndexStats extends ContentGeneratorBase implements ContentGenerator, Cacheable {

  /**
   * The ISO 8601 Date and time format
   * 
   * @see <a href="http://www.iso.org/iso/date_and_time_format">ISO: Numeric representation of Dates and Time</a>
   */
  private static final String ISO8601_DATETIME = "yyyy-MM-dd'T'HH:mm:ssZ";

  /**
   * Logger for debugging
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(GetIndexStats.class);

  /**
   * The index reader.
   */
  private IndexReader _reader = null;

  /**
   * {@inheritDoc} 
   */
  public String getETag(ContentRequest req) {
    Environment env = req.getEnvironment();
    String etag = null;
    try {
      FSDirectory directory = FSDirectory.open(env.getPrivateFile("index"));
      etag = env.getPrivateFile("index").getName()+"-"+IndexReader.lastModified(directory);
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
    Environment env = req.getEnvironment();
    FSDirectory directory = FSDirectory.open(env.getPrivateFile("index"));
    IndexReader reader = initIndexReader(directory);

    xml.openElement("index-stats");
    
    xml.openElement("index");
    xml.attribute("location", env.getPrivateFile("index").getName());
    xml.attribute("exists", Boolean.toString(reader != null));
    if (reader != null) {
      DateFormat iso = new SimpleDateFormat(ISO8601_DATETIME);
      xml.attribute("last-modified", iso.format(IndexReader.lastModified(reader.directory())));
      xml.attribute("current", Boolean.toString(reader.isCurrent()));
      xml.attribute("optimized", Boolean.toString(reader.isOptimized()));
    }
    xml.closeElement();

    if (reader != null) {
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
      }

      xml.closeElement();
    }

    xml.closeElement();
  }

  /**
   * 
   */
  private IndexReader initIndexReader(Directory directory) throws IOException {
    boolean exists = IndexReader.indexExists(directory);
    if (exists) {
      if (this._reader == null) {
        this._reader = IndexReader.open(directory, true);
      } else {
        IndexReader newReader = this._reader.reopen();
        if (newReader != this._reader) {
          // reader was reopened
          this._reader.close(); 
        }
        this._reader = newReader;        
      }
    } else this._reader = null;
    return this._reader;
  }
}
