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

import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.Beta;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentRequest;

import com.topologi.diffx.xml.XMLWriter;

/**
 * Clears the cache content.
 *
 * <p>If a cache name is specified, then only this cache is cleared.
 * <p>Otherwise, all caches are cleared.
 *
 * @author Christophe Lauret
 * @version 24 November 2011
 */
@Beta
public final class ClearCache implements ContentGenerator {

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {

    // Grab the cache name
    String name = req.getParameter("name", "");

    // Get the cache manager
    CacheManager manager = CacheManager.getInstance();
    xml.openElement("clear-cache", true);

    if (!name.isEmpty()) {

      // Clear a specific cache
      Ehcache cache = manager.getEhcache(name);
      cache.removeAll();
      toXML(cache, xml);

    } else {

      // clear all cache
      manager.clearAll();
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
   * @param cache The cache
   * @param xml   The XML Writer
   *
   * @throws IOException If an error occurs while writing the XML
   */
  private static void toXML(Ehcache cache, XMLWriter xml) throws IOException {
    if (cache == null) return;
    xml.openElement("cache", true);
    xml.attribute("name", cache.getName());
    xml.attribute("guid", cache.getGuid());
    xml.closeElement();
  }

}
