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
package org.pageseeder.bastille.cache;

import java.io.IOException;

import org.pageseeder.berlioz.content.ContentGenerator;
import org.pageseeder.berlioz.content.ContentRequest;
import org.pageseeder.xmlwriter.XMLWriter;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;

/**
 * Toggles cache statistics On and Off.
 *
 * @author Christophe Lauret
 * @version Bastille 0.8.4
 */
public final class ToggleCacheStatistics implements ContentGenerator {

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws IOException {

    // Grab the cache name
    String name = req.getParameter("name", null);
    boolean enable = "true".equals(req.getParameter("enable"));

    // Get the cache manager
    CacheManager manager = CacheManager.getInstance();
    xml.openElement("cache-statistics", true);

    if (!name.isEmpty()) {

      // Clear a specific cache
      Ehcache cache = manager.getEhcache(name);
      toXML(cache, xml);

    } else {

      // Iterate over the caches in EH Cache
      String[] names = manager.getCacheNames();
      for (String n : names) {
        Ehcache cache = manager.getEhcache(n);
        toXML(cache, xml);
      }

    }

    xml.closeElement();
  }

  /**
   * Returns basic information about the cache.
   *
   * @param cache  The cache
   * @param xml    The XML Writer
   *
   * @throws IOException If an error occurs while writing the XML
   */
  private static void toXML(Ehcache cache, XMLWriter xml) throws IOException {
    if (cache == null) return;
    xml.openElement("cache", true);
    xml.attribute("name", cache.getName());
    xml.attribute("guid", cache.getGuid());
    xml.openElement("statistics", true);
    xml.attribute("enabled", "false");
    xml.closeElement();
    xml.closeElement();
  }

}
