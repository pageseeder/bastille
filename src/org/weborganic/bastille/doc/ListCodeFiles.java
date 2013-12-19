package org.weborganic.bastille.doc;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.content.Cacheable;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentRequest;
import org.weborganic.berlioz.content.Environment;
import org.weborganic.berlioz.util.FileUtils;

import com.topologi.diffx.xml.XMLWriter;

/**
 * Returns the XSLT documentation using the Cobble format
 *
 * @author Christophe Lauret
 *
 */
public final class ListCodeFiles implements ContentGenerator, Cacheable {

  /**
   * Filters XML files only.
   */
  private static final FileFilter DIRECTORIES_OR_XSLT_FILES = new FileFilter() {
    @Override
    public boolean accept(File file) {
      return file.isDirectory() || file.getName().endsWith(".xsl");
    }
  };

  @Override
  public String getETag(ContentRequest req) {
    return null;
  }

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {

    Environment env = req.getEnvironment();
    File root = env.getPrivateFolder();
    File xslt = env.getPrivateFile("xslt");

    // XSLT documentation first
    SimpleDateFormat ISO8601Local = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    toXML(xslt, xml, root, ISO8601Local);

  }

  /**
   * Serialise the specified file as XML.
   *
   * @param f   the file.
   * @param xml the xml where the file information goes to.
   * @throws IOException Should any IO occurs while retrieving the info or writing XML.
   */
  private void toXML(File f, XMLWriter xml, File ancestor, DateFormat iso) throws IOException {
    xml.openElement("file");
    xml.attribute("name", f.getName());
    xml.attribute("path", FileUtils.path(ancestor, f));
    if (f.exists()) {

      if (f.isDirectory()) {
        xml.attribute("type", "folder");
        for (File x : f.listFiles(DIRECTORIES_OR_XSLT_FILES)) {
          toXML(x, xml, ancestor, iso);
        }

      } else {
        xml.attribute("type", "file");
        xml.attribute("content-type", getMediaType(f));
        xml.attribute("media-type", getMediaType(f));
        xml.attribute("length", Long.toString(f.length()));
        xml.attribute("modified", iso.format(f.lastModified()));
      }

    } else {
      xml.attribute("status", "not-found");
    }
    xml.closeElement();
  }

  /**
   * Returns the MIME type of the given file based on the global MIME properties
   *
   * @param f The file
   * @return the corresponding MIME type
   */
  private String getMediaType(File f) {
    String mime = FileUtils.getMediaType(f);
    return (mime != null)? mime : "application/octet-stream";
  }

}
