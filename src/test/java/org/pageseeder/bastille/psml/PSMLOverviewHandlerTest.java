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
package org.pageseeder.bastille.psml;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import static org.junit.jupiter.api.Assertions.*;

class PSMLOverviewHandlerTest {

  private PSMLOverviewHandler handler;
  private static final AttributesImpl EMPTY_ATTRS = new AttributesImpl();

  @BeforeEach
  void setUp() throws SAXException {
    handler = new PSMLOverviewHandler();
    handler.startDocument();
  }

  // --- title extraction ---

  @Test
  void getTitle_noHeading_returnsNull() {
    assertNull(handler.getTitle());
  }

  @Test
  void getTitle_afterHeadingElement_returnsText() throws SAXException {
    handler.startElement("", "heading", "heading", EMPTY_ATTRS);
    handler.characters("My Title".toCharArray(), 0, 8);
    handler.endElement("", "heading", "heading");

    assertEquals("My Title", handler.getTitle());
  }

  @Test
  void getTitle_onlyFirstHeadingCaptured() throws SAXException {
    handler.startElement("", "heading", "heading", EMPTY_ATTRS);
    handler.characters("First".toCharArray(), 0, 5);
    handler.endElement("", "heading", "heading");

    handler.startElement("", "heading", "heading", EMPTY_ATTRS);
    handler.characters("Second".toCharArray(), 0, 6);
    handler.endElement("", "heading", "heading");

    assertEquals("First", handler.getTitle());
  }

  // --- summary extraction ---

  @Test
  void getSummary_noPara_returnsNull() {
    assertNull(handler.getSummary());
  }

  @Test
  void getSummary_afterParaElement_returnsText() throws SAXException {
    handler.startElement("", "para", "para", EMPTY_ATTRS);
    handler.characters("Summary text".toCharArray(), 0, 12);
    handler.endElement("", "para", "para");

    assertEquals("Summary text", handler.getSummary());
  }

  @Test
  void getSummary_onlyFirstParaCaptured() throws SAXException {
    handler.startElement("", "para", "para", EMPTY_ATTRS);
    handler.characters("First".toCharArray(), 0, 5);
    handler.endElement("", "para", "para");

    handler.startElement("", "para", "para", EMPTY_ATTRS);
    handler.characters("Second".toCharArray(), 0, 6);
    handler.endElement("", "para", "para");

    assertEquals("First", handler.getSummary());
  }

  // --- property extraction ---

  @Test
  void getProperties_empty_returnsEmptyMap() {
    assertTrue(handler.getProperties().isEmpty());
  }

  @Test
  void getProperties_propertyWithValueAttribute_capturedFromAttribute() throws SAXException {
    AttributesImpl attrs = new AttributesImpl();
    attrs.addAttribute("", "name", "name", "CDATA", "author");
    attrs.addAttribute("", "value", "value", "CDATA", "Alice");

    handler.startElement("", "property", "property", attrs);
    handler.endElement("", "property", "property");

    assertEquals("Alice", handler.getProperties().get("author"));
  }

  @Test
  void getProperties_propertyWithTextContent_capturedFromText() throws SAXException {
    AttributesImpl attrs = new AttributesImpl();
    attrs.addAttribute("", "name", "name", "CDATA", "status");

    handler.startElement("", "property", "property", attrs);
    handler.characters("draft".toCharArray(), 0, 5);
    handler.endElement("", "property", "property");

    assertEquals("draft", handler.getProperties().get("status"));
  }

  @Test
  void getProperties_attributeValueTakesPrecedenceOverText() throws SAXException {
    AttributesImpl attrs = new AttributesImpl();
    attrs.addAttribute("", "name", "name", "CDATA", "key");
    attrs.addAttribute("", "value", "value", "CDATA", "attr-val");

    handler.startElement("", "property", "property", attrs);
    handler.characters("text-val".toCharArray(), 0, 8);
    handler.endElement("", "property", "property");

    assertEquals("attr-val", handler.getProperties().get("key"));
  }

  @Test
  void getProperties_multipleProperties_allCaptured() throws SAXException {
    AttributesImpl attrs1 = new AttributesImpl();
    attrs1.addAttribute("", "name", "name", "CDATA", "a");
    attrs1.addAttribute("", "value", "value", "CDATA", "1");

    AttributesImpl attrs2 = new AttributesImpl();
    attrs2.addAttribute("", "name", "name", "CDATA", "b");
    attrs2.addAttribute("", "value", "value", "CDATA", "2");

    handler.startElement("", "property", "property", attrs1);
    handler.endElement("", "property", "property");
    handler.startElement("", "property", "property", attrs2);
    handler.endElement("", "property", "property");

    assertEquals("1", handler.getProperties().get("a"));
    assertEquals("2", handler.getProperties().get("b"));
  }

  // --- startDocument resets state ---

  @Test
  void startDocument_resetsState() throws SAXException {
    handler.startElement("", "heading", "heading", EMPTY_ATTRS);
    handler.characters("Old Title".toCharArray(), 0, 9);
    handler.endElement("", "heading", "heading");

    handler.startDocument();

    assertNull(handler.getTitle());
    assertNull(handler.getSummary());
    assertTrue(handler.getProperties().isEmpty());
  }

  // --- characters not buffered outside tracked elements ---

  @Test
  void characters_outsideTrackedElement_ignored() throws SAXException {
    handler.characters("noise".toCharArray(), 0, 5);
    assertNull(handler.getTitle());
    assertNull(handler.getSummary());
  }
}
