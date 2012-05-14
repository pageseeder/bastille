/* Copyright (c) 2011 Allette Systems pty. ltd. */
package com.weborganic.bastille.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.topologi.diffx.xml.XMLWriter;

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
public class NavTreeHandler extends DefaultHandler implements ContentHandler {

  /*** The document type of master document ***/
  private final static String DOCUMENT_TYPE = "master";

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

  /*** The website root path in PageSeeder ***/
  private String psWebSiteRoot;

  /*** The website content folder in PageSeeder ***/
  private String psWebSiteContent;

  /**
   * @param dr defines the Object of DocumentRoot
   * @param maxlevel defines the maximum leve of tree.
   * @param xml defines the XMLWriter.
   */
  public NavTreeHandler(DocumentRoot dr, int maxlevel, XMLWriter xml) {
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

    if (content == null) {
      this.psWebSiteContent = "";
    } else {
      if (!content.endsWith("/")) {
        this.psWebSiteContent = content + "/";
      } else {
        this.psWebSiteContent = content;
      }
    }

  }

  /* (non-Javadoc)
   * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
   */
  @Override
  public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
    try {
      if ("property".equals(qName) && atts.getValue("name").equalsIgnoreCase("document-type")) {
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

  /* (non-Javadoc)
   * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
   */
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

  /* (non-Javadoc)
   * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
   */
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

  /***
   * Parase the XML
   * @param file defines the souce.
   * @param handler defines the handler
   * @param xml define the XML Writer.
   * @throws NullPointerException
   * @throws IOException
   */
  public synchronized static void parseXML(File file, DefaultHandler handler, XMLWriter xml) throws NullPointerException, IOException {
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
