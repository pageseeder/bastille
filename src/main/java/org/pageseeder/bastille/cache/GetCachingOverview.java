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
import java.util.List;

import org.pageseeder.bastille.cache.util.SizeEstimator;
import org.pageseeder.berlioz.Beta;
import org.pageseeder.berlioz.content.ContentGenerator;
import org.pageseeder.berlioz.content.ContentRequest;
import org.pageseeder.xmlwriter.XMLWriter;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Status;

/**
 * Display a summary of the information about the cache in the application.
 *
 * @author Christophe Lauret
 * @version 24 November 2011
 */
@Beta
public final class GetCachingOverview implements ContentGenerator {

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws IOException {
    List<CacheManager> managers = CacheManager.ALL_CACHE_MANAGERS;
    for (CacheManager manager : managers) {
      xml.openElement("cache-manager", true);
      xml.attribute("name", manager.getName());

      // Iterate over the caches in EH Cache
      String[] names = manager.getCacheNames();
      for (String name : names) {
        Ehcache cache = manager.getEhcache(name);
        toXML(cache, xml);
      }

      xml.closeElement();
    }

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
    Status status = cache.getStatus();
    xml.openElement("cache", true);
    xml.attribute("name", cache.getName());
    xml.attribute("guid", cache.getGuid());
    xml.attribute("status", status.toString());
    xml.attribute("disabled", Boolean.toString(cache.isDisabled()));

    // Basic info
    if (status == Status.STATUS_ALIVE) {
      SizeEstimator estimator = SizeEstimator.singleton();
      xml.openElement("info");
      xml.attribute("size", cache.getSize());
      xml.attribute("memory-store-size",    Long.toString(cache.getMemoryStoreSize()));
      xml.attribute("disk-store-size",      cache.getDiskStoreSize());
      xml.attribute("in-memory-size",       Long.toString(estimator.getInMemorySize(cache)));
      xml.attribute("on-disk-size",         Long.toString(estimator.getOnDiskSize(cache)));
      xml.attribute("statistics-enabled",  "false");
      xml.closeElement();
    }

    xml.closeElement();
  }

}
