/*
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package org.pageseeder.bastille.system;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.pageseeder.bastille.recaptcha.ReCaptcha;
import org.pageseeder.berlioz.BerliozException;
import org.pageseeder.berlioz.content.ContentGenerator;
import org.pageseeder.berlioz.content.ContentRequest;
import org.pageseeder.berlioz.content.GeneratorListener;
import org.pageseeder.berlioz.servlet.BerliozConfig;

import org.pageseeder.xmlwriter.XMLWriter;

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
