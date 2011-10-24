/*
 * Copyright (C) 2011 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.common.cache;

import static com.google.common.cache.CacheTesting.checkEmpty;
import static com.google.common.cache.TestingCacheLoaders.identityLoader;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.SECONDS;

import com.google.common.base.Function;
import com.google.common.cache.CacheBuilderFactory.ExpirationSpec;
import com.google.common.cache.LocalCacheInternalMap.Strength;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.testing.EqualsTester;

import junit.framework.TestCase;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * {@link Cache} tests that deal with empty caches.
 *
 * @author mike nonemacher
 */
public class EmptyCachesTest extends TestCase {
  public void testEmpty() {
    for (Cache<Object, Object> cache : caches()) {
      checkEmpty(cache);
    }
  }

  public void testInvalidate_empty() {
    for (Cache<Object, Object> cache : caches()) {
      cache.getUnchecked("a");
      cache.getUnchecked("b");
      cache.invalidate("a");
      cache.invalidate("b");
      cache.invalidate(0);
      checkEmpty(cache);
    }
  }

  public void testInvalidateAll_empty() {
    for (Cache<Object, Object> cache : caches()) {
      cache.getUnchecked("a");
      cache.getUnchecked("b");
      cache.getUnchecked("c");
      cache.invalidateAll();
      checkEmpty(cache);
    }
  }

  public void testEquals_null() {
    for (Cache<Object, Object> cache : caches()) {
      assertFalse(cache.equals(null));
    }
  }

  public void testEqualsAndHashCode_different() {
    for (CacheBuilder<Object, Object> builder : cacheFactory().buildAllPermutations()) {
      // all caches should be different: instance equality
      new EqualsTester()
          .addEqualityGroup(builder.build(identityLoader()))
          .addEqualityGroup(builder.build(identityLoader()))
          .addEqualityGroup(builder.build(identityLoader()))
          .testEquals();
    }
  }

  public void testGet_null() throws ExecutionException {
    for (Cache<Object, Object> cache : caches()) {
      try {
        cache.get(null);
        fail("Expected NullPointerException");
      } catch (NullPointerException e) {
        // expected
      }
      checkEmpty(cache);
    }
  }

  public void testGetUnchecked_null() {
    for (Cache<Object, Object> cache : caches()) {
      try {
        cache.getUnchecked(null);
        fail("Expected NullPointerException");
      } catch (NullPointerException e) {
        // expected
      }
      checkEmpty(cache);
    }
  }

  /* ---------------- Key Set -------------- */

  public void testKeySet_nullToArray() {
    for (Cache<Object, Object> cache : caches()) {
      Set<Object> keys = cache.asMap().keySet();
      try {
        keys.toArray(null);
        fail();
      } catch (NullPointerException e) {
        // expected
      }
      checkEmpty(cache);
    }
  }

  public void testKeySet_addNotSupported() {
    for (Cache<Object, Object> cache : caches()) {
      try {
        cache.asMap().keySet().add(1);
        fail();
      } catch (UnsupportedOperationException e) {
        // expected
      }

      try {
        cache.asMap().keySet().addAll(asList(1, 2));
        fail();
      } catch (UnsupportedOperationException e) {
        // expected
      }
    }
  }

  public void testKeySet_clear() {
    for (Cache<Object, Object> cache : caches()) {
      warmUp(cache, 0, 100);

      Set<Object> keys = cache.asMap().keySet();
      keys.clear();
      checkEmpty(keys);
      checkEmpty(cache);
    }
  }

  public void testKeySet_empty_remove() {
    for (Cache<Object, Object> cache : caches()) {
      Set<Object> keys = cache.asMap().keySet();
      assertFalse(keys.remove(null));
      assertFalse(keys.remove(6));
      assertFalse(keys.remove(-6));
      assertFalse(keys.removeAll(asList(null, 0, 15, 1500)));
      assertFalse(keys.retainAll(asList(null, 0, 15, 1500)));
      checkEmpty(keys);
      checkEmpty(cache);
    }
  }

