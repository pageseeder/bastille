/*
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package org.weborganic.bastille.system;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weborganic.bastille.recaptcha.ReCaptcha;
import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentRequest;
import org.weborganic.berlioz.content.GeneratorListener;
import org.weborganic.berlioz.content.Initializable;
import org.weborganic.berlioz.servlet.BerliozConfig;

import com.topologi.diffx.xml.XMLWriter;

/**
 * @author Christophe Lauret
 *
 * @version Bastille 0.8.6 - 6 February 2013
 */
public class GetBerliozStatistics implements ContentGenerator, Initializable {

  /**
   * A logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(ReCaptcha.class);

  @Override
  public void init() {
    LOGGER.info("Hello!");
  }

  @Override
  public void destroy() {
    LOGGER.info("Bye bye!");
  }

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
    collector.toXML(xml);
  }

}
