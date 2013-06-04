/*
 * Copyright (C) 2007 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.common.collect;

import static java.util.Arrays.asList;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.collect.Multiset.Entry;
import com.google.common.collect.testing.google.UnmodifiableCollectionTests;
import com.google.common.testing.SerializableTester;

import java.util.Iterator;
import java.util.Set;

/**
 * Common tests for a {@link Multiset}.
 *
 * @author Kevin Bourrillion
 */
@GwtCompatible(emulated = true)
public abstract class AbstractMultisetTest extends AbstractCollectionTest {

  @Override protected abstract <E> Multiset<E> create();

  protected Multiset<String> ms;

  // public for GWT
  @Override public void setUp() throws Exception {
    super.setUp();
    c = ms = create();
  }

  /**
   * Validates that multiset size returned by {@code size()} is the same as the
   * size generated by summing the counts of all multiset entries.
   */
  protected void assertSize(Multiset<String> multiset) {
    long size = 0;
    for (Multiset.Entry<String> entry : multiset.entrySet()) {
      size += entry.getCount();
    }
    assertEquals((int) Math.min(size, Integer.MAX_VALUE), multiset.size());
  }

  protected void assertSize() {
    assertSize(ms);
  }

  @Override protected void assertContents(String... expected) {
    super.assertContents(expected);
    assertSize();
  }
  
  static class WrongType {}

  @Override public void testEqualsNo() {
    ms.add("a");
    ms.add("b");
    ms.add("b");

    Multiset<String> ms2 = create();
    ms2.add("a", 2);
    ms2.add("b");

    assertFalse(ms.equals(ms2));
    assertSize();
  }

  public void testRemoveNoneFromSome() {
    ms.add("a");
    assertEquals(1, ms.remove("a", 0));
    assertContents("a");
  }

  public void testRemoveOneFromNone() {
    assertEquals(0, ms.remove("a", 1));
    assertContents();
  }

  public void testRemoveOneFromOne() {
    ms.add("a");
    assertEquals(1, ms.remove("a", 1));
    assertContents();
  }

  public void testRemoveSomeFromSome() {
    ms.add("a", 5);
    assertEquals(5, ms.remove("a", 3));
    assertContents("a", "a");
  }

  public void testRemoveTooMany() {
    ms.add("a", 3);
    assertEquals(3, ms.remove("a", 5));
    assertContents();
  }

  public void testRemoveNegative() {
    try {
      ms.remove("a", -1);
      fail();
    } catch (IllegalArgumentException expected) {
    }
    assertSize();
  }

  public void testRemoveAllOfOne() {
    ms.add("a", 2);
    ms.add("b");
    assertTrue(ms.removeAll(asList("a", "c")));
    assertContents("b");
  }

  public void testRemoveAllOfDisjoint() {
    ms.add("a", 2);
    ms.add("b");
    assertFalse(ms.removeAll(asList("c", "d")));
    assertContents("a", "a", "b");
  }

  public void testRemoveAllOfEverything() {
    ms.add("a", 2);
    ms.add("b");
    assertTrue(ms.removeAll(asList("a", "b")));
    assertContents();
  }

  public void testRetainAllOfOne() {
    ms.add("a", 2);
    ms.add("b");
    assertTrue(ms.retainAll(asList("a", "c")));
    assertContents("a", "a");
  }

  public void testRetainAllOfDisjoint() {
    ms.add("a", 2);
    ms.add("b");
    assertTrue(ms.retainAll(asList("c", "d")));
    assertContents();
  }

  public void testRetainAllOfEverything() {
    ms.add("a", 2);
    ms.add("b");
    assertFalse(ms.retainAll(asList("a", "b")));
    assertContents("a", "a", "b");
  }

  public void testElementSetIsNotACopy() {
    ms.add("a", 1);
    ms.add("b", 2);
    Set<String> elementSet = ms.elementSet();
    ms.add("c", 3);
    ms.setCount("b", 0);
    assertEquals(Sets.newHashSet("a", "c"), elementSet);
    assertSize();
  }

  public void testRemoveFromElementSetYes() {
    ms.add("a", 1);
    ms.add("b", 2);
    Set<String> elementSet = ms.elementSet();
    assertTrue(elementSet.remove("b"));
    assertContents("a");
  }

  public void testRemoveFromElementSetNo() {
    ms.add("a", 1);
    Set<String> elementSet = ms.elementSet();
    assertFalse(elementSet.remove("b"));
    assertContents("a");
  }

