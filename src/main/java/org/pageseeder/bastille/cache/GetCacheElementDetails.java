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
import java.io.Serializable;
import java.lang.reflect.Field;

import org.pageseeder.bastille.util.Errors;
import org.pageseeder.berlioz.Beta;
import org.pageseeder.berlioz.content.ContentGenerator;
import org.pageseeder.berlioz.content.ContentRequest;
import org.pageseeder.berlioz.util.ISO8601;
import org.pageseeder.xmlwriter.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

/**
 * Display the details about an element in the cache.
 *
 * @author Christophe Lauret
 * @version 10 March 2013
 */
@Beta
public final class GetCacheElementDetails implements ContentGenerator {

  /** Where useful debug info goes. */
  private static final Logger LOGGER = LoggerFactory.getLogger(GetCacheElementDetails.class);

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws IOException {
    String name = req.getParameter("name");
    if (name == null || "".equals(name)) {
      Errors.noParameter(req, xml, "name");
      return;
    }

    String key = req.getParameter("key");
    if (key == null || "".equals(key)) {
      Errors.noParameter(req, xml, "key");
      return;
    }

    // Identify the cache
    CacheManager manager = CacheManager.getInstance();
    Ehcache cache = manager.getEhcache(name);
    toXML(cache, key, xml);

  }

  /**
   * Returns detailed information about the cache.
   *
   * @param cache The cache
   * @param key   The key of the cache entry
   * @param xml   The XML Writer
   *
   * @throws IOException If an error occurs while writing the XML
   */
  private static void toXML(Ehcache cache, String key, XMLWriter xml) throws IOException {
    if (cache == null) return;
    xml.openElement("cache", true);
    xml.attribute("name", cache.getName());
    xml.attribute("guid", cache.getGuid());
    xml.attribute("status", cache.getStatus().toString());
    xml.attribute("disabled", Boolean.toString(cache.isDisabled()));

    xml.openElement("element", true);
    xml.attribute("key", key);
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
      xml.attribute("serialized-size", Long.toString(element.getSerializedSize()));

      // TODO
      Object o = element.getObjectValue();
      toElementValueXML(o, xml);

    }
    xml.closeElement();

    xml.closeElement();
  }

  /**
   * Returns detailed information about the cache.
   *
   * @param o   Object to serialize as XML
   * @param xml The XML Writer
   *
   * @throws IOException If an error occurs while writing the XML
   */
  private static void toElementValueXML(Object o, XMLWriter xml) throws IOException {
    if (o == null) return;
    xml.openElement("value", true);
    xml.attribute("class", o.getClass().getName());
    xml.attribute("serializable", Boolean.toString(o instanceof Serializable));
    toElementObjectXML(o, xml);
    xml.closeElement();
  }

  /**
   * Returns detailed information about the cache.
   *
   * @param o   Object to serialize as XML
   * @param xml The XML Writer
   *
   * @throws IOException If an error occurs while writing the XML
   */
  private static void toElementObjectXML(Object o, XMLWriter xml) throws IOException {
    if (o == null) return;
    if (o instanceof String) {
      xml.writeText(o.toString());
    } else {
      Class<?> c = o.getClass();
      Field[] fields =c.getFields();
      for (Field f : fields) {
        if (f.isAccessible()) {
          Class<?> t = f.getType();
          xml.openElement("value", true);
          xml.attribute("name", f.getName());
          xml.attribute("class", t.getName());
          // TODO Handle more primitive types
          // TODO Handle arrays
          try {
            if (t == Integer.TYPE) {
              xml.writeText(Integer.toString(f.getInt(o)));
            } else if (t == Long.TYPE) {
              xml.writeText(Long.toString(f.getLong(o)));
            } else if (t == Short.TYPE) {
              xml.writeText(Short.toString(f.getShort(o)));
            } else if (t == Float.TYPE) {
              xml.writeText(Float.toString(f.getFloat(o)));
            } else if (t == Double.TYPE) {
              xml.writeText(Double.toString(f.getDouble(o)));
            } else if (t == Boolean.TYPE) {
              xml.writeText(Boolean.toString(f.getBoolean(o)));
            } else if (t == Character.TYPE) {
              xml.writeText(Character.toString(f.getChar(o)));
            } else {
              toElementObjectXML(f.get(o), xml);
            }
          } catch (IllegalArgumentException | IllegalAccessException ex) {
            LOGGER.warn("Unable to extract object value field", ex);
          }
          xml.closeElement();
        }
      }
    }
  }

}
