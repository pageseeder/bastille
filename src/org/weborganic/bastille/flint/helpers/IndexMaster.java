/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.weborganic.bastille.flint.helpers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weborganic.bastille.flint.config.FlintConfig;
import org.weborganic.bastille.flint.config.IFlintConfig;
import org.weborganic.flint.IndexConfig;
import org.weborganic.flint.IndexException;
import org.weborganic.flint.IndexJob;
import org.weborganic.flint.IndexJob.Priority;
import org.weborganic.flint.IndexManager;
import org.weborganic.flint.api.ContentId;
import org.weborganic.flint.api.Index;
import org.weborganic.flint.api.Requester;
import org.weborganic.flint.local.LocalFileContentId;
import org.weborganic.flint.local.LocalIndex;
import org.weborganic.flint.query.SearchPaging;
import org.weborganic.flint.query.SearchQuery;
import org.weborganic.flint.query.SearchResults;
import org.weborganic.flint.query.SuggestionQuery;
import org.weborganic.flint.search.Facet;
import org.weborganic.flint.search.FieldFacet;
import org.weborganic.flint.util.Terms;


/**
 * Centralizes all the indexing and searching function using Flint for one index.
 *
 * @author Christophe Lauret
 * @author Jean-Baptiste Reure
 *
 * @version 0.7.4 - 17 October 2012
 * @since 0.6.0
 */
public final class IndexMaster {

  /**
   * A logger for this class and to provide for Flint.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(IndexMaster.class);

  /**
   * The requester is always the index master.
   */
  private static final Requester REQUESTER = new Requester() {
    @Override
    public String getRequesterID() {
      return "Bastille";
    }
  };

  /**
   * The flint configuration used by this master.
   */
  private IFlintConfig ifconfig;

  /**
   * Where the private files are.
   */
  private volatile IndexManager manager = null;

  /**
   * The underlying index used by this generator.
   */
  private volatile LocalIndex index = null;

  /**
   * The last time this index was modified
   */
  private volatile long lastModified = -1;

  /**
   * Sets up the index master using the default Flint configuration.
   *
   * @param directory the index directory
   */
  public IndexMaster(File directory) {
    this(directory, FlintConfig.get());
  }

  /**
   * Sets up the index master.
   *
   * @param indexDir    the index directory
   * @param flintconfig the Flint configuration to use.
   */
  private IndexMaster(File indexDir, final IFlintConfig flintconfig) {
    this.index = new LocalIndex(indexDir, FlintConfig.newAnalyzer());
    this.lastModified = this.index.getLastModified();
    this.manager = FlintConfig.getManager();
    this.ifconfig = flintconfig;
  }

  /**
   * Returns the index instance this class operates on.
   *
   * @return the index instance this class operates on.
   */
  public Index index() {
    return this.index;
  }

  /**
   * Returns the index config this class operates on.
   *
   * @return the index config this class operates on.
   */
  public IndexConfig config() {
    // TODO
    return null; //this.ifconfig.get(this.index.getIndexID());
  }

  /**
   * Returns the underlying Index Manager.
   *
   * @return the underlying Index Manager.
   */
  public IndexManager manager() {
    return this.manager;
  }

  /**
   * Index the specified file with the given parameters.
   *
   * @param file       The file to index.
   * @param parameters The parameters to pass to the stylesheet.
   */
  public void index(File file, Map<String, String> parameters) {
    ContentId cid = new LocalFileContentId(file);
    this.lastModified = System.currentTimeMillis();
    IndexConfig ic = this.ifconfig.get(this.index.getIndexID());
    this.manager.index(cid, this.index, ic, REQUESTER, Priority.HIGH, parameters);
  }

  /**
   * Clears the content of the wrapped index.
   */
  public void clear() {
    this.manager.clear(this.index, REQUESTER, Priority.HIGH);
  }

  /**
   * @return the list of job waiting to be processed.
   */
  public List<IndexJob> getJobsInQueue() {
    return this.manager.getStatus(REQUESTER);
  }

  /**
   * Makes a query to the wrapped index.
   *
   * @param query The query to make
   * @return The search results
   *
   * @throws IndexException Should any error while the query is made.
   */
  public SearchResults query(SearchQuery query) throws IndexException {
    return this.manager.query(this.index, query);
  }

  /**
   * Makes a query to the wrapped index.
   *
   * @param query  The query to make
   * @param paging The paging configuration
   *
   * @return The search results
   *
   * @throws IndexException Should any error while the query is made.
   */
  public SearchResults query(SearchQuery query, SearchPaging paging) throws IndexException {
    return this.manager.query(this.index, query, paging);
  }

  /**
   * Returns the last time an index job was requested or if none was requested the last time
   * the index was updated.
   *
   * @return the last time an index job was requested.
   */
  public long lastModified() {
    return this.lastModified;
  }

  /**
   * Reload the IXML templates.
   */
  public void reloadTemplates() {
    //XXX: FlintConfig.get().configure(this.config);
  }

