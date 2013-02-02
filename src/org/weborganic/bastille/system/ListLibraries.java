/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.weborganic.bastille.system;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.Beta;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentRequest;

import com.topologi.diffx.xml.XMLWriter;

/**
 * List the Java libraries in use in the application.
 *
 * @author Christophe Lauret
 * @version Bastille 0.8.4 - 1 February 2013
 */
@Beta
public final class ListLibraries implements ContentGenerator {

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {

    File lib = req.getEnvironment().getPrivateFile("lib");
    xml.openElement("libraries");
    if (lib.isDirectory()) {

      // List all the jars
      File[] jars = lib.listFiles(new FileFilter() {
        @Override
        public boolean accept(File file) {
          return file.getName().endsWith(".jar");
        }
      });

      // Iterate over each library
      for (File f : jars) {
        xml.openElement("library");
        xml.attribute("file", f.getName());

        JarFile jar = null;
        try {
          jar = new JarFile(f);
          Manifest manifest = jar.getManifest();
          Attributes attributes = manifest.getMainAttributes();
          getAll(xml, attributes);

        } finally {
          if (jar != null) jar.close();
        }

        xml.closeElement();
      }

    }
    xml.closeElement();
  }

  /**
   * Extracts all the attributes of the manifest as XML
   *
   * @param xml        The XML
   * @param attributes The attributes from the Manifest
   *
   * @throws IOException If an error occurs while writing the XML.
   */
  private static void getAll(XMLWriter xml, Attributes attributes) throws IOException {
    Map<String, List<String>> keys = new HashMap<String, List<String>>();
    for (Object o : attributes.keySet()) {
      String key = o.toString();
      int dash = key.indexOf('-');
      if (dash == -1) {
        // Just add as an attribute
        xml.attribute(key.toLowerCase(), attributes.getValue(key));
      } else {
        // Sort the composed manifest attributes
        String category = key.substring(0, dash);
        String value = key.substring(dash+1);
        List<String> values = keys.get(category);
        if (values == null) {
          values = new ArrayList<String>();
          keys.put(category, values);
        }
        values.add(value);
      }
    }

    // Elements
    for (Entry<String, List<String>> e : keys.entrySet()) {
      xml.openElement(e.getKey().toLowerCase());
      for (String key :  e.getValue()) {
        String value = attributes.getValue(e.getKey()+'-'+key);
        xml.attribute(key.toLowerCase(), value);
      }
      xml.closeElement();
    }

  }

}
