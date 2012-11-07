/*
 * This file is part of the Bastille library.
 *
 * Available under a commercial licence, contact Weborganic.
 *
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package com.weborganic.bastille.psml;

import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parses PSML to generate the overview data.
 *
 * @author Christophe Lauret
 * @version 7 November 2012
 */
class PSMLOverviewHandler extends DefaultHandler {

   /**
    * The internal buffer.
    */
   private StringBuilder buffer = null;

   /**
    * The title if found (first heading).
    */
   private String title = null;

   /**
    * The summary if found (first paragraph).
    */
   private String summary = null;

   /**
    * The internal buffer.
    */
   private Map<String, String> properties = new HashMap<String, String>();

   /**
    * A state variable indicating that a property is being looked for.
    */
   private String property = null;

   @Override
   public void startDocument() throws SAXException {
     this.title = null;
     this.summary = null;
     this.properties.clear();
   }

   @Override
   public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
     if (this.title == null && "heading".equals(qName)) {
       this.buffer = new StringBuilder();
     } else if (this.summary == null && "para".equals(qName)) {
       this.buffer = new StringBuilder();
     } else if ("property".equals(qName)) {
       this.buffer = new StringBuilder();
       this.property = attributes.getValue("name");
       String value = attributes.getValue("value");
       if (value != null)
         this.properties.put(this.property, value);
     }
   }

   @Override
   public void endElement(String uri, String localName, String qName) throws SAXException {
     if (this.title == null && "heading".equals(qName)) {
       this.title = this.buffer.toString();
       this.buffer = null;
     } else if (this.summary == null && "para".equals(qName)) {
       this.summary = this.buffer.toString();
       this.buffer = null;
     } else if ("property".equals(qName)) {
       if (!this.properties.containsKey(this.property))
         this.properties.put(this.property, this.buffer.toString());
       this.property = null;
       this.buffer = null;
     }
   }

   @Override
   public void characters(char[] ch, int start, int length) throws SAXException {
     if (this.buffer != null)
       this.buffer.append(ch, start, length);
   }

   /**
    * @return the title (content of first <code>heading1</code> element).
    */
   public String getTitle() {
     return this.title;
   }

   /**
    * @return the summary of the file (content of first <code>para</code> element).
    */
   public String getSummary() {
     return this.summary;
   }

   /**
    * @return the summary of the file (content of first <code>para</code> element).
    */
   public Map<String, String> getProperties() {
     return this.properties;
   }

}
