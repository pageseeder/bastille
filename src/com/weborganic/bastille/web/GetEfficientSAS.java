/*
 * Copyright (c) 2011 weborganic systems pty. ltd.
 */
package com.weborganic.bastille.web;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.content.Cacheable;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentRequest;
import org.weborganic.berlioz.content.Environment;
import org.weborganic.berlioz.content.Service;
import org.weborganic.berlioz.servlet.HttpContentRequest;

import com.topologi.diffx.xml.XMLWriter;
import com.weborganic.bastille.util.WebBundleTool;

/**
 * This generator returns the XML for the Footer.
 *
 * <h3>Configuration</h3>
 *
 *
 * <h3>Parameters</h3>
 *
 *
 * <h3>Returned XML</h3>
 *
 *
 * <h4>Error handling</h4>
 *
 * <h3>Usage</h3>
 *
 * <h3>ETag</h3>
 *
 * @author Christophe Lauret
 * @version 0.6.5 - 31 May 2010
 * @since 0.6.0
 */
public final class GetEfficientSAS implements ContentGenerator, Cacheable {


  @Override
  public String getETag(ContentRequest req) {
    return null;
  }

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {
    HttpContentRequest hreq = (HttpContentRequest)req;
    Service service = hreq.getService();
    String id = service.id();
    String group = service.group();
    Environment env = req.getEnvironment();

    // Scripts
    File sgl = env.getPublicFile("/script/global.js");
    File sgr = env.getPublicFile("/script/"+group+".js");
    File ssi = env.getPublicFile("/script/"+group+"/"+id+".js");

    // Styles
    File tgl = env.getPublicFile("/style/global.css");
    File tgr = env.getPublicFile("/style/"+group+".css");
    File tsi = env.getPublicFile("/style/"+group+"/"+id+".css");

    // Bundle
    File bundles = env.getPublicFile("/script/bundle/");
    if (!bundles.exists()) bundles.mkdirs();
    WebBundleTool bundler = new WebBundleTool(bundles);
    bundleScript(bundler, sgl, "global", xml);
    bundleScript(bundler, sgr, group, xml);
    bundleScript(bundler, ssi, id, xml);

    File bundles2 = env.getPublicFile("/style/bundle/");
    if (!bundles2.exists()) bundles2.mkdirs();
    WebBundleTool bundler2 = new WebBundleTool(bundles2);
    bundleStyle(bundler2, tgl, "global", xml);
    bundleStyle(bundler2, tgr, group, xml);
    bundleStyle(bundler2, tsi, id, xml);
  }

  private void bundleScript(WebBundleTool bundler, File f, String name, XMLWriter xml) throws IOException {
    if (f != null && f.exists()) {
      File bundle = bundler.bundle(Collections.singletonList(f), name, true);
      xml.openElement("script", false);
      xml.attribute("src", "/script/bundle/"+bundle.getName());
      xml.closeElement();
    }
  }

  private void bundleStyle(WebBundleTool bundler, File f, String name, XMLWriter xml) throws IOException {
    if (f != null && f.exists()) {
      File bundle = bundler.bundle(Collections.singletonList(f), name, true);
      xml.openElement("style", false);
      xml.attribute("src", "/style/bundle/"+bundle.getName());
      xml.closeElement();
    }
  }
}