  public void testKeySet_remove() {
    for (Cache<Object, Object> cache : caches()) {
      cache.getUnchecked(1);
      cache.getUnchecked(2);

      Set<Object> keys = cache.asMap().keySet();
      // We don't know whether these are still in the cache, so we can't assert on the return
      // values of these removes, but the cache should be empty after the removes, regardless.
      keys.remove(1);
      keys.remove(2);
      assertFalse(keys.remove(null));
      assertFalse(keys.remove(6));
      assertFalse(keys.remove(-6));
      assertFalse(keys.removeAll(asList(null, 0, 15, 1500)));
      assertFalse(keys.retainAll(asList(null, 0, 15, 1500)));
      checkEmpty(keys);
      checkEmpty(cache);
    }
  }

  /* ---------------- Values -------------- */

  public void testValues_nullToArray() {
    for (Cache<Object, Object> cache : caches()) {
      Collection<Object> values = cache.asMap().values();
      try {
        values.toArray(null);
        fail();
      } catch (NullPointerException e) {
        // expected
      }
      checkEmpty(cache);
    }
  }

  public void testValues_addNotSupported() {
    for (Cache<Object, Object> cache : caches()) {
      try {
        cache.asMap().values().add(1);
        fail();
      } catch (UnsupportedOperationException e) {
        // expected
      }

      try {
        cache.asMap().values().addAll(asList(1, 2));
        fail();
      } catch (UnsupportedOperationException e) {
        // expected
      }
    }
  }

  public void testValues_clear() {
    for (Cache<Object, Object> cache : caches()) {
      warmUp(cache, 0, 100);

      Collection<Object> values = cache.asMap().values();
      values.clear();
      checkEmpty(values);
      checkEmpty(cache);
    }
  }

  public void testValues_empty_remove() {
    for (Cache<Object, Object> cache : caches()) {
      Collection<Object> values = cache.asMap().values();
      assertFalse(values.remove(null));
      assertFalse(values.remove(6));
      assertFalse(values.remove(-6));
      assertFalse(values.removeAll(asList(null, 0, 15, 1500)));
      assertFalse(values.retainAll(asList(null, 0, 15, 1500)));
      checkEmpty(values);
      checkEmpty(cache);
    }
  }

  public void testValues_remove() {
    for (Cache<Object, Object> cache : caches()) {
      cache.getUnchecked(1);
      cache.getUnchecked(2);

      Collection<Object> values = cache.asMap().keySet();
      // We don't know whether these are still in the cache, so we can't assert on the return
      // values of these removes, but the cache should be empty after the removes, regardless.
      values.remove(1);
      values.remove(2);
      assertFalse(values.remove(null));
      assertFalse(values.remove(6));
      assertFalse(values.remove(-6));
      assertFalse(values.removeAll(asList(null, 0, 15, 1500)));
      assertFalse(values.retainAll(asList(null, 0, 15, 1500)));
      checkEmpty(values);
      checkEmpty(cache);
    }
  }

  /* ---------------- Entry Set -------------- */

  public void testEntrySet_nullToArray() {
    for (Cache<Object, Object> cache : caches()) {
      Set<Entry<Object, Object>> entries = cache.asMap().entrySet();
      try {
        entries.toArray(null);
        fail();
      } catch (NullPointerException e) {
        // expected
      }
      checkEmpty(cache);
    }
  }

  public void testEntrySet_addNotSupported() {
    for (Cache<Object, Object> cache : caches()) {
      try {
        cache.asMap().entrySet().add(entryOf(1, 1));
        fail();
      } catch (UnsupportedOperationException e) {
        // expected
      }

      try {
        cache.asMap().values().addAll(asList(entryOf(1, 1), entryOf(2, 2)));
        fail();
      } catch (UnsupportedOperationException e) {
        // expected
      }
    }
  }

