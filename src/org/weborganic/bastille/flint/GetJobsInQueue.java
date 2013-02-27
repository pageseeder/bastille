/*
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package org.weborganic.bastille.flint;

import java.io.IOException;
import java.util.List;

import org.weborganic.bastille.flint.config.FlintConfig;
import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentRequest;
import org.weborganic.flint.IndexJob;
import org.weborganic.flint.IndexManager;

import com.topologi.diffx.xml.XMLWriter;

/**
 * @author Christophe Lauret
 * @version 0.8.9 - 25 February 2013
 * @since 0.8.8
 */
public class GetJobsInQueue implements ContentGenerator {

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {

    IndexManager manager = FlintConfig.getManager();
    List<IndexJob> jobs = manager.getStatus();

    // Serialise as XML
    xml.openElement("index-jobs");
    xml.attribute("count", jobs.size());
    for (IndexJob job : jobs) {
      toXML(job, xml);
    }
    xml.closeElement();
  }

  /**
   *
   * @param job The job to serialise as XML
   * @param xml The XMLwriter
   * @throws IOException Only an error occurs while writing XML
   */
  private static void toXML(IndexJob job, XMLWriter xml) throws IOException {
    xml.openElement("job");
    xml.attribute("content", job.getContentID().getID());
    xml.attribute("index", job.getIndex().getIndexID());
    xml.closeElement();
  }

}
