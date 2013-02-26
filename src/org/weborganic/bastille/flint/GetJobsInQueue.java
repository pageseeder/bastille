/*
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package org.weborganic.bastille.flint;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weborganic.bastille.flint.config.FlintConfig;
import org.weborganic.bastille.flint.helpers.IndexMaster;
import org.weborganic.bastille.flint.helpers.IndexNames;
import org.weborganic.bastille.util.Errors;
import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentRequest;
import org.weborganic.flint.IndexJob;

import com.topologi.diffx.xml.XMLWriter;

/**
 * @author Christophe Lauret
 * @version 26 February 2013
 */
public class GetJobsInQueue implements ContentGenerator {

  /**
   * Logger for debugging
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(GetIndexTerms.class);

  /** Name of the index parameter. */
  private static final String INDEX_PARAMETER = "index";

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {

    String index = req.getParameter(INDEX_PARAMETER);

    if (index == null) {

      // Check that we are in single index config.
      if (FlintConfig.hasMultiple()) {
        LOGGER.warn("Attempted to request a single index in multiple index configuration");
        Errors.noParameter(req, xml, INDEX_PARAMETER);
        return;
      }

      // Single index
      IndexMaster single = FlintConfig.getMaster();
      List<IndexJob> jobs = single.getJobsInQueue();
      xml.openElement("index-jobs");
      for (IndexJob job : jobs) {
        toXML(job, xml);
      }
      xml.closeElement();

    } else {

      // Check that we are in single index config.
      if (!FlintConfig.hasMultiple()) {
        LOGGER.warn("Attempted to request a named index in single index configuration");
        Errors.invalidParameter(req, xml, INDEX_PARAMETER);
        return;
      }

      List<String> names = toIndexNames(index);

      // No valid index names where specified
      if (names.isEmpty()) {
        Errors.invalidParameter(req, xml, "index");
        return;
      }

      xml.openElement("indexes");
      for (String name : names) {
        IndexMaster master = FlintConfig.getMaster(name);
        List<IndexJob> jobs = master.getJobsInQueue();
        xml.openElement("index-jobs");
        xml.attribute("index", name);
        for (IndexJob job : jobs) {
          toXML(job, xml);
        }
        xml.closeElement();
      }
      xml.closeElement();
    }
  }

  /**
   * Return the list of index names from the specified index parameter.
   *
   * <p>Only includes names which are valid and corresponding to an existing index.
   *
   * @param index The index parameter
   * @return the corresponding list.
   */
  private static List<String> toIndexNames(String index) {
    List<String> names = new ArrayList<String>();

    // Check the index names
    for (String name : index.split(",")) {
      if (IndexNames.isValid(name)) {
        File f = new File(FlintConfig.directory(), name);
        if (f.exists() && f.isDirectory()) {
          names.add(name);
        } else {
          LOGGER.debug("Invalid index name '{}' was specified", name);
        }
      } else {
        LOGGER.debug("Invalid index name '{}' was specified", name);
      }
    }
    return names;
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
