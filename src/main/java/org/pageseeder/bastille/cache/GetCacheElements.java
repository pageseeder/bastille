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

import org.pageseeder.bastille.util.Errors;
import org.pageseeder.berlioz.BerliozException;
import org.pageseeder.berlioz.Beta;
import org.pageseeder.berlioz.content.ContentGenerator;
import org.pageseeder.berlioz.content.ContentRequest;
import org.pageseeder.berlioz.util.ISO8601;
import org.pageseeder.xmlwriter.XMLWriter;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

/**
 * Display the elements in the cache.
 *
 * @author Christophe Lauret
 * @version 24 November 2011
 */
@Beta
public final class GetCacheElements implements ContentGenerator {

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {
    String name = req.getParameter("name");
    if (name == null || "".equals(name)) {
      Errors.noParameter(req, xml, "name");
      return;
    }
    int page = req.getIntParameter("page", 1);
    int perpage = req.getIntParameter("pagesize", 1000);

    // TODO

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
    xml.openElement("cache", true);
    xml.attribute("name", cache.getName());
    xml.attribute("guid", cache.getGuid());
    xml.attribute("status", cache.getStatus().toString());
    xml.attribute("disabled", Boolean.toString(cache.isDisabled()));

    // Keys
    List<?> keys = cache.getKeys();
    xml.openElement("keys");
    xml.attribute("count", keys.size());
    for (Object key : keys) {
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
