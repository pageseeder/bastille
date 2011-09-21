/*
 * Copyright (c) 2011 weborganic systems pty. ltd.
 */
package com.weborganic.bastille.flint;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.document.Document;
import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.content.Cacheable;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentRequest;
import org.weborganic.berlioz.util.MD5;
import org.weborganic.flint.IndexException;
import org.weborganic.flint.query.SearchResults;
import org.weborganic.flint.util.Documents;

import com.topologi.diffx.xml.XMLWriter;
import com.weborganic.bastille.flint.helpers.IndexMaster;
import com.weborganic.bastille.flint.helpers.SingleIndex;

/**
 * Returns the search results suggestions from a list of terms.
 * 
 * <p>Parameters for this generator are:
 * <ul>
 *   <li><code>term</code>: a space separated list of terms to lookup</li>
 *   <li><code>field</code>: the comma-separated list of the fields to lookup</li>
 *   <li><code>predicate</code>: a query to use as a condition (eg. type of Lucene document, etc...)</li>
 * </ul>
 * 
 * @author Christophe Lauret
 * @version 0.6.0 - 26 July 2010
 * @since 0.6.0
 */
public class GetResultSuggestions implements ContentGenerator, Cacheable {

  /**
   * Generate an ETag based on the parameters and the last modified date of the index.
   */
  @Override public String getETag(ContentRequest req) {
    StringBuilder etag= new StringBuilder();
    // Get relevant parameters
    etag.append(req.getParameter("term", "")).append('%');
    etag.append(req.getParameter("field", "")).append('%');
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
  @Override public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {

    // Collect parameters
    String input = req.getParameter("term", "");
    String field = req.getParameter("field", "");
    String predicate = req.getParameter("predicate", "");
    List<String> fields = asList(field, ",");
    List<String> texts  = asList(input, "\\s+");

    // Start writing output
    xml.openElement("auto-suggest");

    // Check the request
    if (texts.isEmpty()) {
      xml.attribute("no-term", "true");

    } else {
      xml.attribute("term", input);
      xml.attribute("field", field);
      xml.attribute("predicate", predicate);

      // Start the search
      IndexMaster master = SingleIndex.master();
      if (master != null) {
        try {
          // Get the suggestions
          SearchResults results = master.getSuggestions(fields, texts, 10, predicate);
          results.toXML(xml);

        } catch (IndexException ex) {
          throw new BerliozException("Exception thrown while fetching suggestions", ex);
        }
      }
    }

    xml.closeElement();
  }

  /**
   * Tokenizes the terms and returns a list of terms.
   * 
   * @param terms the untokenised string.
   * 
   * @return the list of terms
   */
  private List<String> asList(String terms, String regex) {
    String t = terms.trim();
    return Arrays.asList(t.split(regex));
  }

  /**
   * Returns the XML for a document.
   * 
   * @deprecated New part of Flint in {@link Documents#toXML(XMLWriter, Document)}/
   * 
   * @param xml The XML writer.
   * @param doc Lucene document to serialise as XML.
   * 
   * @throws IOException Any I/O error thrown by the XML writer.
   */
  @Deprecated
  public static void toXML(XMLWriter xml, Document doc) throws IOException {
    Documents.toXML(xml, doc);
  }

}
