/*
 * This file is part of the Bastille library.
 *
 * Available under a commercial licence, contact Weborganic.
 *
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package com.weborganic.bastille.psml;

import java.io.IOException;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class to get the PSML from the cache.
 *
 * @author Christophe Lauret
 * @version 6 October 2012
 */
public final class PSMLCache {

  /**
   * Utility class.
   */
  private PSMLCache() {
  }

  /**
   * Logger for debugging
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(PSMLCache.class);

  /**
   * Name of the cache.
   */
  public static final String CACHE_NAME = "PSML";

  /**
   * Reuse the same cache manager to avoid I/O problems (configuration seems to be parsed for each getInstance).
   */
  private static volatile CacheManager manager = null;

  /**
   * The cache containing all the PSML entries.
   */
  private static volatile Ehcache cache = null;

  /**
   * Return the cached content of the specified PSML file.
   *
   * @param psml The PSML file
   * @return The PSML content.
   *
   * @throws IOException If thrown while loading the file.
   */
  public static String getContent(PSMLFile psml) throws IOException {
    // Initialise if required.
    if (cache == null) { init(); }
    // Attempt to grab the content
    Element cached = cache.get(psml.path());
    String data = null;
    if (cached == null || cached.getLastUpdateTime() < psml.file().lastModified()) {
      data = PSMLConfig.load(psml);
      cache.put(new Element(psml.path(), data));
    } else {
      data = (String)cached.getObjectValue();
    }
    return data;
  }

  /**
   * Initialises the cache for PSML files.
   */
  private static synchronized void init() {
    // Create cache
    if (manager == null) {
      LOGGER.info("Initialising cache for PSML data");
      manager = CacheManager.getInstance();
      // May have been created with another service.
      cache = manager.getEhcache(CACHE_NAME);
      if (cache == null) {
        LOGGER.warn("No cache exists for PSML data! Create a new cache entry for {} in your cache config!", CACHE_NAME);
        manager.addCache(CACHE_NAME);
        LOGGER.info("Created new cache named {} to store PSML data", CACHE_NAME);
        cache = manager.getEhcache(CACHE_NAME);
      }
    }
  }
}