  public void testEntrySet_clear() {
    for (Cache<Object, Object> cache : caches()) {
      warmUp(cache, 0, 100);

      Set<Entry<Object, Object>> entrySet = cache.asMap().entrySet();
      entrySet.clear();
      checkEmpty(entrySet);
      checkEmpty(cache);
    }
  }

  public void testEntrySet_empty_remove() {
    for (Cache<Object, Object> cache : caches()) {
      Set<Entry<Object, Object>> entrySet = cache.asMap().entrySet();
      assertFalse(entrySet.remove(null));
      assertFalse(entrySet.remove(entryOf(6, 6)));
      assertFalse(entrySet.remove(entryOf(-6, -6)));
      assertFalse(entrySet.removeAll(asList(null, entryOf(0, 0), entryOf(15, 15))));
      assertFalse(entrySet.retainAll( asList(null, entryOf(0, 0), entryOf(15, 15))));
      checkEmpty(entrySet);
      checkEmpty(cache);
    }
  }

  public void testEntrySet_remove() {
    for (Cache<Object, Object> cache : caches()) {
      cache.getUnchecked(1);
      cache.getUnchecked(2);

      Set<Entry<Object, Object>> entrySet = cache.asMap().entrySet();
      // We don't know whether these are still in the cache, so we can't assert on the return
      // values of these removes, but the cache should be empty after the removes, regardless.
      entrySet.remove(entryOf(1, 1));
      entrySet.remove(entryOf(2, 2));
      assertFalse(entrySet.remove(null));
      assertFalse(entrySet.remove(entryOf(1, 1)));
      assertFalse(entrySet.remove(entryOf(6, 6)));
      assertFalse(entrySet.removeAll(asList(null, entryOf(1, 1), entryOf(15, 15))));
      assertFalse(entrySet.retainAll(asList(null, entryOf(1, 1), entryOf(15, 15))));
      checkEmpty(entrySet);
      checkEmpty(cache);
    }
  }

  /* ---------------- Local utilities -------------- */

  /**
   * Most of the tests in this class run against every one of these caches.
   */
  private Iterable<Cache<Object, Object>> caches() {
    // lots of different ways to configure a Cache
    CacheBuilderFactory factory = cacheFactory();
    return Iterables.transform(factory.buildAllPermutations(),
        new Function<CacheBuilder<Object, Object>, Cache<Object, Object>>() {
          @Override public Cache<Object, Object> apply(CacheBuilder<Object, Object> builder) {
            return builder.build(identityLoader());
          }
        });
  }

  private CacheBuilderFactory cacheFactory() {
    return new CacheBuilderFactory()
        .withKeyStrengths(ImmutableSet.of(Strength.STRONG, Strength.WEAK))
        .withValueStrengths(ImmutableSet.copyOf(Strength.values()))
        .withConcurrencyLevels(ImmutableSet.of(1, 4, 16, 64))
        .withMaximumSizes(ImmutableSet.of(0, 1, 10, 100, 1000))
        .withInitialCapacities(ImmutableSet.of(0, 1, 10, 100, 1000))
        .withExpirations(ImmutableSet.of(
            ExpirationSpec.afterAccess(0, SECONDS),
            ExpirationSpec.afterAccess(1, SECONDS),
            ExpirationSpec.afterAccess(1, DAYS),
            ExpirationSpec.afterWrite(0, SECONDS),
            ExpirationSpec.afterWrite(1, SECONDS),
            ExpirationSpec.afterWrite(1, DAYS)));
  }

  private void warmUp(Cache<Object, Object> cache, int minimum, int maximum) {
    for (int i = minimum; i < maximum; i++) {
      cache.getUnchecked(i);
    }
  }

  private Entry<Object, Object> entryOf(Object key, Object value) {
    return Maps.immutableEntry(key, value);
  }
}
