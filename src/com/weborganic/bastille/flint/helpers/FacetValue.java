/*
 * Copyright (c) 2011 weborganic systems pty. ltd.
 */
package com.weborganic.bastille.flint.helpers;

import java.io.IOException;

import org.apache.lucene.search.Query;
import org.weborganic.berlioz.Beta;
import org.weborganic.flint.query.SearchParameter;

import com.topologi.diffx.xml.XMLWriter;

/**
 * The selected value for a facet.
 * 
 * @author Christophe Lauret
 * @version 0.6.0 - 2 June 2010
 * @since 0.6.0
 */
@Beta public class FacetValue implements SearchParameter {

  /**
   * TODO Javadoc.
   * @return <code>false</code>
   */
  public boolean isEmpty() {
    // TODO Auto-generated method stub
    return false;
  }

  /**
   * TODO Javadoc.
   * @return <code>null</code>
   */
  public Query toQuery() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * TODO Javadoc.
   */
  public void toXML(XMLWriter xml) throws IOException {
    // TODO Auto-generated method stub
  }

}
