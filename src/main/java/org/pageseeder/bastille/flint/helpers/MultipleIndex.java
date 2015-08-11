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
package org.pageseeder.bastille.flint.helpers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ParallelMultiSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopFieldCollector;
import org.pageseeder.bastille.flint.config.FlintConfig;
import org.pageseeder.berlioz.util.Pair;
import org.pageseeder.flint.IndexException;
import org.pageseeder.flint.query.SearchPaging;
import org.pageseeder.flint.query.SearchQuery;
import org.pageseeder.flint.search.Facet;
import org.pageseeder.flint.search.FieldFacet;

/**
 * Handles multiple Index Masters, each master is specified by its index directory.
 *
 * @author Jean-Baptiste Reure
 *
 * @version 0.6.20 - 26 September 2011
 * @since 0.6.18
 */
public final class MultipleIndex {

  /**
   * @param index The directory for the index to return
   *
   * @deprecated Use {@link #getMaster(File)} instead
   *
   * @return the master for the given index root
   */
  @Deprecated
  public static IndexMaster getMaster(File index) {
    return FlintConfig.getOrCreateMaster(index);
  }

  // non-static methods
  // ----------------------------------------------------------------------------------------------

  /**
   * The list of indexes for this MultipleIndex
   */
  private final List<File> indexDirs = new ArrayList<File>();

  /**
   * The last time this index was modified
   */
  private volatile long lastModified = -1;


  /**
   * Build a new multiple index.
   *
   * @param indexDirectories the root folders for all indexes
   */
  public MultipleIndex(List<File> indexDirectories) {
    if (indexDirectories != null) {
      this.indexDirs.addAll(indexDirectories);
      // get the last modified date
      for (File f: indexDirectories ) {
        if (f.exists() && f.isDirectory() && f.lastModified()>this.lastModified ) {
          this.lastModified = f.lastModified();
        }
      }
    }
  }

  /**
   * Perform a query on multiple indexes.
   *
   * <p>Note that all the indexes MUST be initialized before calling this method.
   *
   * @param query     the query to perform.
   *
   * @return The search results
   *
   * @throws IndexException        If the query failed
   * @throws IllegalStateException If one of the indexes is not initialized
   */
  public MultiSearchResults query(SearchQuery query) throws IndexException {
    return query(query, new SearchPaging());
  }

  /**
   * Perform a query on multiple indexes.
   *
   * <p>Note that all the indexes MUST be initialised before calling this method.
   *
   * @param query     the query to perform.
   * @param paging    the paging mechanism
   *
   * @return The search results
   *
   * @throws IndexException        If the query failed
   * @throws IllegalStateException If one of the indexes is not initialised
   */
  public MultiSearchResults query(SearchQuery query, SearchPaging paging) throws IndexException {
    return query(query, paging, 0);
  }

  /**
   * Perform a query on multiple indexes.
   *
   * <p>Note that all the indexes MUST be initialised before calling this method.
   *
   * @param query                The query to perform.
   * @param paging               The paging mechanism
   * @param maxFieldValueLength  The field value will not be returned if its
   *                             length is longer than or equal maxFieldValueLength.
   * @return The search results
   *
   * @throws IndexException        If the query failed
   * @throws IllegalStateException If one of the indexes is not initialised
   */
  public MultiSearchResults query(SearchQuery query, SearchPaging paging,
      int maxFieldValueLength) throws IndexException {
    // retrieve all searchers
    IndexSearcher[] searchers = new IndexSearcher[this.indexDirs.size()];
    Map<IndexMaster, IndexSearcher> indexes = new HashMap<IndexMaster, IndexSearcher>();
    // grab a reader for each indexes
    for (int i = 0; i < this.indexDirs.size(); i++) {
      IndexMaster master = FlintConfig.getOrCreateMaster(this.indexDirs.get(i));
      // make sure index has been setup
      if (master == null) throw new IllegalStateException("Cannot search on an index before it has been initialised");
      // grab a searcher
      IndexSearcher searcher = master.manager().grabSearcher(master.index());
      searchers[i] = searcher;
      indexes.put(master, searcher);
    }
    try {
      ParallelMultiSearcher multiSearcher = new ParallelMultiSearcher(searchers);
      // retrieve the sorting rules
      Sort sort = query.getSort();
      // default is follow index order
      if (sort == null) {
        sort = Sort.INDEXORDER;
      }
      // create result collector
      TopFieldCollector tfc = TopFieldCollector.create(sort, paging.getHitsPerPage() * paging.getPage(), true, true, false, true);
      // run query
      multiSearcher.search(query.toQuery(), tfc);
      // build search result object
      return new MultiSearchResults(query, tfc.topDocs().scoreDocs, tfc.getTotalHits(), paging, multiSearcher, indexes, maxFieldValueLength);
    } catch (IOException e) {
      throw new IndexException("Failed performing a query on Multiple Index because of an I/O problem", e);
    }
  }