  public void testClearViaElementSet() {
    ms = createSample();
    ms.elementSet().clear();
    assertContents();
  }

  public void testClearViaEntrySet() {
    ms = createSample();
    ms.entrySet().clear();
    assertContents();
  }

  public void testReallyBig() {
    ms.add("a", Integer.MAX_VALUE - 1);
    assertEquals(Integer.MAX_VALUE - 1, ms.size());
    ms.add("b", 3);

    // See Collection.size() contract
    assertEquals(Integer.MAX_VALUE, ms.size());

    // Make sure we didn't forget our size
    ms.remove("a", 4);
    assertEquals(Integer.MAX_VALUE - 2, ms.size());
    assertSize();
  }

  public void testToStringNull() {
    ms.add("a", 3);
    ms.add("c", 1);
    ms.add("b", 2);
    ms.add(null, 4);

    // This test is brittle. The original test was meant to validate the
    // contents of the string itself, but key ordering tended to change
    // under unpredictable circumstances. Instead, we're just ensuring
    // that the string not return null, and implicitly, not throw an exception.
    assertNotNull(ms.toString());
    assertSize();
  }

  @GwtIncompatible("SerializableTester")
  public void testSerializable() {
    ms = createSample();
    assertEquals(ms, SerializableTester.reserialize(ms));
    assertSize();
  }

  public void testEntryAfterRemove() {
    ms.add("a", 8);
    Multiset.Entry<String> entry = ms.entrySet().iterator().next();
    assertEquals(8, entry.getCount());
    ms.remove("a");
    assertEquals(7, entry.getCount());
    ms.remove("a", 4);
    assertEquals(3, entry.getCount());
    ms.elementSet().remove("a");
    assertEquals(0, entry.getCount());
    ms.add("a", 5);
    assertEquals(5, entry.getCount());
  }

  public void testEntryAfterClear() {
    ms.add("a", 3);
    Multiset.Entry<String> entry = ms.entrySet().iterator().next();
    ms.clear();
    assertEquals(0, entry.getCount());
    ms.add("a", 5);
    assertEquals(5, entry.getCount());
  }

  public void testEntryAfterEntrySetClear() {
    ms.add("a", 3);
    Multiset.Entry<String> entry = ms.entrySet().iterator().next();
    ms.entrySet().clear();
    assertEquals(0, entry.getCount());
    ms.add("a", 5);
    assertEquals(5, entry.getCount());
  }

  public void testEntryAfterEntrySetIteratorRemove() {
    ms.add("a", 3);
    Iterator<Multiset.Entry<String>> iterator = ms.entrySet().iterator();
    Multiset.Entry<String> entry = iterator.next();
    iterator.remove();
    assertEquals(0, entry.getCount());
    try {
      iterator.remove();
      fail();
    } catch (IllegalStateException expected) {}
    ms.add("a", 5);
    assertEquals(5, entry.getCount());
  }

  public void testEntryAfterElementSetIteratorRemove() {
    ms.add("a", 3);
    Multiset.Entry<String> entry = ms.entrySet().iterator().next();
    Iterator<String> iterator = ms.elementSet().iterator();
    iterator.next();
    iterator.remove();
    assertEquals(0, entry.getCount());
    ms.add("a", 5);
    assertEquals(5, entry.getCount());
  }

  public void testEntrySetRemove() {
    ms.add("a", 3);
    Set<Entry<String>> es = ms.entrySet();
    assertFalse(es.remove(null));
    assertFalse(es.remove(Maps.immutableEntry("a", 3)));
    assertFalse(es.remove(Multisets.immutableEntry("a", 2)));
    assertFalse(es.remove(Multisets.immutableEntry("b", 3)));
    assertFalse(es.remove(Multisets.immutableEntry("b", 0)));
    assertEquals(3, ms.count("a"));
    assertTrue(es.remove(Multisets.immutableEntry("a", 3)));
    assertEquals(0, ms.count("a"));
  }

  public void testUnmodifiableMultiset() {
    ms.add("a", 3);
    ms.add("b");
    ms.add("c", 2);
    Multiset<Object> unmodifiable = Multisets.<Object>unmodifiableMultiset(ms);
    UnmodifiableCollectionTests.assertMultisetIsUnmodifiable(unmodifiable, "a");
  }

  @Override protected Multiset<String> createSample() {
    Multiset<String> ms = create();
    ms.add("a", 1);
    ms.add("b", 2);
    ms.add("c", 1);
    ms.add("d", 3);
    return ms;
  }
}
