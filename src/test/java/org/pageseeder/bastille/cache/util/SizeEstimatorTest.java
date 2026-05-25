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

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SizeEstimatorTest {

  @Test
  void singleton_returnsNonNull() {
    assertNotNull(SizeEstimator.singleton());
  }

  @Test
  void singleton_returnsSameInstance() {
    assertSame(SizeEstimator.singleton(), SizeEstimator.singleton());
  }

  @Test
  void estimateInMemorySize_noSample_returnsMinusOne() {
    SizeEstimator estimator = SizeEstimator.singleton();
    assertEquals(-1L, estimator.estimateInMemorySize("cache-not-sampled-yet-" + System.nanoTime(), 10));
  }

  @Test
  void estimateOnDiskSize_noSample_returnsMinusOne() {
    SizeEstimator estimator = SizeEstimator.singleton();
    assertEquals(-1L, estimator.estimateOnDiskSize("cache-not-sampled-yet-" + System.nanoTime(), 10));
  }

  @Test
  void estimateInMemorySize_zeroElements_returnsZero() throws Exception {
    SizeEstimator estimator = SizeEstimator.singleton();
    String cacheName = "test-zero-elements-" + System.nanoTime();
    injectSample(estimator, "inMemorySamples", cacheName, 0, 0L);
    assertEquals(0L, estimator.estimateInMemorySize(cacheName, 0));
  }

  @Test
  void estimateInMemorySize_exactSampleElements() throws Exception {
    SizeEstimator estimator = SizeEstimator.singleton();
    String cacheName = "test-exact-" + System.nanoTime();
    injectSample(estimator, "inMemorySamples", cacheName, 100, 5000L);
    assertEquals(5000L, estimator.estimateInMemorySize(cacheName, 100));
  }

  @Test
  void estimateInMemorySize_interpolatesFromSample() throws Exception {
    SizeEstimator estimator = SizeEstimator.singleton();
    String cacheName = "test-interp-" + System.nanoTime();
    injectSample(estimator, "inMemorySamples", cacheName, 100, 10000L);
    assertEquals(5000L, estimator.estimateInMemorySize(cacheName, 50));
  }

  @Test
  void estimateOnDiskSize_exactSampleElements() throws Exception {
    SizeEstimator estimator = SizeEstimator.singleton();
    String cacheName = "test-disk-exact-" + System.nanoTime();
    injectSample(estimator, "onDiskSamples", cacheName, 50, 2500L);
    assertEquals(2500L, estimator.estimateOnDiskSize(cacheName, 50));
  }

  /**
   * Injects a Sample into the named private map field using reflection, bypassing the Ehcache dependency.
   */
  @SuppressWarnings("unchecked")
  private static void injectSample(SizeEstimator estimator, String fieldName, String cacheName, int elements, long bytesize) throws Exception {
    Field mapField = SizeEstimator.class.getDeclaredField(fieldName);
    mapField.setAccessible(true);
    Map<String, Object> map = (Map<String, Object>) mapField.get(estimator);

    // Instantiate the private Sample class via reflection
    Class<?> sampleClass = Class.forName(SizeEstimator.class.getName() + "$Sample");
    java.lang.reflect.Constructor<?> ctor = sampleClass.getDeclaredConstructor(int.class, long.class);
    ctor.setAccessible(true);
    Object sample = ctor.newInstance(elements, bytesize);
    map.put(cacheName, sample);
  }
}
