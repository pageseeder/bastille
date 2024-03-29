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
package org.pageseeder.bastille.cache.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.ehcache.Ehcache;

/**
 * A simple class to estimate the size of a cache based on previously calculated sized
 * and the number of elements.
 *
 * @author Christophe Lauret
 * @version Bastille 0.8.3
 */
public final class SizeEstimator {

  /** Singleton instance */
  private static final SizeEstimator SINGLETON = new SizeEstimator();

  /**
   * A new sample is taken when the number of elements in the cache exceeds the number of elements in the sample
   * times this factor.
   */
  private static final float RESAMPLE_FACTOR = 1.5f;

  /**
   * Samples for "in memory" sizes mapped to the cache names.
   */
  private final Map<String, Sample> inMemorySamples = new ConcurrentHashMap<>();

  /**
   * Samples for "in memory" sizes mapped to the cache names.
   */
  private final Map<String, Sample> onDiskSamples = new ConcurrentHashMap<>();

  /**
   * Request singleton instead.
   */
  private SizeEstimator() {
  }

  /**
   * @return the single instance.
   */
  public static SizeEstimator singleton() {
    return SINGLETON;
  }

  /**
   * Check whether a new "in memory" size sample need to be re-calculated.
   *
   * @param cache The cache.
   * @return <code>true</code> if a new sample was recalculated;
   *         <code>false</code> otherwise.
   */
  public boolean checkInMemorySample(Ehcache cache) {
    Sample sample = this.inMemorySamples.get(cache.getName());
    int elements = cache.getSize();
    if (sample == null || sample.elements() * RESAMPLE_FACTOR < elements || sample._bytesize == 0) {
      long bytesize = elements > 0? cache.calculateInMemorySize() : 0;
      sample = new Sample(elements, bytesize);
      this.inMemorySamples.put(cache.getName(), sample);
      return elements > 0;
    }
    return false;
  }

  /**
   * Check whether a new "on disk" size sample need to be re-calculated.
   *
   * @param cache The cache.
   * @return <code>true</code> if a new sample was recalculated;
   *         <code>false</code> otherwise.
   */
  public boolean checkOnDiskSample(Ehcache cache) {
    Sample sample = this.onDiskSamples.get(cache.getName());
    int elements = cache.getSize();
    if (sample == null || sample.elements()*2 < elements) {
      long bytesize = elements > 0? cache.calculateOnDiskSize() : 0;
      sample = new Sample(elements, bytesize);
      this.onDiskSamples.put(cache.getName(), sample);
      return elements > 0;
    }
    return false;
  }

  /**
   * Returns the "in memory" size from the cache.
   *
   * <p>This method does not indicate whether the size was calculated or estimated from a previous sample.
   *
   * @param cache The cache
   * @return return the actual value or -1;
   */
  public long getInMemorySize(Ehcache cache) {
    synchronized (this.inMemorySamples) {
      checkInMemorySample(cache);
      return estimateInMemorySize(cache.getName(), cache.getSize());
    }
  }

  /**
   * Returns the "on disk" size from the cache.
   *
   * <p>This method does not indicate whether the size was calculated or estimated from a previous sample.
   *
   * @param cache The cache
   * @return return the actual value or -1;
   */
  public long getOnDiskSize(Ehcache cache) {
    synchronized (this.onDiskSamples) {
      checkOnDiskSample(cache);
      return estimateOnDiskSize(cache.getName(), cache.getSize());
    }
  }

  /**
   * Estimate the "in memory" size based on previous samples.
   *
   * @param name The name of the cache
   * @param elements The number of elements
   * @return return the actual value or -1;
   */
  public long estimateInMemorySize(String name, int elements) {
    return estimate(this.inMemorySamples, name, elements);
  }

  /**
   * Estimate the "on disk" size based on previous samples.
   *
   * @param name The name of the cache
   * @param elements The number of elements
   * @return return the actual value or -1;
   */
  public long estimateOnDiskSize(String name, int elements) {
    return estimate(this.onDiskSamples, name, elements);
  }

  /**
   * Returns an estimates from a previous sample.
   *
   * @param samples
   * @param name
   * @param elements
   * @return
   */
  private static long estimate(Map<String, Sample> samples, String name, int elements) {
    Sample sample = samples.get(name);
    long estimate = -1;
    if (sample != null) {
      estimate = sample.estimate(elements);
    }
    return estimate;
  }

  /**
   * A byte size sample.
   *
   * <p>This class is immutable, but new samples can be created,
   *
   * @author Christophe Lauret
   * @version 10 March 2013
   */
  private static class Sample {

    /**
     * Number of elements in the sample.
     */
    private final int _elements;

    /**
     * The size in bytes for that number of elements.
     */
    private final long _bytesize;

    /**
     * @param elements Number of elements
     * @param bytesize The size in bytes
     */
    public Sample(int elements, long bytesize) {
      this._elements = elements;
      this._bytesize = bytesize;
    }

    /**
     * Returns an estimate for the specified number of elements
     *
     * @param elements The number of elements
     * @return the estimated size
     */
    public long estimate(int elements) {
      if (this._elements == elements) return this._bytesize;
      if (this._elements == 0) return 0;
      // XXX: May be larger than MAX_LONG?
      return (this._bytesize * elements) / this._elements;
    }

    /**
     * @return the number of elements in the sample
     */
    public int elements() {
      return this._elements;
    }
  }
}
