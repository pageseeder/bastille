/*
 * Copyright (c) 2011 weborganic systems pty. ltd.
 */
package com.weborganic.bastille.flint;

import java.io.IOException;

import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.content.Cacheable;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentRequest;
import org.weborganic.berlioz.content.Environment;
import org.weborganic.berlioz.util.MD5;
import org.weborganic.flint.IndexException;
import org.weborganic.flint.query.GenericSearchQuery;
import org.weborganic.flint.query.SearchPaging;
import org.weborganic.flint.query.SearchQuery;
import org.weborganic.flint.query.SearchResults;
import org.weborganic.flint.query.TermParameter;

import com.topologi.diffx.xml.XMLWriter;
import com.weborganic.bastille.flint.helpers.IndexMaster;
import com.weborganic.bastille.flint.helpers.SingleIndex;

/**
 * TODO Javadoc.
 * 
 * @author Christophe Lauret
 * @version 0.6.0 - 2 June 2010
 * @since 0.6.0
 * 
 * @deprecated
 */
public class ComprehensiveSearch implements ContentGenerator, Cacheable {

  /**
   * Logger for debugging
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(ComprehensiveSearch.class);

  /**
   * Generate an ETag based on the parameters and the last modified date of the index.
   */
  @Override public String getETag(ContentRequest req) {
    StringBuilder etag = new StringBuilder();
    // Get relevant parameters
    etag.append(req.getParameter("field", "keyword")).append('%');
    etag.append(req.getParameter("predicate", "")).append('%');
    // Get last time index was modified
    IndexMaster master = SingleIndex.master();
    if (master != null) {
      etag.append(master.lastModified());
    }
    // MD5 of computed etag value
    return MD5.hash(etag.toString());
  }

  /**
   * {@inheritDoc}
   */
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {

    final Environment env = req.getEnvironment();
    IndexMaster central = SingleIndex.master();
    if (central == null) {
      central = SingleIndex.setupMaster(env.getPrivateFile("ixml/default.xsl"));
    }

    // Create a new query object using HTTP parameters
    GenericSearchQuery query = new GenericSearchQuery();
    String field = req.getParameter("field", "fulltext");
    TermParameter term = new TermParameter(field, req.getParameter("term"));
    LOGGER.debug("Search for "+term);
    query.add(term);

    // check for category
    String category = req.getParameter("category");
    if (category != null)
      query.add(new TermParameter("category", category));

    query.setSort(new Sort(new SortField(null, SortField.SCORE)));

    // Initialise the paging setting
    SearchPaging paging = new SearchPaging();
    int page = req.getIntParameter("page", 1);
    paging.setPage(page);
    paging.setHitsPerPage(100);

    // get search result
    try {
      SearchResults results = central.query(query, paging);

      // print the result
      printResult(query, results, xml);

    } catch (IndexException e) {
      LOGGER.info("Fail to retrieve search result using query: {}", query.toString());
    }
  }

  /**
   * Prints the results as XML.
   * 
   * @param query The query made.
   * @param results The search results.
   * @param xml The XML writer.
   */
  private void printResult(SearchQuery query, SearchResults results, XMLWriter xml) {
    // firstly print the query
    try {
      // start serialising the search
      String header = "<content-type>search-result</content-type>";
      xml.openElement("index-search", true);
      query.toXML(xml);
      results.toXML(xml);
      xml.writeXML(header);
      xml.closeElement();

    } catch (IOException e) {
      LOGGER.info("An IOException occur when try to print the search result.");
    }
  }

}
