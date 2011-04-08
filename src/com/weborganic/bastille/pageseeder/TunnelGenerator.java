package com.weborganic.bastille.pageseeder;

import java.io.IOException;

import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentRequest;

import com.topologi.diffx.xml.XMLWriter;

/**
 * A generator than can tunnel a request through to PageSeeder.
 * 
 * <p>Use this generator to connect to servlet, service, etc... when no other generator provides
 * the same functionality. 
 * 
 * @author Christophe Lauret
 * @version 8 April 2011
 */
public final class TunnelGenerator implements ContentGenerator {

  /**
   * {@inheritDoc}
   */
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {

    // Servlet class
    String servlet = req.getParameter("servlet");

    PSRequest tunnel = new PSRequest(PSResourceType.SERVLET, servlet);
    xml.openElement("tunnel", true);

    // Resource
    xml.openElement("resource");
    xml.attribute("type", "servlet");
    xml.attribute("name", servlet);
    xml.closeElement();

    // Grab the XML form the PageSeeder request
    tunnel.get(xml);

    // Done!
    xml.closeElement();
  }

}
