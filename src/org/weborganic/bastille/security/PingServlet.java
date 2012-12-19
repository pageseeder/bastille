/*
 * This file is part of the Bastille library.
 *
 * Available under a commercial licence, contact Weborganic.
 *
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package org.weborganic.bastille.security;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.weborganic.bastille.util.Resources;


/**
 * A Servlet to allow another ping servlet to ping it.
 *
 * @author Christophe Lauret
 * @version 9 January 2011
 */
public final class PingServlet extends HttpServlet {

  /**
   * As per requirement for the <code>Serializable</code> interface.
   */
  private static final long serialVersionUID = -3343755604269705856L;

  /**
   * If the content type is specified.
   */
  private String contentType = null;

  /**
   * The corresponding data.
   */
  private byte[] data = null;

  @Override
  public void init(ServletConfig config) throws ServletException {
    String content = config.getInitParameter("content-type");
    if ("image/png".equals(content)) {
      this.contentType = content;
      this.data = Resources.getResource("com/weborganic/bastille/security/resource/tick.png");
    }
  }

  /**
   * Logs the user out by invalidating the session.
   *
   * {@inheritDoc}
   */
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

    // If the data is defined and found
    if (this.data != null) {

      // Set the headers
      res.setContentType(this.contentType);
      res.setContentLength(this.data.length);

      // Copy the data
      ServletOutputStream out = res.getOutputStream();
      out.write(this.data);
      out.close();

    } else {
      res.setStatus(HttpServletResponse.SC_NO_CONTENT);
      res.setContentLength(0);
    }

  }

  /**
   * Same as when using GET.
   *
   * {@inheritDoc}
   */
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    doGet(req, res);
  }

}
