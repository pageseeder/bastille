/*
 * This file is part of the Bastille library.
 *
 * Available under a commercial licence, contact Weborganic.
 *
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package org.weborganic.bastille.cache;

import java.io.IOException;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Statistics;
import net.sf.ehcache.Status;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import org.weborganic.berlioz.BerliozException;
import org.weborganic.berlioz.Beta;
import org.weborganic.berlioz.content.ContentGenerator;
import org.weborganic.berlioz.content.ContentRequest;
import org.weborganic.berlioz.content.ContentStatus;

import com.topologi.diffx.xml.XMLWriter;

/**
 * Display information about the cache.
 *
 * @author Christophe Lauret
 * @version 24 November 2011
 */
@Beta
public final class GetCacheInfo implements ContentGenerator {

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {
    String name = req.getParameter("name");
    if (name == null || "".equals(name)) {
      req.setStatus(ContentStatus.NOT_FOUND);
      return;
    }

    // Identify the cache
    CacheManager manager = CacheManager.getInstance();
    Ehcache cache = manager.getEhcache(name);
    toXML(cache, xml);

  }

  /**
   * Returns detailed information about the cache.
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

    // Configuration
    toXML(cache.getCacheConfiguration(), xml);

    // Statistics
    if (status == Status.STATUS_ALIVE) {
      boolean hasStatistics = cache.isStatisticsEnabled();
      if (hasStatistics) {
        toXML(cache.getStatistics(), xml);
      } else {
        xml.openElement("statistics");
        xml.attribute("enabled", "false");
        xml.closeElement();
      }
    }

    xml.closeElement();
  }

  /**
   * Display information about the cache configuration
   *
   * @param config The cache configuration
   * @param xml    The XML Writer.
   *
   * @throws IOException If an error occurs while writing the XML
   */
  private static void toXML(CacheConfiguration config, XMLWriter xml) throws IOException {
    // configuration
    xml.openElement("configuration");
    xml.attribute("disk-spool-buffer-size-mb", config.getDiskSpoolBufferSizeMB());
    if (config.getDiskStorePath() != null)
      xml.attribute("store-path", config.getDiskStorePath());
    xml.attribute("expiry-thread-interval-seconds", Long.toString(config.getDiskExpiryThreadIntervalSeconds()));
    xml.attribute("max-bytes-local-disk", Long.toString(config.getMaxBytesLocalDisk()));
    xml.attribute("max-bytes-local-disk-percentage-set", Boolean.toString(config.isMaxBytesLocalDiskPercentageSet()));
    xml.attribute("max-bytes-local-heap", Long.toString(config.getMaxBytesLocalHeap()));
    xml.attribute("max-bytes-local-heap-percentage-set", Boolean.toString(config.isMaxBytesLocalHeapPercentageSet()));
    xml.attribute("disk-persistent",   Boolean.toString(config.isDiskPersistent()));
    xml.attribute("eternal",           Boolean.toString(config.isEternal()));
    xml.attribute("clear-on-flush",    Boolean.toString(config.isClearOnFlush()));
    xml.attribute("copy-on-read",      Boolean.toString(config.isCopyOnRead()));
    xml.attribute("copy-on-write",     Boolean.toString(config.isCopyOnWrite()));
    xml.attribute("count-based-tuned", Boolean.toString(config.isCountBasedTuned()));
    xml.attribute("overflow-to-disk",  Boolean.toString(config.isOverflowToDisk()));
    if (config.isMaxBytesLocalDiskPercentageSet()) {
      xml.attribute("max-bytes-local-disk-percentage",  config.getMaxBytesLocalDiskPercentage());
    }
    if (config.isMaxBytesLocalHeapPercentageSet()) {
      xml.attribute("max-bytes-local-disk-percentage",  config.getMaxBytesLocalHeapPercentage());
    }
    MemoryStoreEvictionPolicy policy = config.getMemoryStoreEvictionPolicy();
    xml.attribute("memory-store-eviction-policy", policy.toString());
    xml.closeElement();
  }

  /**
   * Display information about the cache configuration
   *
   * @param statistics Statistics about the cache
   * @param xml        The XML Writer.
   *
   * @throws IOException If an error occurs while writing the XML
   */
  private static void toXML(Statistics statistics, XMLWriter xml) throws IOException {
    // configuration
    xml.openElement("statistics");
    xml.attribute("enabled", "true");
    xml.attribute("accuracy", statistics.getStatisticsAccuracyDescription());
    xml.attribute("cache-hits",       Long.toString(statistics.getCacheHits()));
    xml.attribute("cache-misses",     Long.toString(statistics.getCacheMisses()));
    xml.attribute("eviction-count",   Long.toString(statistics.getEvictionCount()));
    xml.attribute("in-memory-hits",   Long.toString(statistics.getInMemoryHits()));
    xml.attribute("in-memory-misses", Long.toString(statistics.getInMemoryMisses()));
    xml.attribute("on-disk-hits",     Long.toString(statistics.getOnDiskHits()));
    xml.attribute("on-disk-misses",   Long.toString(statistics.getOnDiskMisses()));
    xml.closeElement();
  }

}
