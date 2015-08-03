package org.pageseeder.bastille.doc;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import org.pageseeder.bastille.util.Errors;
import org.pageseeder.berlioz.BerliozException;
import org.pageseeder.berlioz.content.Cacheable;
import org.pageseeder.berlioz.content.ContentGenerator;
import org.pageseeder.berlioz.content.ContentRequest;
import org.pageseeder.berlioz.content.ContentStatus;
import org.pageseeder.berlioz.content.Environment;
import org.pageseeder.cobble.CobbleException;
import org.pageseeder.cobble.XMLGenerator;
import org.pageseeder.xmlwriter.XMLWriter;

/**
 * Returns the XSLT documentation using the Cobble format
 *
 * @author Christophe Lauret
 *
 */
public final class GetCodeDocumentation implements ContentGenerator, Cacheable {

  @Override
  public String getETag(ContentRequest req) {
    return null;
  }

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {

    String path = req.getParameter("path");
    if (path == null) {
      Errors.noParameter(req, xml, "path");
      return;
    }

    Environment env = req.getEnvironment();
    File code = env.getPrivateFile(path);

    if (!XMLGenerator.isSupported(path) || !code.exists()) {
      Errors.invalidParameter(req, xml, "path");
      return;
    }

    // Generate the document
    XMLGenerator docgen = new XMLGenerator(code);
    try {
      StringWriter w = new StringWriter();
      docgen.generate(w);
      xml.writeXML(w.toString());
    } catch (CobbleException ex) {
      Errors.error(req, xml, "server", ex.getMessage(), ContentStatus.INTERNAL_SERVER_ERROR);
    }

  }

}
