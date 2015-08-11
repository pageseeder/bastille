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

import org.pageseeder.berlioz.Beta;
import org.pageseeder.xmlwriter.XMLWriter;
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Defines a handler for XML returned by PageSeeder for use by the PS Connector.
 *
 * <p>Implementations should write the XML output to the XML specified by the
 * {@link #setOutput(XMLWriter)} method. This method is called before the handler starts parsing.
 *
 * @author Christophe Lauret
 * @version 0.6.11 - 15 August 2011
 * @since 0.6.2
 */
@Beta
public abstract class PSHandler extends DefaultHandler implements ContentHandler {

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
