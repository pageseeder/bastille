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
package org.pageseeder.bastille.pageseeder;

import org.pageseeder.berlioz.xml.XMLCopy;
import org.pageseeder.xmlwriter.XMLWriter;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

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
    if (xml == null) throw new IllegalStateException("No XML to copy to!");
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
