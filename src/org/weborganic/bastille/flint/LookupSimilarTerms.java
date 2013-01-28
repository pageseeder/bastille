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
import java.util.List;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weborganic.bastille.flint.config.FlintConfig;
import org.weborganic.bastille.flint.helpers.IndexMaster;
import org.weborganic.bastille.flint.helpers.MultipleIndex;
import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.content.Cacheable;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentRequest;
import org.weborganic.berlioz.util.MD5;
import org.weborganic.flint.IndexException;
import org.weborganic.flint.util.Bucket;
import org.weborganic.flint.util.Bucket.Entry;
import org.weborganic.flint.util.Terms;

import com.topologi.diffx.xml.XMLWriter;

/**
 * Lookup the similar terms for the specified term.
 *
 * <p>Generate an ETag based on the parameters and the last modified date of the index.
 *
 * @author Christophe Lauret
 * @version 0.6.0 - 28 May 2010
 * @since 0.6.0
 */
public final class LookupSimilarTerms implements ContentGenerator, Cacheable {

  /**
   * Logger for debugging
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(LookupSimilarTerms.class);

  @Override public String getETag(ContentRequest req) {
    StringBuilder etag = new StringBuilder();
    // Get relevant parameters
    etag.append(req.getParameter("term", "keyword")).append('%');
    etag.append(req.getParameter("field", "fulltext")).append('%');
    // Get last time index was modified
    IndexMaster master = FlintConfig.getMaster();
    if (master != null) {
      etag.append(master.lastModified());
    }
    // MD5 of computed etag value
    return MD5.hash(etag.toString());
  }

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {
    // Create a new query object
    String[] fields = req.getParameter("field", "fulltext").split(",");
    String text = req.getParameter("term", "keyword");

    LOGGER.debug("Looking up fuzzy terms for {} in {}", text, fields);
    xml.openElement("similar-terms");

    // Start the search
    String index = req.getParameter("index", "");
    if (index.length() > 0) {
      // find all indexes specified
      String[] indexeNames = index.split(",");
      List<File> indexDirectories = new ArrayList<File>();
      for (String ind : indexeNames) {
        indexDirectories.add(req.getEnvironment().getPrivateFile("index/"+ind));
      }
      MultipleIndex indexes = new MultipleIndex(indexDirectories);
      // grab a reader
      MultipleIndex.MultipleIndexReader multiReader = indexes.getMultiReader();
      try {
        IndexReader reader = multiReader.grab();
        Bucket<Term> bucket = new Bucket<Term>(20);
        // run fuzzy and prefix searches
        // search in all fields
        for (String field : fields) {
          Term term = new Term(field, text);
          Terms.fuzzy(reader, bucket, term);
          Terms.prefix(reader, bucket, term);
        }
        // output to XML
        for (Entry<Term> e : bucket.entrySet()) {
          Terms.toXML(xml, e.item(), e.count());
        }
      } catch (IOException ex) {
        throw new BerliozException("Exception thrown while fetching fuzzy terms", ex);
      } catch (IndexException ex) {
        throw new BerliozException("Exception thrown while fetching fuzzy terms", ex);
      } finally {
        // release the reader
        multiReader.releaseSilently();
      }
    } else {
      IndexMaster master = FlintConfig.getMaster();
      if (master != null) {
        IndexReader reader = null;
        try {
          Bucket<Term> bucket = new Bucket<Term>(20);
          // grab a reader
          reader = master.grabReader();
          // run fuzzy and prefix searches
          // search in all fields
          for (String field : fields) {
            Term term = new Term(field, text);
            Terms.fuzzy(reader, bucket, term);
            Terms.prefix(reader, bucket, term);
          }
          // output to XML
          for (Entry<Term> e : bucket.entrySet()) {
            Terms.toXML(xml, e.item(), e.count());
          }
        } catch (IOException ex) {
          throw new BerliozException("Exception thrown while fetching fuzzy terms", ex);
        } catch (IndexException ex) {
          throw new BerliozException("Exception thrown while fetching fuzzy terms", ex);
        } finally {
          // release the reader
          master.releaseSilently(reader);
        }
      }
    }

    xml.closeElement();
  }

}
