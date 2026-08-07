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

import java.util.Comparator;
import java.util.List;

import org.jspecify.annotations.Nullable;

import org.pageseeder.berlioz.Beta;
import org.pageseeder.berlioz.content.Request;
import org.pageseeder.berlioz.content.Response;
import org.pageseeder.berlioz.content.XmlGenerator;
import org.pageseeder.berlioz.util.ISO8601;
import org.pageseeder.berlioz.xml.XmlWriter;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

/**
 * Display the elements in the cache.
 *
 * @author Christophe Lauret
 * @version 0.13.0
 */
@Beta
public final class GetCacheElements implements XmlGenerator {

  /** Default number of elements per page. */
  private static final int DEFAULT_PAGE_SIZE = 100;

  /** Maximum number of elements per page. */
  private static final int MAX_PAGE_SIZE = 1000;

  @Override
  public Response generate(Request req, XmlWriter xml) {
    String name = req.parameter("name").asString().required();
    int page = req.parameter("page").asInt().clamp(1, Integer.MAX_VALUE).defaultValue(1);
    int pageSize = req.parameter("pagesize").asInt().clamp(1, MAX_PAGE_SIZE).defaultValue(DEFAULT_PAGE_SIZE);

    // Identify the cache
    CacheManager manager = CacheManager.getInstance();
    Ehcache cache = manager.getEhcache(name);
    toXml(cache, page, pageSize, xml);

    return Response.ok();
  }

  /**
   * Returns detailed information about the cache.
   *
   * @param cache    The cache
   * @param page     The page of elements to return (1-based)
   * @param pageSize The number of elements per page
   * @param xml      The XML Writer
   */
  private static void toXml(@Nullable Ehcache cache, int page, int pageSize, XmlWriter xml) {
    if (cache == null) return;
    xml.openElement("cache", true);
    xml.attribute("name", cache.getName());
    xml.attribute("guid", cache.getGuid());
    xml.attribute("status", cache.getStatus().toString());
    xml.attribute("disabled", Boolean.toString(cache.isDisabled()));

    // Keys sorted for a stable order across pages
    List<?> keys = cache.getKeys();
    keys.sort(Comparator.comparing(Object::toString));
    int total = keys.size();
    int from = (int) Math.min((long) (page - 1) * pageSize, total);
    int to = (int) Math.min((long) from + pageSize, total);

    xml.openElement("keys");
    xml.attribute("count", (long)to - from);
    xml.attribute("total", total);
    xml.attribute("page", page);
    xml.attribute("pagesize", pageSize);
    for (Object key : keys.subList(from, to)) {
      xml.openElement("element");
      xml.attribute("key", key.toString());
      Element element = cache.getQuiet(key);
      if (element != null) {
        xml.attribute("creation-time", ISO8601.DATETIME.format(element.getCreationTime()));
        xml.attribute("expiration-time", ISO8601.DATETIME.format(element.getExpirationTime()));
        xml.attribute("last-access-time", ISO8601.DATETIME.format(element.getLastAccessTime()));
        xml.attribute("last-update-time", ISO8601.DATETIME.format(element.getLastUpdateTime()));
        xml.attribute("hit-count", Long.toString(element.getHitCount()));
        xml.attribute("version", Long.toString(element.getVersion()));
        xml.attribute("time-to-idle", element.getTimeToIdle());
        xml.attribute("time-to-live", element.getTimeToLive());
        xml.attribute("eternal", Boolean.toString(element.isEternal()));
        xml.attribute("expired", Boolean.toString(element.isExpired()));
      }
      xml.closeElement();
    }
    xml.closeElement();

    xml.closeElement();
  }


}
