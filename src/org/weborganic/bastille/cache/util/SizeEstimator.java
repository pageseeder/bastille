/*
 * This file is part of the Bastille library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.weborganic.bastille.cache.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.ehcache.Ehcache;

/**
 * A simple class to estimate the size of a cache based on previously calculated sized
 * and the number of elements.
 *
 * @author Christophe Lauret
 * @version 10 March 2013
 */
public final class SizeEstimator {

  /** Singleton instance */
  private static final SizeEstimator SINGLETON = new SizeEstimator();

  /**
   * A new sample is taken when the number of elements in the cache exceeds the number of elements in the sample
   * times this factor.
   */
  private static final float RESAMPLE_FACTOR = 2.0f;

  /**
   * Samples for "in memory" sizes mapped to the cache names.
   */
  private final Map<String, Sample> inMemorySamples = new ConcurrentHashMap<String, Sample>();

  /**
   * Samples for "in memory" sizes mapped to the cache names.
   */
  private final Map<String, Sample> onDiskSamples = new ConcurrentHashMap<String, Sample>();

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
    if (sample == null || sample.elements() * RESAMPLE_FACTOR < elements) {
      long bytesize = cache.calculateInMemorySize();
      sample = new Sample(elements, bytesize);
      this.inMemorySamples.put(cache.getName(), sample);
      return true;
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
      long bytesize = cache.calculateOnDiskSize();
      sample = new Sample(elements, bytesize);
      this.onDiskSamples.put(cache.getName(), sample);
      return true;
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

    public Sample(int elements, long bytesize) {
      this._elements = elements;
      this._bytesize = bytesize;
    }

    /**
     * Returns an estimate for the specified number of elements
     *
     * @param elements The number of elements
     * @return
     */
    public long estimate(int elements) {
      if (this._elements == elements) return this._bytesize;
      // XXX: May be larger than MAX_LONG?
      return (this._bytesize * elements) / this._elements;
    }

    public int elements() {
      return this._elements;
    }
  }
}
