/*
 * This file is part of the Bastille library.
 *
 * Available under a commercial licence, contact Weborganic.
 *
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package com.weborganic.bastille.psml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.LoggerFactory;
import org.weborganic.berlioz.xml.XMLCopy;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

import com.topologi.diffx.xml.XMLWriter;
import com.weborganic.bastille.util.Paths;

/**
 * Parses PSML to process the links.
 *
 * @author Christophe Lauret
 * @version 25 November 2012
 */
class PSMLLinkProcessorHandler extends DefaultHandler implements ContentHandler, LexicalHandler {

  /**
   * The maximum possible depth when processing the links.
   */
  private static final int MAX_DEPTH = 8;

  // class attributes
  // ----------------------------------------------------------------------------------------------

  /**
   * The current depth.
   */
  private final int _depth;

  /**
   * The list of outbound cross references found.
   */
  private final List<String> _types;

  /**
   * The list of outbound cross references found.
   */
  private final List<File> _links;

  /**
   * The path prefix to add to referenced documents such as images and paths.
   */
  private final String _shift;

  /**
   * The XMLCopy handler to make it easier to perform the copy from the handler method.
   *
   * MUST use the same XML writer.
   */
  private final XMLCopy _copy;

  /**
   * The base path for the file currently being processed.
   */
  private final PSMLFile _source;

  /**
   * The heading level to adjust.
   */
  private final int _level;

  // state variable
  // ----------------------------------------------------------------------------------------------

  /**
   * A state variable indicating whether we are currently processing the link
   */
  private boolean insideLink = false;

  /**
   * Creates new handler with a depth of 1.
   *
   * @param source the source PSML document
   */
  public PSMLLinkProcessorHandler(PSMLFile source) {
    this(source, (XMLWriter)null);
  }

  /**
   * Creates new handler with a depth of 1.
   *
   * @param source the source PSML document
   * @param xml    the XML writer to use.
   */
  public PSMLLinkProcessorHandler(PSMLFile source, XMLWriter xml) {
    this._source = source;
    this._depth = 1;
    this._types = Collections.singletonList("transclude");
    this._shift = "";
    this._copy = xml != null? new XMLCopy(xml) : null;
    this._links = new ArrayList<File>();
    this._links.add(source.file());
    this._level = 0;
  }

  /**
   * Creates new handler with a depth of 1.
   *
   * @param source the source PSML document
   * @param parent the parent handler.
   * @param level  the level attribute from the cross-reference.
   */
  public PSMLLinkProcessorHandler(PSMLFile source, PSMLLinkProcessorHandler parent, int level) {
    this._source = source;
    this._depth = parent._depth + 1;
    this._types = parent._types;
    this._copy = parent._copy;
    this._links = parent._links;
    this._links.add(source.file());
    this._shift = Paths.path(parent._source.getBase(), source.getBase())+"/";
    this._level = parent._level + level;
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    // Copy the XML
    if (this._copy != null) {
      Attributes modified = attributes;
      if ("blockxref".equals(qName) || "xref".equals(qName) || "reversexref".equals(qName)) {
        // Update the references
        String value = Paths.normalize(this._shift + attributes.getValue("href"));
        modified = update(attributes, "href", value);

      } else if ("image".equals(qName)) {
        // Update the path to images
        String value = Paths.normalize(this._shift + attributes.getValue("src"));
        modified = update(attributes, "src", value);

      } else if ("heading".equals(qName)) {
        // Update the heading level
        String value = Integer.toString(toLevel(attributes) + this._level);
        modified = update(attributes, "level", value);

      }
      this._copy.startElement(uri, localName, qName, modified);
    }

    // Process the blockxref if there is one
    if ("blockxref".equals(qName)) {

      // Only process xref matching the correct xref type and media type
      String type = attributes.getValue("type");
      String mediatype = attributes.getValue("mediatype");
      int level = toLevel(attributes);
      if (this._depth < MAX_DEPTH && this._types.contains(type) && isProcessable(mediatype)) {

        // compute path to target file
        String base = this._source.getBase();
        String path = base + attributes.getValue("href");
        if (path.indexOf('/') == 0) path = path.substring(1);
        if (path.endsWith(".psml")) path = path.substring(0, path.length()-5);

        // grab the level (if we need to adjust the headings)

        PSMLFile target = PSMLConfig.getFile(path);
        if (target.exists()) {
          if (this._copy != null) {
            this.insideLink = true;
            try {
              PSMLLinkProcessor.processLinks(target, new PSMLLinkProcessorHandler(target, this, level));
            } catch (IOException ex) {
              throw new SAXException("Unable to transclude content of "+target);
            }
          }
        } else {
          String comment = "Unable to find content for transclusion";
          this._copy.comment(comment.toCharArray(), 0, comment.length());
        }
      }
    }
  }