  /**
   * Returns the list of term and how frequently they are used by performing a fuzzy match on the
   * specified term.
   *
   * @param fields the fields to use as facets
   * @param upTo   the max number of values to return
   * @param query  a predicate to apply on the facet (can be null or empty)
   *
   * @throws IndexException if there was an error reading the indexes or creating the condition query
   * @throws IllegalStateException If one of the indexes is not initialised
   */
  public List<Facet> getFacets(List<String> fields, int upTo, Query query) throws IOException, IndexException {
    // parameter checks
    if (fields == null || fields.isEmpty() || this.indexDirs.isEmpty())
      return Collections.emptyList();
    // check for one index only
    if (this.indexDirs.size() == 1) {
      List<Facet> facets = new ArrayList<Facet>();
      IndexMaster master = FlintConfig.getOrCreateMaster(this.indexDirs.get(0));
      // make sure index has been setup
      if (master == null) throw new IllegalStateException("Cannot search on an index before it has been initialised");
      for (String field : fields) {
        if (field.length() > 0) {
          facets.add(master.getFacet(field, upTo, query));
        }
      }
      return facets;
    }
    // retrieve all searchers and readers
    IndexSearcher[] searchers = new IndexSearcher[this.indexDirs.size()];
    IndexReader[] readers = new IndexReader[this.indexDirs.size()];
    Map<IndexMaster, Pair<IndexReader, IndexSearcher>> indexes = new HashMap<IndexMaster, Pair<IndexReader, IndexSearcher>>();
    // grab a reader for each indexes
    for (int i = 0; i < this.indexDirs.size(); i++) {
      IndexMaster master = FlintConfig.getOrCreateMaster(this.indexDirs.get(i));
      // make sure index has been setup
      if (master == null) throw new IllegalStateException("Cannot search on an index before it has been initialised");
      // grab what we need
      IndexSearcher searcher = master.manager().grabSearcher(master.index());
      IndexReader reader = master.manager().grabReader(master.index());
      searchers[i] = searcher;
      readers[i] = reader;
      indexes.put(master, new Pair<IndexReader, IndexSearcher>(reader, searcher));
    }
    List<Facet> facets = new ArrayList<Facet>();
    try {
      // Retrieve all terms for the field
      IndexReader multiReader = new MultiReader(readers);
      ParallelMultiSearcher multiSearcher = new ParallelMultiSearcher(searchers);
      for (String field : fields) {
        if (field.length() > 0) {
          FieldFacet facet = FieldFacet.newFacet(field, multiReader);
          // search
          facet.compute(multiSearcher, query, upTo);
          // store it
          facets.add(facet);
        }
      }
    } finally {
      // now release everything we used
      for (Map.Entry<IndexMaster, Pair<IndexReader, IndexSearcher>> entry : indexes.entrySet())  {
        entry.getKey().releaseSilently(entry.getValue().first());
        entry.getKey().releaseSilently(entry.getValue().second());
      }
    }
    return facets;
  }

  /**
   * @return a new reader reading all indexes.
   */
  public MultipleIndexReader getMultiReader() {
    return new MultipleIndexReader();
  }

  /**
   * Returns the last time the index was updated.
   *
   * @return the last time the index was updated.
   */
  public long lastModified() {
    return this.lastModified;
  }

  /**
   * Handle a list of readers.
   *
   * @author Jean-Baptiste Reure
   * @version 20 September 2011
   *
   */
  public final class MultipleIndexReader {

    /**
     * List of open readers.
     */
    private final Map<IndexMaster, IndexReader> indexes = new HashMap<IndexMaster, IndexReader>();

    /**
     * Grab a new index reader.
     *
     * @throws IndexException
     */
    public IndexReader grab() throws IndexException {
      IndexReader[] readers = new IndexReader[MultipleIndex.this.indexDirs.size()];
      // grab a reader for each indexes
      for (int i = 0; i < MultipleIndex.this.indexDirs.size(); i++) {
        IndexMaster master = FlintConfig.getOrCreateMaster(MultipleIndex.this.indexDirs.get(i));
        // make sure index has been setup
        if (master == null) throw new IllegalStateException("Cannot search on an index before it has been initialised");
        // grab what we need
        IndexReader reader = master.manager().grabReader(master.index());
        readers[i] = reader;
        // store it so we can release it later on
        this.indexes.put(master, reader);
      }
      return new MultiReader(readers);
    }

    /**
     * Release all the open readers we have listed.
     */
    public void releaseSilently() {
      // now release everything we used
      while (!this.indexes.isEmpty())  {
        IndexMaster master = this.indexes.keySet().iterator().next();
        master.releaseSilently(this.indexes.remove(master));
      }
    }
  }

}
