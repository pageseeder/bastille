/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.weborganic.bastille.pageseeder;

import org.weborganic.berlioz.xml.XMLCopy;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.topologi.diffx.xml.XMLWriter;

/**
 * A PS handler which can be used to copy the content and retrieve information at the same time.
 *
 * <p>This class is intended to be subclassed.
 *
 * <p>The {@link #setXMLWriter(XMLWriter)} method must be called before the {@link #startDocument()} is invoked
 * or it will throw an {@link IllegalStateException} exception.
 *
 * @author Christophe Lauret
 * @version 3 November 2011
 */
public class PSXMLCopy extends PSHandler {

  /**
   * We are going to make a copy.
   */
  protected XMLCopy copy = null;

  @Override
  public void startDocument() throws SAXException {
    XMLWriter xml = getXMLWriter();
    if (xml == null) {
      throw new IllegalStateException("No XML to copy to!");
    }
    this.copy = new XMLCopy(xml);
    this.copy.startDocument();
  }

  @Override
  public void endDocument() throws SAXException {
    this.copy.endDocument();
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
    this.copy.startElement(uri, localName, qName, atts);
  }

  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    this.copy.endElement(uri, localName, qName);
  }

  @Override
  public void startPrefixMapping(String prefix, String uri) throws SAXException {
    this.copy.startPrefixMapping(prefix, uri);
  }

  @Override
  public void endPrefixMapping(String prefix) throws SAXException {
    this.copy.endPrefixMapping(prefix);
  }

  @Override
  public void processingInstruction(String target, String data) throws SAXException {
    this.copy.processingInstruction(target, data);
  }

  @Override
  public void characters(char[] ch, int start, int length) throws SAXException {
    this.copy.characters(ch, start, length);
  }

  @Override
  public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
    this.copy.ignorableWhitespace(ch, start, length);
  }

  @Override
  public void skippedEntity(String name) throws SAXException {
    this.copy.skippedEntity(name);
  }

}
