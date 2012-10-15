/*
 * This file is part of the Bastille library.
 *
 * Available under a commercial licence, contact Weborganic.
 *
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package com.weborganic.bastille.cache;

import java.io.IOException;
import java.util.List;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Status;

import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.Beta;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentRequest;

import com.topologi.diffx.xml.XMLWriter;

/**
 * Display a summary of the information about the cache in the application.
 *
 * @author Christophe Lauret
 * @version 24 November 2011
 */
@Beta
public final class GetCachingOverview implements ContentGenerator {

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {
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
      xml.openElement("info");
      xml.attribute("size", cache.getSize());
      xml.attribute("memory-store-size",    Long.toString(cache.getMemoryStoreSize()));
      xml.attribute("disk-store-size",      cache.getDiskStoreSize());
      xml.attribute("in-memory-size",       Long.toString(cache.calculateInMemorySize()));
      xml.attribute("off-heap-size",        Long.toString(cache.calculateOffHeapSize()));
      xml.attribute("on-disk-size",         Long.toString(cache.calculateOnDiskSize()));
      xml.attribute("average-search-time",  Long.toString(cache.getAverageSearchTime()));
      xml.attribute("searches-per-seconds", Long.toString(cache.getSearchesPerSecond()));
      xml.attribute("average-get-time",     Float.toString(cache.getAverageGetTime()));
      xml.closeElement();
    }

    xml.closeElement();
  }

}
