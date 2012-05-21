/*
 * This file is part of the Bastille library.
 *
 * Available under a commercial licence, contact Weborganic.
 *
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package com.weborganic.bastille.xml;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.content.Cacheable;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentRequest;
import org.weborganic.berlioz.util.MD5;
import org.xml.sax.helpers.DefaultHandler;

import com.topologi.diffx.xml.XMLWriter;
import com.topologi.diffx.xml.XMLWriterImpl;

/**
 *
 * Generate the Navigation Tree.
 * <h3>Configuration</h3>
 * <p>No configuration required for this generator.</p>
 *
 * <h3>Parameters</h3>
 * <ul>
 * <li><code>master-file</code> defines the location of master file.</li>
 * <li><code>display-level</code> (optional) defines the resolve level. (default: 2)</li>
 * <li><code>pswebsite-root</code> (optional) defines the path of website root in PageSeeder. </li>
 * <li><code>pswebsite-content</code> (optional) defines the content folder in PageSeeder. </li>
 * </ul>
 *
 * <h3>Returned XML</h3>
 * <pre>{@code
 *  <navs>
 *    <nav level="1" title="1 Introduction" href="/publications/wbm/1_Introduction/1_Introduction.xml">
 *      <nav level="2" title="Introduction" href="/publications/wbm/1_Introduction/Introduction_women_s_health.xml"/>
 *      <nav level="2" title="Looking after women's health" href="/publications/wbm/1_Introduction/Looking_after_women_s_health.xml"/>
 *    </nav>
 *    <nav level="1" title="2  Emergencies" href="/publications/wbm/2_Emergencies/2_Emergencies.xml">
 *      <nav level="2" title="Coping after emergencies" href="/publications/wbm/2_Emergencies/Coping_with_emergencies.xml"/>
 *      <nav level="2" title="How to manage emergencies" href="/publications/wbm/2_Emergencies/How_to_manage_emergencies.xml"/>
 *      ...
 *      <nav level="2" title="Heavy vaginal bleeding" href="/publications/wbm/2_Emergencies/Heavy_vaginal_bleeding.xml"/>
 *    </nav>
 *  <navs>
 * }</pre>
 *
 * <h3>Configuration </h3>
 * <pre>{@code
 * <generator class="com.weborganic.bastille.xml.GetNavTreeFromMasterDoc" name="sourcemasterxml" target="main">
 *   <parameter name="pswebsite-root" value="content"/>
 *   <parameter name="master-file" value="ui5/ui5"/>
 *   <parameter name="display-level" value="3"/>
 * </generator>
 * }</pre>
 *
 * @author Ciber Cai
 * @version 11 May 2011
 *
 */

public class GetNavTreeFromMasterDoc implements ContentGenerator, Cacheable {
  /**
   * Logger for debugging
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(GetNavTreeFromMasterDoc.class);

  @Override
  public String getETag(ContentRequest req) {
    StringBuffer etag = new StringBuffer();
    etag.append(req.getParameter("master-file", "/"));
    etag.append(req.getParameter("pswebsite-root", "/"));
    etag.append(req.getIntParameter("display-level", 2));
    etag.append(req.getParameter("pswebsite-content", ""));

    try {
      // get the content as a etag
      etag.append(buildNavTree(req));
    } catch (IOException e) {
      etag.append("incorrect");
    }
    return MD5.hash(etag.toString());

  }

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {
    String treeData = "";
    try {
      // get the content as a etag
      treeData = buildNavTree(req);
    } catch (IOException e) {
      treeData = "";
    }

    String status = treeData != null && !treeData.isEmpty() ? "true" : "false";
    // output master doc tree
    xml.openElement("navs");
    xml.attribute("status", status);
    xml.writeXML(treeData);
    xml.closeElement();
    xml.close();
  }

  /***
   * Build the Navigation Tree based on master document.
   * @param req defines the content request.
   * @return return the content of the whole tree in string type.
   * @throws IOException
   */
  private String buildNavTree(ContentRequest req) throws IOException {
    LOGGER.debug("master-file {}", req.getParameter("master-file", ""));
    LOGGER.debug("display-level {} ", req.getParameter("display-level", "2"));

    StringWriter output = new StringWriter();
    XMLWriter xml = new XMLWriterImpl(output);

    String reqFilePath = req.getParameter("master-file", "");
    String psWebsiteRoot = req.getParameter("pswebsite-root", "/");
    String psWebsiteContent = req.getParameter("pswebsite-content", "");
    int displaylevel = req.getIntParameter("display-level", 2);

    DocumentRoot dr = new DocumentRoot(psWebsiteRoot, psWebsiteContent, XMLConfiguration.getXMLRootFolder(req));

    // add extension
    if (reqFilePath != null && !reqFilePath.contains(".xml")) {
      if (reqFilePath.endsWith("/")) {
        reqFilePath.substring(0, (reqFilePath.toString().length() - 1));
      }
      reqFilePath += ".xml";
    }

    File navFile = new File(dr.getBerliozXMLRoot(), reqFilePath);
    LOGGER.debug("Nav File {} exists {} ", navFile, navFile.exists());
    if (navFile != null && navFile.exists()) {
      DefaultHandler handler = new NavTreeHandler(dr, displaylevel, xml);
      NavTreeHandler.parseXML(navFile, handler, xml);
    }
    return output.toString();
  }
}

/***
 * The helper object class to contain the information for Berlioz and PageSeeder ***
 * <ul>
 * <li><code>psWebsiteRoot</code> defines the PageSeeder Website Root Folder.</li>
 * <li><code>psWebsiteContent</code> defines the PageSeeder Website Content Folder.</li>
 * <li><code>berliozXMLRoot</code> defines the Berlioz XML Folder.</li>
 * </ul>
 *
 */
class DocumentRoot {
  String psWebsiteRoot;
  String psWebsiteContent;
  File berliozXMLRoot;

  DocumentRoot(String r, String ct, File rp) {
    this.psWebsiteRoot = r;
    this.psWebsiteContent = ct;
    this.berliozXMLRoot = rp;
  }

  String getPSWebsiteRoot() {
    return this.psWebsiteRoot;
  }

  String getPSWebsiteContent() {
    return this.psWebsiteContent;
  }

  File getBerliozXMLRoot() {
    return this.berliozXMLRoot;
  }

}
