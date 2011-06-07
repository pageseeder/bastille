/*
 * Copyright (c) 2011 weborganic systems pty. ltd.
 */
package com.weborganic.bastille.pageseeder;

import org.weborganic.berlioz.Beta;
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.DefaultHandler;

import com.topologi.diffx.xml.XMLWriter;

/**
 * Defines a handler for XML returned by PageSeeder for use by the PS Connector. 
 * 
 * <p>Implementations should write the XML output to the XML specified by the 
 * {@link #setOutput(XMLWriter)} method. This method is called before the handler starts parsing.
 * 
 * @author Christophe Lauret
 * @version 0.6.2 - 12 April 2011
 * @since 0.6.2
 */
@Beta public abstract class PSHandler extends DefaultHandler implements ContentHandler {

  /**
   * The XML writer to use. 
   */
  protected XMLWriter _xml;

  /**
   * Sets the XML Writer that this handler should use to write the XML.
   * 
   * @param xml An XML Writer implementation. 
   */
  public final void setXMLWriter(XMLWriter xml) {
    this._xml = xml;
  }

  /**
   * @return The XML Writer 
   */
  public final XMLWriter getXMLWriter() {
    return this._xml;
  }
}
