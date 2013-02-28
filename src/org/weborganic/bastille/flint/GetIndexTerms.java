/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.weborganic.bastille.flint;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weborganic.bastille.flint.config.FlintConfig;
import org.weborganic.bastille.flint.helpers.Etags;
import org.weborganic.bastille.flint.helpers.IndexMaster;
import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.content.Cacheable;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentRequest;
import org.weborganic.flint.IndexException;
import org.weborganic.flint.local.LocalIndex;

import com.topologi.diffx.xml.XMLWriter;

/**
 * List the terms from the index.
 *
 * @author Christophe Lauret
 * @author Jean-Baptiste Reure
 *
 * @version 0.8.9 - 19 October 2012
 * @since 0.6.0
 */
public final class GetIndexTerms implements ContentGenerator, Cacheable {

  /**
   * Logger for debugging
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(GetIndexTerms.class);

  /**
   * To list only folders
   */
  private static final FileFilter FOLDERS_ONLY = new FileFilter() {
    @Override
    public boolean accept(File d) {
      return d.isDirectory();
    }
  };

  @Override
  public String getETag(ContentRequest req) {
    return Etags.getETag(req.getParameter("index"));
  }

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {
    // Getting the index
    File root = FlintConfig.directory();
    String field = req.getParameter("field");

    if (LocalIndex.exists(root)) {
      // single index, output it
      termsToXML(null, field, xml);

    } else {
      String indexName = req.getParameter("index");
      if (indexName != null) {
        termsToXML(indexName, field, xml);
      } else {
        // multiple indexes maybe
        File[] dirs = root.listFiles(FOLDERS_ONLY);
        if (dirs != null && dirs.length > 0) {
          for (File d : dirs) {
            termsToXML(d.getName(), field, xml);
          }
        } else {
          xml.openElement("terms");
          xml.attribute("error", "No index");
          xml.closeElement();
        }
      }
    }
  }

  /**
   * Output the given terms in the index as XML
   *
   * @param name The name of the index.
   * @param xml  The XML to write on.
   *
   * @throws IOException Should any IO error occur.
   */
  private void termsToXML(String index, String field, XMLWriter xml) throws IOException {
    xml.openElement("terms");
    if (index != null) {
      xml.attribute("index", index);
    }
    if (field != null) {
      xml.attribute("field", field);
    }
    IndexReader reader = null;
    IndexMaster master = FlintConfig.getMaster(index);
    try {
      reader = master.grabReader();
    } catch (IndexException ex) {
      xml.attribute("error", "Failed to load reader: "+ex.getMessage());
    }
    if (reader != null) {
      try {
        TermEnum e;
        Term t;
        // Field a specified let's enumerate the terms for that field
        if (field != null) {
          e = reader.terms(new Term(field));
          do {
            t = e.term();
            // The enum includes other field names, let's stop before
            if (!field.equals(t.field())) break;
            toXML(t, e, xml);
            t = e.term();
          } while (e.next());

        // No field let's iterate over ALL the terms in the index
        } else {
          e = reader.terms();
          do {
            t = e.term();
            toXML(t, e, xml);
            t = e.term();
          } while (e.next());
        }
        e.close();
      } catch (IOException ex) {
        LOGGER.error("Error while extracting term statistics", ex);
      } finally {
        master.releaseSilently(reader);
      }
    } else {
      xml.attribute("error", "Reader is null");
    }
    xml.closeElement();
  }

  /**
   * Write the term as XML.
   *
   * @param t   The term to serialize as XML
   * @param e   The term enum it belongs to (for doc frequency)
   * @param xml The XML
   *
   * @throws IOException
   */
  private static void toXML(Term t, TermEnum e, XMLWriter xml) throws IOException {
    if (t == null) return;
    xml.openElement("term");
    xml.attribute("field", t.field());
    xml.attribute("text", t.text());
    xml.attribute("doc-freq", e.docFreq());
    xml.closeElement();
  }
}
