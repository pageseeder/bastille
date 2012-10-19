/*
 * This file is part of the Bastille library.
 *
 * Available under a commercial licence, contact Weborganic.
 *
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package com.weborganic.bastille.flint;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentRequest;
import org.weborganic.flint.IndexException;
import org.weborganic.flint.query.BasicQuery;
import org.weborganic.flint.query.SearchPaging;
import org.weborganic.flint.query.SearchParameter;
import org.weborganic.flint.query.SearchResults;
import org.weborganic.flint.query.TermParameter;
import org.weborganic.flint.search.Facet;

import com.topologi.diffx.xml.XMLWriter;
import com.weborganic.bastille.flint.config.FlintConfig;
import com.weborganic.bastille.flint.helpers.IndexMaster;
import com.weborganic.bastille.flint.helpers.MultiSearchResults;
import com.weborganic.bastille.flint.helpers.MultipleIndex;

/**
 * <p>Perform a search on the index, using the following details:</p>
 * <ul>
 *   <li>The main field used is the value of the parameter <code>field</code>.<br/>
 *       If the parameter is not specified, <code>fulltext</code> is used.</li>
 *   <li>The value of the main field is the value of the parameter <code>term</code>.</li>
 *   <li>If there is a parameter <code>category</code>, its value is used to filter the
 *       results using the field called <code>category</code>. (the predicate <code>+category:[category]</code> is added).</li>
 *   <li>Facets can be specified using the parameter <code>facets</code> (comma-separated list of field names).</li>
 *   <li>If an <code>index</code> parameter is specified, the (comma-separated) list of indexes will be searched;
 *       otherwise the default index will be used.</li>
 * </ul>
 *
 * @author Christophe Lauret
 * @author Jean-Baptiste Reure
 * @version 0.7.4 - 18 October 2012
 * @since 0.6.0
 */
public class FulltextSearch implements ContentGenerator {

  /**
   * Logger for debugging
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(FulltextSearch.class);

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {

    // Create a new query object using HTTP parameters
    String field = req.getParameter("field", "fulltext");
    String facets = req.getParameter("facets", "");
    TermParameter term = new TermParameter(field, req.getParameter("term"));
    LOGGER.debug("Search for "+term);

    // check for category
    List<SearchParameter> params = new ArrayList<SearchParameter>();
    String category = req.getParameter("category");
    if (category != null)
      params.add(new TermParameter("category", category));

    BasicQuery<TermParameter> query = BasicQuery.newBasicQuery(term, params);
    query.setSort(new Sort(new SortField(null, SortField.SCORE)));

    // Initialise the paging setting
    SearchPaging paging = new SearchPaging();
    int page = req.getIntParameter("page", 1);
    paging.setPage(page);
    paging.setHitsPerPage(100);

    String header = "<content-type>search-result</content-type>";
    // get search result
    try {
      // get index to search on
      String index = req.getParameter("index");
      if (index == null) {
        IndexMaster central = FlintConfig.getMaster();
        SearchResults results = central.query(query, paging);
        // facets
        for (String f : facets.split(",")) {
          if (f.length() > 0) {
            Facet facet = central.getFacet(f, 10, query.toQuery());
            facet.toXML(xml);
          }
        }

        // print the result
        xml.openElement("index-search", true);
        query.toXML(xml);
        results.toXML(xml);
        xml.writeXML(header);
        xml.closeElement();
      } else {
        // find all indexes specified
        String[] indexeNames = index.split(",");
        List<File> indexDirectories = new ArrayList<File>();
        for (String ind : indexeNames) {
          indexDirectories.add(req.getEnvironment().getPrivateFile("index/"+ind));
        }
        MultipleIndex indexes = new MultipleIndex(indexDirectories);
        // run query
        MultiSearchResults results = indexes.query(query, paging);
        // facets
        List<String> fields = Arrays.asList(facets.split(","));
        List<Facet> fieldFacets = indexes.getFacets(fields, 10, query.toQuery());
        for (Facet facet : fieldFacets) {
          facet.toXML(xml);
        }

        // print the result
        xml.openElement("index-search", true);
        query.toXML(xml);
        results.toXML(xml);
        xml.writeXML(header);
        xml.closeElement();
      }

    } catch (IndexException ex) {
      LOGGER.warn("Fail to retrieve search result using query: {}", query.toString(), ex);
    }
  }

}
