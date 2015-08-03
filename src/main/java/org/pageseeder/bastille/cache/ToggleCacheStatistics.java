/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.pageseeder.bastille.cache;

import java.io.IOException;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;

import org.pageseeder.berlioz.BerliozException;
import org.pageseeder.berlioz.content.ContentGenerator;
import org.pageseeder.berlioz.content.ContentRequest;

import org.pageseeder.xmlwriter.XMLWriter;

/**
 * Toggles cache statistics On and Off.
 *
 * @author Christophe Lauret
 * @version Bastille 0.8.4 - 2 February 2013
 */
public final class ToggleCacheStatistics implements ContentGenerator {

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {

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
