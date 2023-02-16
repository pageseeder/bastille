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
package org.pageseeder.bastille.xml;

import java.io.File;
import java.io.IOException;

import org.pageseeder.berlioz.content.Cacheable;
import org.pageseeder.berlioz.content.ContentGenerator;
import org.pageseeder.berlioz.content.ContentRequest;
import org.pageseeder.berlioz.content.Environment;
import org.pageseeder.berlioz.util.MD5;
import org.pageseeder.xmlwriter.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>This generator returns the breadcrumb status based on Berlioz path.<p>
 *
 * <h3>Returned XML</h3>
 * <pre> {@code
 *  <breadcrumbs>
 *    <breadcrumb name="string" path="string" exist="true|false"/>
 *  </breadcrumbs>
 * } </pre>
 *
 * <h3>The example cases</h3>
 * <p>if Berlioz path is 'Ping/pong', it will look at the status of 'Ping.xml', 'Ping' folder, 'Ping/pong.xml' and 'Ping/pong' folder</p>
 * <p/><p/>
 * <p>Case 1:</p>
 * <p>Ping.xml is not exist. Ping folder exist</p>
 * <p>The return XML will be</p>
 *
 * <pre> {@code
 *  <breadcrumbs>
 *    <breadcrumb name="ping" path="/ping" exist="true"/>
 *  </breadcrumbs>
 * } </pre>
 *
 * <p/><p/>
 * <p>Case 2:</p>
 * <p>Ping.xml is not exist. Ping folder is not exist</p>
 * <p>The return XML will be</p>
 *
 * <pre> {@code
 *  <breadcrumbs>
 *    <breadcrumb name="ping" path="/ping" exist="false"/>
 *  </breadcrumbs>
 * } </pre>
 *
 * <p/><p/>
 * <p>Case 3:</p>
 * <p>Ping.xml is exist. Ping folder not exist</p>
 * <p>The return XML will be</p>
 *
 * <pre> {@code
 *  <breadcrumbs>
 *    <breadcrumb name="ping" path="/ping" exist="true"/>
 *  </breadcrumbs>
 * } </pre>
 *
 * <p/><p/>
 * <p>Case 4:</p>
 * <p>Ping.xml is exist. Ping exist</p>
 * <p>The return XML will be</p>
 *
 * <pre> {@code
 *  <breadcrumbs>
 *    <breadcrumb name="ping" path="/ping" exist="true"/>
 *  </breadcrumbs>
 * } </pre>
 *
 * @deprecated Will be removed in 0.12
 *
 * @author Ciber Cai
 * @version 14 July 2011
 */
@Deprecated
public final class GetBreadCrumbFromBerliozPath implements ContentGenerator, Cacheable {

  /**
   * Logger for debugging
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(GetBreadCrumbFromBerliozPath.class);

  @Override
  public String getETag(ContentRequest req) {
    Environment env = req.getEnvironment();
    File file = env.getPublicFile(req.getBerliozPath());
    if (file != null && file.exists()) return MD5.hash(req.getBerliozPath() + "_" + file.length() + "x" + file.lastModified());
    else return "not-exist";
  }

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws IOException {
    LOGGER.debug("Berlioz path {}", req.getBerliozPath());

    File rootfolder = XMLConfiguration.getXMLRootFolder(req);
    String path = req.getBerliozPath();
    StringBuilder subpath = new StringBuilder();

    xml.openElement("breadcrumbs");

    for (String p : path.split("/")) {
      if (!p.isEmpty()) {
        subpath.append('/').append(p);
        String current = subpath.toString();
        xml.openElement("breadcrumb");
        xml.attribute("name", p);
        xml.attribute("path", current);
        xml.attribute("exist", String.valueOf(fileisExist(rootfolder, current)));
        xml.closeElement();
      }
    }
    xml.closeElement();
  }

  /**
   * Return the status of request path.
   * @param rootfolder defines the rootfolder.
   * @param path defines the relative path from <code>rootfolder</code>.
   * @return true|false defines the status of the request.
   */
  private boolean fileisExist(File rootfolder, String path) {
    File file = new File(rootfolder, path + ".xml");
    File folder = new File(rootfolder, path);

    // request file exists
    if (file.exists()) return true;
    else {
      // request file is a directory
      return folder.isDirectory();
    }
  }
}
