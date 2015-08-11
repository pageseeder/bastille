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
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;
import org.pageseeder.bastille.flint.config.FlintConfig;
import org.pageseeder.bastille.flint.helpers.IndexMaster;
import org.pageseeder.bastille.flint.helpers.MultipleIndex;
import org.pageseeder.berlioz.BerliozException;
import org.pageseeder.berlioz.content.Cacheable;
import org.pageseeder.berlioz.content.ContentGenerator;
import org.pageseeder.berlioz.content.ContentRequest;
import org.pageseeder.berlioz.content.ContentStatus;
import org.pageseeder.berlioz.util.MD5;
import org.pageseeder.flint.IndexException;
import org.pageseeder.flint.search.Facet;
import org.pageseeder.xmlwriter.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Returns the facets from a query.
 *
 * <p>This is a simple and efficient generator that is most useful for use with autocomplete.
 *
 * @author Christophe Lauret
 * @version 0.6.0 - 26 July 2010
 * @since 0.6.0
 */
public final class GetFacets implements ContentGenerator, Cacheable {

  /**
   * Logger for debugging
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(GetFacets.class);

  /**
   * Generate an ETag based on the parameters and the last modified date of the index.
   *
   * {@inheritDoc}
   */
  @Override
  public String getETag(ContentRequest req) {
    StringBuilder etag = new StringBuilder();
    // Get relevant parameters
    etag.append(req.getParameter("field", "keyword")).append('%');
    etag.append(req.getParameter("predicate", "")).append('%');
    String index = req.getParameter("index");
    if (index != null) {
      for (String ind : index.split(",")) {
        etag.append(FlintConfig.getMaster(ind).lastModified()).append('%');
      }
    } else {
      // Get last time index was modified
      IndexMaster master = FlintConfig.getMaster();
      if (master != null) {
        etag.append(master.lastModified()).append('%');
      }
    }
    // MD5 of computed etag value
    return MD5.hash(etag.toString());
  }

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {
    // Create a new query object
    String base = req.getParameter("base", "");
    String facets = req.getParameter("facets", "");

    // make sure condition is valid
    Query query;
    try {
      query = IndexMaster.toQuery(base);
    } catch (IndexException ex) {
      LOGGER.error("Unable to parse query", ex);
      xml.openElement("error");
      xml.attribute("type", "invalid-parameter");
      xml.attribute("message", "Unable to create query from condition "+base);
      xml.closeElement();
      req.setStatus(ContentStatus.BAD_REQUEST);
      return;
    }

    LOGGER.debug("Computing facets {} for {}", facets, base);
    xml.openElement("facets");
    xml.attribute("for", base);

    // check if there is one or more index specified
    String index = req.getParameter("index");
    try {
      if (index != null) {
        String[] indexeNames = index.split(",");
        List<File> indexDirectories = new ArrayList<File>();
        for (String ind : indexeNames) {
          indexDirectories.add(req.getEnvironment().getPrivateFile("index/"+ind));
        }
        MultipleIndex indexes = new MultipleIndex(indexDirectories);
        // facets
        try {
          List<String> fields = Arrays.asList(facets.split(","));
          List<Facet> fieldFacets = indexes.getFacets(fields, 10, query);
          for (Facet facet : fieldFacets) {
            facet.toXML(xml);
          }
        } catch (IndexException ex) {
          LOGGER.error("Unable to load facets", ex);
        }
      } else {
        // Make sure there is an index
        IndexMaster master = FlintConfig.getMaster();
        if (master != null) {
          IndexReader reader = null;
          try {

            // facets
            for (String f : facets.split(",")) {
              if (f.length() > 0) {
                Facet facet = master.getFacet(f, 10, query);
                facet.toXML(xml);
              }
            }

          } catch (IndexException ex) {
            LOGGER.error("Unable to load facets", ex);
          } finally {
            master.releaseSilently(reader);
          }
        }
      }
    } finally {
      xml.closeElement();
    }
  }

}
