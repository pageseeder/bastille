package com.weborganic.bastille.flint.helpers;

import java.io.IOException;

import org.apache.lucene.search.Query;
import org.weborganic.flint.query.SearchParameter;

import com.topologi.diffx.xml.XMLWriter;

/**
 * The selected value for a facet
 * 
 * @author christophe
 *
 */
public class FacetValue implements SearchParameter {

  

  /**
   * 
   */
  public boolean isEmpty() {
    // TODO Auto-generated method stub
    return false;
  }

  /**
   * 
   */
  public Query toQuery() {
    // TODO Auto-generated method stub
    return null;
  }
  
  
  public void toXML(XMLWriter xml) throws IOException {
    // TODO Auto-generated method stub
  }

}
