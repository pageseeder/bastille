/*
 * This file is part of the Flint library.
 *
 * For licensing information please see the file license.txt included in the release. A copy of this licence can also be
 * found at http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package com.weborganic.bastille.flint.helpers;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.zip.DataFormatException;

import org.apache.lucene.document.CompressionTools;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopFieldDocs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weborganic.flint.IndexException;
import org.weborganic.flint.query.SearchPaging;
import org.weborganic.flint.query.SearchQuery;
import org.weborganic.flint.query.TermExtractable;
import org.weborganic.flint.util.Documents;

import com.topologi.diffx.xml.XMLWritable;
import com.topologi.diffx.xml.XMLWriter;

/**
 * A container for search results of a query ran on multiple indexes.
 *
 * <p>Use this class to serialise Lucene Search results as XML.
 *
 * <p>Note: the current implementation is a "throw away" object, once the toXML method has been
 * called, this instance is useless.
 *
 * @author Christophe Lauret (Weborganic)
 * @author Jean-Baptiste Reure (Weborganic)
 * @author William Liem (Allette Systems)
 *
 * @version 16 September 2011
 */
public final class MultiSearchResults implements XMLWritable {

  /**
   * Logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(MultiSearchResults.class);

  /**
   * The ISO 8601 Date and time format
   *
   * @see <a href="http://www.iso.org/iso/date_and_time_format">ISO: Numeric representation of Dates and Time</a>
   */
  private static final String ISO8601_DATETIME = "yyyy-MM-dd'T'HH:mm:ssZ";

  /**
   * The maximum length for a field to expand.
   */
  private static final int MAX_FIELD_VALUE_LENGTH = 1000;

  /**
   * The actual search results from Lucene.
   */
  private final ScoreDoc[] scoredocs;

  /**
   * Fields used for sorting.
   */
  private final SortField[] sortfields;

  /**
   * Indicates the paging information.
   */
  private final SearchPaging paging;

  /**
   * The query used to produce these results.
   */
  private final SearchQuery query;

  /**
   * The indexes and searchers.
   */
  private final Map<IndexMaster, IndexSearcher> searchers = new HashMap<IndexMaster, IndexSearcher>();

  /**
   * The multi searcher used.
   */
  private final MultiSearcher searcher;

  /**
   * Whether the search results instance has finished.
   */
  private boolean terminated = false;

  /**
   * The total number of results
   */
  private final int totalNbOfResults;

  /**
   * The timezone offset used to adjust the correct date and time.
   */
  private int timezoneOffset;

  /**
   * Creates a new SearchResults.
   *
   * @param fielddocs The actual search results from Lucene in TopFieldDocs.
   * @param paging The paging configuration.
   * @param io The IndexIO object, used to release the searcher when terminated
   * @param searcher The Lucene searcher.
   * @throws IndexException if the documents could not be retrieved from the Index
   */
  public MultiSearchResults(SearchQuery query, TopFieldDocs fielddocs, SearchPaging paging, MultiSearcher searcher, Map<IndexMaster, IndexSearcher> indexes)
      throws IOException, IndexException {
    this(query, fielddocs.scoreDocs, fielddocs.fields, fielddocs.totalHits, paging, searcher, indexes);
  }

  /**
   * Creates a new SearchResults.
   *
   * @param hits The actual search results from Lucene in ScoreDoc.
   * @param paging The paging configuration.
   * @param io The IndexIO object, used to release the searcher when terminated
   * @param searcher The Lucene searcher.
   * @throws IndexException if the documents could not be retrieved from the Index
   */
  public MultiSearchResults(SearchQuery query, ScoreDoc[] hits, int totalHits, SearchPaging paging, MultiSearcher searcher, Map<IndexMaster, IndexSearcher> indexes)
      throws IndexException {
    this(query, hits, null, totalHits, paging, searcher, indexes);
  }

  /**
   * @return the paging used
   */
  public SearchPaging getPaging() {
    return this.paging;
  }

