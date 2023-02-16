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

import org.pageseeder.bastille.util.Errors;
import org.pageseeder.berlioz.Beta;
import org.pageseeder.berlioz.content.ContentGenerator;
import org.pageseeder.berlioz.content.ContentRequest;
import org.pageseeder.berlioz.content.ContentStatus;
import org.pageseeder.xmlwriter.XMLWriter;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;

/**
 * Removes an entry from the cache content.
 *
 * @author Christophe Lauret
 * @version Bastille 0.8.4
 */
@Beta
public final class RemoveCacheElement implements ContentGenerator {

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws IOException {

    // Grab the cache name
    String name = req.getParameter("name", null);
    String key = req.getParameter("key", null);

    // Check required parameters
    if (name == null) {
      Errors.noParameter(req, xml, "name");
      return;
    }
    if (key == null) {
      Errors.noParameter(req, xml, "key");
      return;
    }

    // Get the cache manager
    CacheManager manager = CacheManager.getInstance();
    xml.openElement("clear-entry", true);

    // Clear a specific cache
    Ehcache cache = manager.getEhcache(name);
    if (cache != null) {

      boolean removed = cache.remove(key);

      // The cache
      xml.openElement("cache");
      xml.attribute("name", cache.getName());
      xml.attribute("guid", cache.getGuid());
      xml.closeElement();
      // The element
      xml.openElement("element");
      xml.attribute("key", key);
      xml.attribute("removed", Boolean.toString(removed));
      xml.closeElement();
    } else {
      // No cache
      xml.openElement("no-cache");
      xml.attribute("name", name);
      xml.closeElement();
      req.setStatus(ContentStatus.NOT_FOUND);
    }

    xml.closeElement();
  }

}
