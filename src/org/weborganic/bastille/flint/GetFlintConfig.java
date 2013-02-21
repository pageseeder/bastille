package org.weborganic.bastille.flint;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.weborganic.bastille.flint.config.FlintConfig;
import org.weborganic.bastille.flint.config.IFlintConfig;
import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.Beta;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentRequest;

import com.topologi.diffx.xml.XMLWriter;

@Beta
public final class GetFlintConfig implements ContentGenerator {

  /**
   * To list only folders
   */
  private static final FileFilter FOLDERS_ONLY = new FileFilter() {
    @Override
    public boolean accept(File d) {
      return d.isDirectory();
    }
  };

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {
    IFlintConfig config = FlintConfig.get();
    File directory = FlintConfig.directory();
    //config
    xml.openElement("flint-config");
    // TODO: not the best way to display where the index is located!
    xml.attribute("directory", directory.getName().equals("index")? "index" : directory.getName());
    xml.attribute("multiple", Boolean.toString(config.hasMultiple()));
    xml.attribute("class", config.getClass().getName());
    if (config.hasMultiple()) {
      if (directory.exists() && directory.isDirectory()) {
        File[] subdirs = directory.listFiles(FOLDERS_ONLY);
        for (File f : subdirs) {
          xml.openElement("index");
          xml.attribute("name", f.getName());
          xml.closeElement();
        }
      }
    }
    xml.closeElement();
  }

}