  // Simply Copy
  // ---------------------------------------------------------------------------------------------------------

  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    // Copy the XML
    if (this._copy != null)
      this._copy.endElement(uri, localName, qName);

    if ("blockxref".equals(qName)) this.insideLink = false;
  }

  @Override
  public void characters(char[] ch, int start, int length) throws SAXException {
    // Copy the XML
    if (this._copy != null && !this.insideLink)
      this._copy.characters(ch, start, length);
  }

  @Override
  public void startPrefixMapping(String prefix, String uri) throws SAXException {
    // Copy the XML
    if (this._copy != null)
      this._copy.startPrefixMapping(prefix, uri);
  }

  @Override
  public void processingInstruction(String target, String data) throws SAXException {
    // Copy the XML
    if (this._copy != null)
      this._copy.processingInstruction(target, data);
  }

  @Override
  public void comment(char[] ch, int start, int length) throws SAXException {
    // Copy the XML
    if (this._copy != null)
      this._copy.comment(ch, start, length);
  }

  @Override
  public void startCDATA() throws SAXException {
  }

  @Override
  public void endCDATA() throws SAXException {
  }

  @Override
  public void startDTD(String name, String publicId, String systemId) throws SAXException {
  }

  @Override
  public void endDTD() throws SAXException {
  }

  @Override
  public void startEntity(String name) throws SAXException {
  }

  @Override
  public void endEntity(String name) throws SAXException {
  }

  // Other public methods
  // ---------------------------------------------------------------------------------------------------------

  /**
   * @return the links
   */
  public List<File> getLinks() {
    return this._links;
  }

  // Private helpers
  // ---------------------------------------------------------------------------------------------------------

  /**
   * Indicates whether it is processable.
   *
   * @param mediatype The media type.
   *
   * @return <code>true</code> if the mediatype is either "text/xml" or "application/vnd.pageseeder.psml+xml";
   *         <code>false</code> otherwise.
   */
  private static boolean isProcessable(String mediatype) {
    return "application/vnd.pageseeder.psml+xml".equals(mediatype) || "text/xml".equals(mediatype);
  }

  /**
   * Returns the level as an int form the attributes.
   *
   * @param attributes the attributes.
   * @return the corresponding level
   *
   * @throws NullPointerException If the attributes parameter is <code>null</code>
   */
  private static int toLevel(Attributes attributes) {
    String value = attributes.getValue("level");
    int level = 0;
    if (value != null) {
      try {
        level = Integer.parseInt(value);
      } catch (NumberFormatException ex) {
        LoggerFactory.getLogger(PSMLLinkProcessorHandler.class).warn("Unparsable level attribute found");
      }
    }
    return level;
  }

  /**
   * Updates the attributes so that the attributes include the new attribute.
   *
   * This method will automatically add or update the attribute.
   *
   * @param atts  The attributes
   * @param name  The name of the attribute to update
   * @param value The value of the attribute to update
   *
   * @return A new set of attributes
   */
  private static Attributes update(Attributes atts, String name, String value) {
    AttributesImpl updated = new AttributesImpl(atts);
    int i = atts.getIndex(name);
    if (i != -1) {
      updated.setAttribute(i, "", name, name, "CDATA", value);
    } else {
      updated.addAttribute("", name, name, "CDATA", value);
    }
    return updated;
  }

}
