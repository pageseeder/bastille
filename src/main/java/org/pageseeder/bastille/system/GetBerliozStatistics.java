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
package org.pageseeder.bastille.system;

import java.io.IOException;

import org.pageseeder.bastille.recaptcha.ReCaptcha;
import org.pageseeder.berlioz.BerliozException;
import org.pageseeder.berlioz.content.ContentGenerator;
import org.pageseeder.berlioz.content.ContentRequest;
import org.pageseeder.berlioz.content.GeneratorListener;
import org.pageseeder.berlioz.servlet.BerliozConfig;
import org.pageseeder.xmlwriter.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christophe Lauret
 *
 * @version Bastille 0.8.6 - 6 February 2013
 */
public class GetBerliozStatistics implements ContentGenerator {

  /**
   * A logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(ReCaptcha.class);

  /**
   * Will also create and bind a statistics collector to Berlioz.
   */
  public GetBerliozStatistics() {
    GeneratorListener listener = BerliozConfig.getListener();
    BerliozStatisticsCollector collector = BerliozStatisticsCollector.getInstance();
    if (BerliozConfig.getListener() == null) {
      BerliozConfig.setListener(collector);
    } else if (collector != listener) {
      LOGGER.warn("Unable to initialise the Berlioz statistics");
    }
  }

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {
    BerliozStatisticsCollector collector = BerliozStatisticsCollector.getInstance();

    if ("true".equals(req.getParameter("reset", "false"))) {
      collector.clear();
    }

    collector.toXML(xml);
  }

}
