package com.weborganic.bastille.flint;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.content.Cacheable;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentRequest;
import org.weborganic.berlioz.util.MD5;
import org.weborganic.flint.IndexException;
import org.weborganic.flint.util.Bucket;
import org.weborganic.flint.util.Terms;
import org.weborganic.flint.util.Bucket.Entry;

import com.topologi.diffx.xml.XMLWriter;
import com.weborganic.bastille.flint.helpers.IndexMaster;

/**
 * Lookup the similar terms for the specified term.
 * 
 * @author Christophe Lauret 
 * @version 28 May 2010
 */
public final class LookupSimilarTerms implements ContentGenerator, Cacheable {

  /**
   * Logger for debugging
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(LookupSimilarTerms.class);

  /**
   * Generate an ETag based on the parameters and the last modified date of the index.
   */
  @Override public String getETag(ContentRequest req) {
    StringBuilder etag = new StringBuilder();
    // Get relevant parameters
    etag.append(req.getParameter("term", "keyword")).append('%');
    etag.append(req.getParameter("field", "")).append('%');
    // Get last time index was modified
    IndexMaster master = IndexMaster.getInstance();
    if (master.isSetup()) {
      etag.append(master.lastModified());
    }
    // MD5 of computed etag value
    return MD5.hash(etag.toString());
  }

  /**
   * {@inheritDoc}
   */
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {
    // Create a new query object
    String field = req.getParameter("field", "keyword");
    Term term = new Term(field, req.getParameter("term"));

    LOGGER.debug("Looking up fuzzy terms for "+term);
    xml.openElement("similar-terms");

    // Start the search
    IndexMaster master = IndexMaster.getInstance();
    if (master.isSetup()) {
      IndexReader reader = null;
      try {
        Bucket<Term> bucket = new Bucket<Term>(20);
        reader = master.grabReader();
        Terms.fuzzy(reader, bucket, term);
        Terms.prefix(reader, bucket, term);
        for (Entry<Term> e : bucket.entrySet()) {
          Terms.toXML(xml, e.item(), e.count());
        }
      } catch (IOException ex) {
        throw new BerliozException("Exception thrown while fetching fuzzy terms", ex);
      } catch (IndexException ex) {
        throw new BerliozException("Exception thrown while fetching fuzzy terms", ex);
      } finally {
        master.releaseSilently(reader);
      }
    }

    xml.closeElement();
  }

}