  /**
   * Returns the list of term and how frequently they are used by performing a fuzzy match on the
   * specified term.
   *
   * @param field  the field to use as a facet
   * @param upTo   the max number of values to return
   * @param query  a predicate to apply on the facet (can be null or empty)
   *
   * @return the facte instance.
   *
   * @throws IOException    if there was an error reading the index or creating the condition query
   * @throws IndexException if there was an error getting the reader or searcher.
   */
  public Facet getFacet(String field, int upTo, Query query) throws IndexException, IOException {
    FieldFacet facet = null;
    IndexReader reader = null;
    IndexSearcher searcher = null;
    try {
      // Retrieve all terms for the field
      reader = this.manager.grabReader(this.index);
      facet = FieldFacet.newFacet(field, reader);

      // search
      searcher = this.manager.grabSearcher(this.index);
      facet.compute(searcher, query, upTo);

    } finally {
      this.manager.releaseQuietly(this.index, reader);
      this.manager.releaseQuietly(this.index, searcher);
    }
    return facet;
  }

  /**
   * Returns the list of files that have been indexed form the index content.
   *
   * @deprecated This method ignores the term, use {@link #list()} instead.
   *
   * @return the list of files that have been indexed form the index content.
   *
   * @throws IOException    if there was an error getting the reader.
   */
  @Deprecated
  public List<File> list(Term t) throws IOException {
    List<File> files = new ArrayList<File>();
    IndexReader reader = null;
    try {
      reader = this.manager.grabReader(this.index);
      for (int i = 0; i < reader.numDocs(); i++) {
        File f = this.ifconfig.toFile(reader.document(i));
        files.add(f);
      }
    } catch (IndexException ex) {
      throw new IOException(ex);
    } finally {
      this.manager.releaseQuietly(this.index, reader);
    }
    return files;
  }

  /**
   * Returns the list of files that have been indexed form the index content.
   *
   * @return the list of files that have been indexed form the index content.
   */
  public List<File> list() throws IOException {
    List<File> files = new ArrayList<File>();
    IndexReader reader = null;
    try {
      reader = this.manager.grabReader(this.index);
      for (int i = 0; i < reader.numDocs(); i++) {
        File f = this.ifconfig.toFile(reader.document(i));
        files.add(f);
      }
    } catch (IndexException ex) {
      throw new IOException(ex);
    } finally {
      this.manager.releaseQuietly(this.index, reader);
    }
    return files;
  }

  /**
   * Suggests results for the given fields and text.
   *
   * @param fields    The list of fields to use.
   * @param texts     The list of term texts to use.
   * @param max       The maximum number of suggested results.
   * @param predicate By default, assumes that it is the document type.
   *
   * @return the suggestions in the form of search results.
   */
  public SearchResults getSuggestions(List<String> fields, List<String> texts, int max, String predicate) throws IOException, IndexException {
    // Generate the list of terms
    List<Term> terms = Terms.terms(fields, texts);

    // Parse the condition query
    Query condition = toQuery(predicate);

    // Creates the suggestion query
    SuggestionQuery query = new SuggestionQuery(terms, condition);
    IndexReader reader = null;
    try {
      reader = this.manager.grabReader(this.index);
      query.compute(reader);
    } finally {
      this.releaseSilently(reader);
    }

    // Pages
    SearchPaging pages = new SearchPaging();
    if (max > 0) pages.setHitsPerPage(max);
    return this.manager.query(this.index, query, pages);
  }

  /**
   * Returns the query for the specified predicate using the Query Parser.
   *
   * @param predicate The predicate to parse
   * @return the corresponding query object or <code>null</code>.
   *
   * @throws IndexException should any error occur
   */
  public static Query toQuery(String predicate) throws IndexException {
    // Parse the condition query
    QueryParser parser = new QueryParser(IndexManager.LUCENE_VERSION, "type", FlintConfig.newAnalyzer());
    Query condition = null;
    if (predicate != null && !"".equals(predicate)) {
      try {
        condition = parser.parse(predicate);
      } catch (ParseException ex) {
        throw new IndexException("Condition for the suggestion could not be parsed.", ex);
      }
    }
    return condition;
  }

  /**
   * Return an index reader on the index.
   *
   * @return The index will need to be released.
   *
   * @throws IndexException If thrown by index manager.
   */
  public IndexReader grabReader() throws IndexException {
    return this.manager.grabReader(this.index);
  }

  /**
   * Return an index searcher on the index.
   *
   * @return The index searcher that will need to be released.
   *
   * @throws IndexException If thrown by index manager.
   */
  public IndexSearcher grabSearcher() throws IndexException {
    return this.manager.grabSearcher(this.index);
  }

  /**
   * Releases this reader for use by other threads silently (any exception will be ignored).
   *
   * <p>Provided for convenience when used inside a <code>finally</code> block.
   *
   * @param reader The Lucene index reader to release.
   */
  public void releaseSilently(IndexReader reader) {
    this.manager.releaseQuietly(this.index, reader);
  }

  /**
   * Releases this searcher for use by other threads silently (any exception will be ignored).
   *
   * <p>Provided for convenience when used inside a <code>finally</code> block.
   *
   * @param searcher The Lucene index searcher to release.
   */
  public void releaseSilently(IndexSearcher searcher) {
    this.manager.releaseQuietly(this.index, searcher);
  }

}
