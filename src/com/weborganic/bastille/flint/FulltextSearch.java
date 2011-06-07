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
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentGeneratorBase;
import org.weborganic.berlioz.content.ContentRequest;
import org.weborganic.berlioz.content.Environment;
import org.weborganic.flint.IndexException;
import org.weborganic.flint.query.GenericSearchQuery;
import org.weborganic.flint.query.SearchPaging;
import org.weborganic.flint.query.SearchQuery;
import org.weborganic.flint.query.SearchResults;
import org.weborganic.flint.query.TermParameter;
import org.weborganic.flint.search.Facet;

import com.topologi.diffx.xml.XMLWriter;
import com.weborganic.bastille.flint.helpers.IndexMaster;

/**
 * TODO Javadoc.
 * 
 * @author Christophe Lauret
 * @version 0.6.0 - 2 June 2010
 * @since 0.6.0
 */
public class FulltextSearch extends ContentGeneratorBase implements ContentGenerator {

  /**
   * Logger for debugging
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(FulltextSearch.class);

  /**
   * {@inheritDoc}
   */
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {

    final Environment env = req.getEnvironment();
    IndexMaster central = IndexMaster.getInstance();
    if (!central.isSetup()) {
      central.setup(env.getPrivateFile("index"), env.getPrivateFile("ixml/default.xsl"));
    }

    // Create a new query object using HTTP parameters
    GenericSearchQuery query = new GenericSearchQuery();
    String field = req.getParameter("field", "fulltext");
    String facets = req.getParameter("facets", "");
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

      // facets
      for (String f : facets.split(",")) {
        if (f.length() > 0) {
          Facet facet = central.getFacet(f, 10, query.toQuery());
          facet.toXML(xml);
        }
      }

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
