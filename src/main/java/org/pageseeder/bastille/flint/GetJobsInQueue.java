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
package org.pageseeder.bastille.flint;

import java.io.IOException;
import java.util.List;

import org.pageseeder.bastille.flint.config.FlintConfig;
import org.pageseeder.berlioz.BerliozException;
import org.pageseeder.berlioz.content.ContentGenerator;
import org.pageseeder.berlioz.content.ContentRequest;
import org.pageseeder.flint.IndexJob;
import org.pageseeder.flint.IndexManager;
import org.pageseeder.xmlwriter.XMLWriter;

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
    if (!"true".equals(req.getParameter("count-only"))) {
      for (IndexJob job : jobs) {
        toXML(job, xml);
      }
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
