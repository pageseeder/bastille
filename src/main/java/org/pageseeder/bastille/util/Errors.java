/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.pageseeder.bastille.util;

import java.io.IOException;

import org.pageseeder.berlioz.Beta;
import org.pageseeder.berlioz.content.ContentRequest;
import org.pageseeder.berlioz.content.ContentStatus;

import org.pageseeder.xmlwriter.XMLWriter;

/**
 * A utility class for handling error situations in generators.
 *
 * @author Christophe Lauret
 * @version 26 September 2011
 */
@Beta
public final class Errors {

  /**
   * Utility class.
   */
  private Errors() {
  }

  // Client errors
  // ----------------------------------------------------------------------------------------------

  /**
   * Write the XML for when a required parameter is missing.
   *
   * <p>Also sets the status of the response to 'bad request'.
   *
   * <p>Generator should generally terminate after invoking this method.
   *
   * @param req  The content request
   * @param xml  The XML writer
   * @param name The name of the missing parameter
   *
   * @throws IOException          Should any error occur while writing the XML.
   * @throws NullPointerException If Any argument is <code>null</code>.
   */
  public static void noParameter(ContentRequest req, XMLWriter xml, String name) throws IOException {
    final String message = "The parameter '"+name+"' was not specified.";
    error(req, xml, "client", message, ContentStatus.BAD_REQUEST);
  }

  /**
   * Write the XML for when a required parameter is missing.
   *
   * <p>Also sets the status of the response to 'bad request'.
   *
   * <p>Generator should generally terminate after invoking this method.
   *
   * @param req  The content request
   * @param xml  The XML writer
   * @param name The name of the invalid parameter
   *
   * @throws IOException          Should any error occur while writing the XML.
   * @throws NullPointerException If Any argument is <code>null</code>.
   */
  public static void invalidParameter(ContentRequest req, XMLWriter xml, String name) throws IOException {
    final String message = "The parameter '"+name+"' is invalid.";
    error(req, xml, "client", message, ContentStatus.BAD_REQUEST);
  }

  /**
   * Write the XML for when the user has not logged in, but is required.
   *
   * <p>Also sets the status of the response to 'forbidden'.
   *
   * <p>Generator should generally terminate after invoking this method.
   *
   * @param req  The content request.
   * @param xml  The XML writer.
   *
   * @throws IOException          Should any error occur while writing the XML.
   * @throws NullPointerException If Any argument is <code>null</code>.
   */
  public static void noUser(ContentRequest req, XMLWriter xml) throws IOException {
    final String message = "The user must be logged in to access this information";
    error(req, xml, "client", message, ContentStatus.FORBIDDEN);
  }

  // Generic errors
  // ----------------------------------------------------------------------------------------------

  /**
   * Write the XML for when an error occurs
   *
   * <p>Also sets the status of the response.
   *
   * <p>Generator should generally terminate after invoking this method.
   *
   * @param req     The content request.
   * @param xml     The XML writer.
   * @param type    The type of error.
   * @param message The message to explain the error.
   * @param status  The new status of the request.
   *
   * @throws IOException          Should any error occur while writing the XML.
   * @throws NullPointerException If Any argument is <code>null</code>.
   */
  public static void error(ContentRequest req, XMLWriter xml, String type, String message, ContentStatus status)
      throws IOException {
    xml.openElement("error");
    xml.attribute("type", type);
    xml.attribute("message", message);
    xml.closeElement();
    req.setStatus(status);
  }

}
