/*
 * Copyright 2015 Allette Systems (Australia)
 * http://www.allette.com.au
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pageseeder.bastille.flint;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.pageseeder.bastille.flint.config.FlintConfig;
import org.pageseeder.bastille.flint.helpers.IndexMaster;
import org.pageseeder.bastille.flint.helpers.MultiSearchResults;
import org.pageseeder.bastille.flint.helpers.MultipleIndex;
import org.pageseeder.bastille.util.Errors;
import org.pageseeder.berlioz.BerliozException;
import org.pageseeder.berlioz.Beta;
import org.pageseeder.berlioz.content.ContentGenerator;
import org.pageseeder.berlioz.content.ContentRequest;
import org.pageseeder.flint.IndexException;
import org.pageseeder.flint.query.BasicQuery;
import org.pageseeder.flint.query.SearchPaging;
import org.pageseeder.flint.query.SearchResults;
import org.pageseeder.flint.query.TermParameter;
import org.pageseeder.xmlwriter.XMLWriter;

/**
 * Performs a search on the index using a specific term query.
 *
 * @author Christophe Lauret
 *
 * @version 0.8.6 - 8 February 2013
 * @since 0.8.6
 */
@Beta
public final class TermSearch implements ContentGenerator  {

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {

    // Create a new query object using HTTP parameters
    String field = req.getParameter("field", "");
    String term = req.getParameter("term", "");
    if (field.isEmpty()) {
      Errors.noParameter(req, xml, "field");
      return;
    }
    if (term.isEmpty()) {
      // empty string is OK for term
      Errors.noParameter(req, xml, "term");
      return;
    }

    // Create a query from the predicate
    TermParameter parameter = new TermParameter(field, term);
    BasicQuery<TermParameter> query = BasicQuery.newBasicQuery(parameter);

    // Initialise the paging setting
    SearchPaging paging = new SearchPaging();
    int page = req.getIntParameter("page", 1);
    paging.setPage(page);
    paging.setHitsPerPage(100);

    // Get search result
    try {

      // get index to search on
      String index = req.getParameter("index");
      if (index == null) {
        IndexMaster central = FlintConfig.getMaster();
        SearchResults results = central.query(query, paging);

        // print the result
        xml.openElement("index-search", true);
        query.toXML(xml);
        results.toXML(xml);
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

        // print the result
        xml.openElement("index-search", true);
        query.toXML(xml);
        results.toXML(xml);
        xml.closeElement();
      }

    } catch (IndexException ex) {
      throw new BerliozException("Fail to retrieve search result using query: "+query.toString(), ex);
    }
  }

}
