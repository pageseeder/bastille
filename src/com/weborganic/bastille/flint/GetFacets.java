package com.weborganic.bastille.flint;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.content.Cacheable;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentRequest;
import org.weborganic.berlioz.util.MD5;
import org.weborganic.flint.IndexException;
import org.weborganic.flint.search.Facet;

import com.topologi.diffx.xml.XMLWriter;
import com.weborganic.bastille.flint.helpers.IndexMaster;

/**
 * Returns the facets from a query.
 * 
 * <p>This is a simple and efficient generator that is most useful for use with autocomplete. 
 * 
 * @author Christophe Lauret 
 * @version 26 July 2010
 */
public final class GetFacets implements ContentGenerator, Cacheable {

  /**
   * Logger for debugging
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(GetFacets.class);

  /**
   * Generate an ETag based on the parameters and the last modified date of the index.
   */
  @Override public String getETag(ContentRequest req) {
    StringBuilder etag = new StringBuilder();
    // Get relevant parameters
    etag.append(req.getParameter("field", "keyword")).append('%');
    etag.append(req.getParameter("predicate", "")).append('%');
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
    String base = req.getParameter("base", "");
    String facets = req.getParameter("facets", "");

    LOGGER.debug("Computing facets {} for {}", facets, base);
    xml.openElement("facets");
    xml.attribute("for", base);

    // Start the search
    IndexMaster master = IndexMaster.getInstance();
    if (master.isSetup()) {
      IndexReader reader = null;
      try {
        Query q = master.toQuery(base);

        // facets
        for (String f : facets.split(",")) {
          if (f.length() > 0) {
            Facet facet = master.getFacet(f, 10, q);
            facet.toXML(xml);
          }
        }

      } catch (IndexException ex) {
        LOGGER.error("Unable to parse query", ex);
      } finally {
        master.releaseSilently(reader);
      }
    }

    xml.closeElement();
  }

}
