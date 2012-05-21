/*
 * This file is part of the Bastille library.
 *
 * Available under a commercial licence, contact Weborganic.
 *
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package com.weborganic.bastille.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.content.Cacheable;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentRequest;
import org.weborganic.berlioz.util.MD5;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
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

  /**
   * The helper object class to contain the information for Berlioz and PageSeeder
   *
   * <ul>
   *   <li><code>psWebsiteRoot</code> defines the PageSeeder Website Root Folder.</li>
   *   <li><code>psWebsiteContent</code> defines the PageSeeder Website Content Folder.</li>
   *   <li><code>berliozXMLRoot</code> defines the Berlioz XML Folder.</li>
   * </ul>
   *
   */
  private static class DocumentRoot {
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

  /**
   * This is a SAX extension handler.
   * It is to resolve the cross reference and process the xml tree based on cross reference links.
   *
   * <h3>The XML Tree Structure</h3>
   * <pre> {@code
   * <navs>
   *  <nav level="1" title="my nav 1" href="file1.xml">
   *    <nav level="2" title="my nav 1" href="nav2/file1.xml">
   *    <nav level="2" title="my nav 2" href="nav2/file2.xml">
   *    <nav level="2" title="my nav 3" href="nav2/file3.xml">
   *    ...
   *  </nav>
   *  ....
   *  </navs>
   *
   * } </pre>
   *
   * @author Ciber Cai
   * @version 11 May 2011
   *
   */
  private static class NavTreeHandler extends DefaultHandler implements ContentHandler {

    /*** The document type of master document ***/
    private static final String DOCUMENT_TYPE = "master";

    private static final Logger LOGGER = LoggerFactory.getLogger(NavTreeHandler.class);

    /*** Is the master document **/
    private static boolean isMaster = false;

    /*** The XML Writer **/
    private final XMLWriter to;

    private boolean inProp = false;
    private boolean inElem = false;
    private String element;
    private String xref;
    private int level;
    private int maxLevel;

    /** The XML ROOT Folder in Berlioz **/
    private File rootFolder;

    /** The website root path in PageSeeder **/
    private String psWebSiteRoot;

    /** The website content folder in PageSeeder **/
    private String psWebSiteContent;

    /**
     * @param dr defines the Object of DocumentRoot
     * @param maxlevel defines the maximum leve of tree.
     * @param xml defines the XMLWriter.
     */
    NavTreeHandler(DocumentRoot dr, int maxlevel, XMLWriter xml) {
      this(xml, dr.getBerliozXMLRoot(), dr.getPSWebsiteRoot(), dr.getPSWebsiteContent(), 1, maxlevel);
    }

    /**
     * The private constructor for inner loop use.
     *
     * @param xml defines the XML Writer.
     * @param rootfolder defines the XML ROOT Folder in berlioz.
     * @param documentroot defines the website root path in PageSeeder
     * @param content defines the website content in PageSeeder
     * @param level the current level of tree.
     * @param maxlevel defines the maximum level of tree.
     */
    private NavTreeHandler(XMLWriter xml, File rootfolder, String documentroot, String content, int level, int maxlevel) {
      this.to = xml;
      this.element = "xref";
      this.rootFolder = rootfolder;
      this.level = level;
      this.maxLevel = maxlevel;

      if (documentroot == null) {
        this.psWebSiteRoot = "";
      } else {
        if (!documentroot.endsWith("/")) {
          this.psWebSiteRoot = documentroot + "/";
        } else {
          this.psWebSiteRoot = documentroot;
        }
      }

      if (content == null || content.isEmpty()) {
        this.psWebSiteContent = "";
      } else {
        if (!content.endsWith("/")) {
          this.psWebSiteContent = content + "/";
        } else {
          this.psWebSiteContent = content;
        }
      }

    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
      try {
        if ("property".equals(qName) && "document-type".equalsIgnoreCase(atts.getValue("name"))) {
          this.inProp = true;
        }

        if (this.element.equals(qName) && isMaster) {
          this.inElem = true;
        }

        if (this.inElem) {
          this.to.openElement("nav");
          this.to.attribute("level", this.level);

          if (atts.getValue("title") != null && !atts.getValue("title").isEmpty()) {
            this.to.attribute("title", atts.getValue("title"));
          } else {
            this.to.attribute("title", atts.getValue("urititle"));
          }

          // remove the PS website root folder
          String path = "";
          if (this.psWebSiteRoot != null) {
            path = atts.getValue("href").replace(this.psWebSiteRoot, "");
          }

          // remove the PS website content folder
          if (this.psWebSiteContent != null) {
            path = path.replace(this.psWebSiteContent, "");
          }

          File reqfile = new File(this.rootFolder, path);
          String status = reqfile != null && reqfile.exists() ? "true" : "false";

          this.xref = path;
          this.to.attribute("href", path);
          this.to.attribute("status", status);
          if (reqfile != null && reqfile.exists()) {
            this.to.attribute("lastmodifed", String.valueOf(reqfile.lastModified()));
          }

        }
      } catch (IOException ex) {
        throw new SAXException(ex);
      }

    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
      try {
        // close element.
        if (this.inElem) {
          // resolve the xref
          File hrefFile = new File(this.rootFolder, this.xref);

          if (hrefFile != null && hrefFile.exists() && this.level < this.maxLevel) {

            DefaultHandler handler = new NavTreeHandler(this.to, this.rootFolder, this.psWebSiteRoot, this.psWebSiteContent, (this.level + 1), this.maxLevel);
            parseXML(hrefFile, handler, this.to);
            hrefFile = null;
          }
        }

        if (this.element.equals(qName) && isMaster) {
          this.inElem = false;
          this.to.closeElement();
        }
      } catch (IOException ex) {
        throw new SAXException(ex);
      }

    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
      if (this.inProp) {
        StringBuffer st = new StringBuffer();
        st.append(ch, start, length);
        if (st.toString().equalsIgnoreCase(DOCUMENT_TYPE)) {
          isMaster = true;
        }
      }
    }

    /**
     * Parse the XML.
     *
     * @param file defines the souce.
     * @param handler defines the handler
     * @param xml define the XML Writer.
     *
     * @throws NullPointerException
     * @throws IOException
     */
    public synchronized static void parseXML(File file, DefaultHandler handler, XMLWriter xml) throws IOException {
      LOGGER.debug("Prase file {} ", file.getAbsoluteFile());

      SAXParserFactory factory = SAXParserFactory.newInstance();
      factory.setValidating(false);
      factory.setNamespaceAware(false);
      factory.setXIncludeAware(false);

      InputStream in = null;
      try {
        // Get the source as input stream
        in = new FileInputStream(file);
        if (in != null) {
          InputSource source = new InputSource(in);
          source.setEncoding("utf-8");
          SAXParser parser = factory.newSAXParser();
          parser.parse(source, handler);

        }
      } catch (ParserConfigurationException ex) {
        LOGGER.error("Error ", ex);
      } catch (SAXException ex) {
        LOGGER.error("Error ", ex);
      } finally {
        if (in != null) {
          in.close();
          in = null;
        }
      }
    }

  }
}