  /**
   * Creates a new SearchResults.
   *
   * @param hits The actual search results from Lucene in ScoreDoc.
   * @param sortf The Field used to sort the results
   * @param paging The paging configuration.
   * @param searcher The Lucene searcher.
   * @throws IndexException if the documents could not be retrieved from the Index
   */
  private MultiSearchResults(SearchQuery query, ScoreDoc[] hits, SortField[] sortf, int totalResults, SearchPaging paging,
      MultiSearcher searcher, Map<IndexMaster, IndexSearcher> indexes) throws IndexException {
    this.query = query;
    this.scoredocs = hits;
    this.sortfields = sortf;
    if (paging == null)
      this.paging = new SearchPaging();
    else
      this.paging = paging;
    this.searcher = searcher;
    this.searchers.putAll(indexes);
    this.totalNbOfResults = totalResults;
    // default timezone is the server's
    TimeZone tz = TimeZone.getDefault();
    this.timezoneOffset = tz.getRawOffset();
    // take daylight savings into account
    if (tz.inDaylightTime(new Date())) this.timezoneOffset += 3600000;
  }

  /**
   * Returns the total number of results.
   *
   * @return the total number of results.
   */
  public int getTotalNbOfResults() {
    return this.totalNbOfResults;
  }

  /**
   * Indicates whether the search results are empty.
   *
   * @return <code>true</code> if the results are empty;
   *         <code>false</code> if there is more than one hit.
   */
  public boolean isEmpty() {
    return this.totalNbOfResults == 0;
  }

  /**
   * Sets the time zone to use when formatting the results as XML.
   *
   * @param timezoneInMinutes the timezone offset in minutes (difference with GMT)
   */
  public void setTimeZone(int timezoneInMinutes) {
    this.timezoneOffset = timezoneInMinutes * 60000;
  }

  /**
   * Serialises the search results as XML.
   *
   * @param xml The XML writer.
   *
   * @throws IOException Should there be any I/O exception while writing the XML.
   */
  @Override
  public void toXML(XMLWriter xml) throws IOException {
    xml.openElement("search-results", true);
    // Check whether it's equally distribute mode, if yes then calculate num of hits for each page
    int length = this.totalNbOfResults;
    int hitsperpage = this.paging.getHitsPerPage();
    int firsthit = hitsperpage * (this.paging.getPage() - 1) + 1;
    int lasthit = Math.min(length, firsthit + hitsperpage - 1);

    // include query
    if (this.query != null) {
      xml.openElement("query", true);
      xml.attribute("lucene", this.query.toQuery().toString());
      this.query.toXML(xml);
      xml.closeElement();
    }

    // Display some metadata on the search
    xml.openElement("metadata", true);
    xml.openElement("hits", true);
    xml.element("per-page", Integer.toString(hitsperpage));
    xml.element("total", Integer.toString(length));
    xml.closeElement();
    xml.openElement("page", true);
    xml.element("first-hit", Integer.toString(firsthit));
    xml.element("last-hit", Integer.toString(lasthit));
    xml.element("current", Integer.toString(this.paging.getPage()));
    xml.element("last", Integer.toString(((length - 1) / hitsperpage) + 1));
    xml.closeElement();
    if (this.sortfields != null) {
      xml.openElement("sort-fields", true);
      for (SortField field : this.sortfields) {
        xml.element("field", field.getField());
      }
      xml.closeElement();
    }
    xml.closeElement();

    // Returned documents
    xml.openElement("documents", true);

    // iterate over the hits
    final DateFormat iso = new SimpleDateFormat(ISO8601_DATETIME);
    for (int i = firsthit - 1; i < lasthit; i++) {
      xml.openElement("document", true);
      String score = Float.toString(this.scoredocs[i].score);
      xml.element("score", score);
      Document doc = this.searcher.doc(this.scoredocs[i].doc);

      // Find the extract only applies to TermExtractable queries
      if (this.query instanceof TermExtractable) {
        TermExtractable q = (TermExtractable)this.query;
        Set<Term> terms = new HashSet<Term>();
        q.extractTerms(terms);
        for (Fieldable f : doc.getFields()) {
          for (Term t : terms) {
            if (t.field().equals(f.name())) {
              String extract = Documents.extract(value(f), t.text(), 200);
              if (extract != null) {
                xml.openElement("extract");
                xml.attribute("from", t.field());
                xml.writeXML(extract);
                xml.closeElement();
              }
            }
          }
        }
      }

      // display the value of each field
      for (Fieldable f : doc.getFields()) {
        // Retrieve the value
        String value = value(f);
        // format dates using ISO 8601
        if (f.name().contains("date")) {
          value = formatISO8601(value, iso, this.timezoneOffset);
        }
        // unnecessary to return the full value of long fields
        if (value != null && value.length() < MAX_FIELD_VALUE_LENGTH) {
          xml.openElement("field");
          xml.attribute("name", f.name());
          xml.writeText(value);
          xml.closeElement();
        }
      }
      // close 'document'
      xml.closeElement();
    }
    // close 'documents'
    xml.closeElement();

    // close 'results'
    xml.closeElement();

    // close everything
    try {
      terminate();
    } catch (IndexException e) {
      throw new IOException("Error when terminating Search Results", e);
    }
  }

