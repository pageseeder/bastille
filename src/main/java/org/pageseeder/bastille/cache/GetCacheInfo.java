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

import org.jspecify.annotations.Nullable;

import org.pageseeder.bastille.cache.util.SizeEstimator;
import org.pageseeder.berlioz.Beta;
import org.pageseeder.berlioz.content.Request;
import org.pageseeder.berlioz.content.Response;
import org.pageseeder.berlioz.content.XmlGenerator;
import org.pageseeder.berlioz.xml.XmlWriter;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Status;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

/**
 * Display information about the cache.
 *
 * @author Christophe Lauret
 * @version 0.6.7
 */
@Beta
public final class GetCacheInfo implements XmlGenerator {

  @Override
  public Response generate(Request req, XmlWriter xml) {
    String name = req.parameter("name").asString().required();

    // Identify the cache
    CacheManager manager = CacheManager.getInstance();
    Ehcache cache = manager.getEhcache(name);
    toXml(cache, xml);

    return Response.ok();
  }

  /**
   * Returns detailed information about the cache.
   *
   * @param cache The cache
   * @param xml   The XML Writer
   */
  private static void toXml(@Nullable Ehcache cache, XmlWriter xml) {
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
      xml.attribute("memory-store-size",    Long.toString(cache.getStatistics().getLocalHeapSize()));
      xml.attribute("disk-store-size",      Long.toString(cache.getStatistics().getLocalDiskSize()));
      xml.attribute("in-memory-size",       Long.toString(estimator.getInMemorySize(cache)));
      xml.attribute("on-disk-size",         Long.toString(estimator.getOnDiskSize(cache)));
      xml.closeElement();
    }

    // Configuration
    toXml(cache.getCacheConfiguration(), xml);

    // Statistics
    if (status == Status.STATUS_ALIVE) {
      xml.openElement("statistics");
      xml.attribute("enabled", "false");
      xml.closeElement();
    }

    xml.closeElement();
  }

  /**
   * Display information about the cache configuration
   *
   * @param config The cache configuration
   * @param xml    The XML Writer.
   */
  private static void toXml(CacheConfiguration config, XmlWriter xml) {
    // configuration
    xml.openElement("configuration");
    xml.attribute("disk-spool-buffer-size-mb", config.getDiskSpoolBufferSizeMB());
    xml.attribute("expiry-thread-interval-seconds", Long.toString(config.getDiskExpiryThreadIntervalSeconds()));
    xml.attribute("max-bytes-local-disk", Long.toString(config.getMaxBytesLocalDisk()));
    xml.attribute("max-bytes-local-disk-percentage-set", Boolean.toString(config.isMaxBytesLocalDiskPercentageSet()));
    xml.attribute("max-bytes-local-heap", Long.toString(config.getMaxBytesLocalHeap()));
    xml.attribute("max-bytes-local-heap-percentage-set", Boolean.toString(config.isMaxBytesLocalHeapPercentageSet()));
    PersistenceConfiguration pc = config.getPersistenceConfiguration();
    xml.attribute("disk-persistent",   Boolean.toString(pc != null && pc.getStrategy() == PersistenceConfiguration.Strategy.LOCALRESTARTABLE));
    xml.attribute("eternal",           Boolean.toString(config.isEternal()));
    xml.attribute("clear-on-flush",    Boolean.toString(config.isClearOnFlush()));
    xml.attribute("copy-on-read",      Boolean.toString(config.isCopyOnRead()));
    xml.attribute("copy-on-write",     Boolean.toString(config.isCopyOnWrite()));
    xml.attribute("count-based-tuned", Boolean.toString(config.isCountBasedTuned()));
    xml.attribute("overflow-to-disk",  Boolean.toString(pc != null && pc.getStrategy() == PersistenceConfiguration.Strategy.LOCALTEMPSWAP));
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
}
