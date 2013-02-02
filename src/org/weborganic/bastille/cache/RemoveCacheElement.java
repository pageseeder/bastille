/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.weborganic.bastille.cache;

import java.io.IOException;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;

import org.weborganic.bastille.util.Errors;
import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.Beta;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentRequest;
import org.weborganic.berlioz.content.ContentStatus;

import com.topologi.diffx.xml.XMLWriter;

/**
 * Removes an entry from the cache content.
 *
 * @author Christophe Lauret
 * @version Bastille 0.8.4 - 1 February 2013
 */
@Beta
public final class RemoveCacheElement implements ContentGenerator {

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {

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
