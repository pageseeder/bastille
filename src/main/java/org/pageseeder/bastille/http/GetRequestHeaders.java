/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.pageseeder.bastille.http;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.pageseeder.berlioz.BerliozException;
import org.pageseeder.berlioz.GlobalSettings;
import org.pageseeder.berlioz.content.ContentGenerator;
import org.pageseeder.berlioz.content.ContentRequest;
import org.pageseeder.berlioz.servlet.HttpRequestWrapper;

import org.pageseeder.xmlwriter.XMLWriter;

/**
 * This generator returns the HTTP request headers as XML.
 *
 * <h3>Configuration</h3>
 * <p>The root XML folder can be configured globally using the Berlioz configuration:
 * <p>For example:
 * <pre>{@code
 * <node name="bastille">
 *   <map/>
 *   <node name="http">
 *     <map>
 *       <entry key="headers" value="User-Agent,Accept-Language"/>
 *     </map>
 *   </node>
 * </node>
 * }</pre>
 *
 * <h3>Parameters</h3>
 * <p>The <var>match</var> parameter can be used to specify which headers should be displayed as a
 * comma separated list.
 * If <code>null</code> or equal to "", all HTTP headers are returned.
 *
 * <h3>Returned XML</h3>
 * <p>XML for a file:
 * <pre>{@code
 *   <http-headers match="[header-match]">
 *      <header name="[header-name]" value="[header-value]"/>
 *      <header name="[header-name]" value="[header-value]"/>
 *      <header name="[header-name]" value="[header-value]"/>
 *      <header name="[header-name]" value="[header-value]"/>
 *      <header name="[header-name]" value="[header-value]"/>
 *      ...
 *   </http-headers>
 * }</pre>
 *
 * <p>Note: this generator does not return anything, if the content request is not made from an
 * HTTP request.
 *
 * @author Christophe Lauret
 * @version 0.6.7 - 31 May 2011
 * @since 0.6.3
 */
public final class GetRequestHeaders implements ContentGenerator {


  @SuppressWarnings("unchecked")
  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {
    if (req instanceof HttpRequestWrapper) {
      HttpServletRequest r = ((HttpRequestWrapper)req).getHttpRequest();
      String match = req.getParameter("match", GlobalSettings.get("bastille.http.headers", "*"));
      List<String> matching = matching(match);

      // Start serialising XML
      xml.openElement("http-headers");
      xml.attribute("match", match);

      // Iterate over the header names
      Enumeration<String> names = r.getHeaderNames();
      while (names.hasMoreElements()) {
        String name = names.nextElement();

        if (matching.size() == 0 || matching.contains(name)) {
          // Iterate over the header values
          Enumeration<String> values = r.getHeaders(name);
          while (values.hasMoreElements()) {
            String value = values.nextElement();
            xml.openElement("header");
            xml.attribute("name", name);
            xml.attribute("value", value);
            xml.closeElement();
          }
        }
      }
      xml.closeElement();
    }
  }

  /**
   * Returns the list of matching headers.
   * @param match A comma separated list of matching
   * @return the list of matching headers.
   */
  private List<String> matching(String match) {
    if ("*".equals(match)) return Collections.emptyList();
    List<String> matching = new ArrayList<String>();
    for (String m : match.split("\\s*,\\s*")) {
      if (m.length() > 0) {
        matching.add(m);
      }
    }
    return matching;
  }

}