  /**
   * Return the results.
   *
   * @return the search results
   * @throws IndexException
   */
  public ScoreDoc[] getScoreDoc() throws IndexException {
    if (this.terminated)
      throw new IndexException("Cannot retrieve documents after termination", new IllegalStateException());
    return this.scoredocs;
  }

  /**
   * Load a document from the index.
   *
   * @param id the id of the document
   * @return the document object loaded from the index, could be null
   * @throws IndexException if the index is invalid
   */
  public Document getDocument(int id) throws IndexException {
    if (this.terminated)
      throw new IndexException("Cannot retrieve documents after termination", new IllegalStateException());
    try {
      return this.searcher.doc(id);
    } catch (CorruptIndexException e) {
      LOGGER.error("Failed to retrieve a document because of a corrupted Index", e);
      throw new IndexException("Failed to retrieve a document because of a corrupted Index", e);
    } catch (IOException ioe) {
      LOGGER.error("Failed to retrieve a document because of an I/O problem", ioe);
      throw new IndexException("Failed to retrieve a document because of an I/O problem", ioe);
    }
  }

  /**
   * Release all references to all the searchers used.
   *
   * @throws IndexException
   */
  public void terminate() throws IndexException {
    if (this.searchers.isEmpty()) return;
    for (Map.Entry<IndexMaster, IndexSearcher> entry : this.searchers.entrySet()) {
      entry.getKey().releaseSilently(entry.getValue());
    }
    this.terminated = true;
  }

  @Override
  protected void finalize() throws Throwable {
    if (!this.terminated) terminate();
    super.finalize();
  }

  /**
   *
   * @param value  the value from the index
   * @param iso    the ISO 8601 date formatter
   * @param offset the timezone offset
   * @return
   */
  private static String formatISO8601(String value, DateFormat iso, int offset) {
    if (value == null) return null;
    if ("0".equals(value)) return "";
    if (!"".equals(value)) try {
      Date date = DateTools.stringToDate(value);
      TimeZone tz = TimeZone.getDefault();
      int rawOffset = tz.inDaylightTime(date)? offset - 3600000 : offset;
      tz.setRawOffset(rawOffset);
      iso.setTimeZone(tz);
      String formatted = iso.format(date);
      // the Java timezone does not include the required ':'
      return formatted.substring(0, formatted.length() - 2) + ":" + formatted.substring(formatted.length() - 2);
    } catch (Exception ex) {
      // oh well, the field is probably not a date then, we'll keep going with the index value
      LOGGER.error("Failed to format date field", ex);
    }
    // return the value 'as is'
    return value;
  }

  private static String value(Fieldable f) {
    String value = f.stringValue();
    // is it a compressed field?
    if (value == null && f.getBinaryLength() > 0) try {
      value = CompressionTools.decompressString(f.getBinaryValue());
    } catch (DataFormatException ex) {
      // strange but true, unable to decompress
      LOGGER.error("Failed to decompress field value", ex);
      return null;
    }
    return value;
  }

}
