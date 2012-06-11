/*
 * Copyright (C) 2011 The Guava Authors
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

package com.google.common.hash;

import static com.google.common.hash.Hashing.murmur3_32;

import com.google.common.hash.Funnels;
import com.google.common.hash.HashTestUtils.HashFn;

import junit.framework.TestCase;

/**
 * Tests for {@link Murmur3_32HashFunction}.
 */
public class Murmur3Hash32Test extends TestCase {
  public void testKnownIntegerInputs() {
    assertHash(593689054, 0);
    assertHash(-189366624, -42);
    assertHash(-1134849565, 42);
    assertHash(-1718298732, Integer.MIN_VALUE);
    assertHash(-1653689534, Integer.MAX_VALUE);
  }

  public void testKnownStringInputs() {
    assertHash(0, "");
    assertHash(679745764, "k");
    assertHash(-675079799, "hello");
    assertHash(1935035788, "http://www.google.com/");
    assertHash(-528633700, "The quick brown fox jumps over the lazy dog");
  }

  private static void assertHash(int expected, int input) {
    assertEquals(expected, murmur3_32().hashInt(input).asInt());
    assertEquals(expected, murmur3_32().newHasher().putInt(input).hash().asInt());
  }

  private static void assertHash(int expected, String input) {
    assertEquals(expected, murmur3_32().hashString(input).asInt());
    assertEquals(expected, murmur3_32().newHasher().putString(input).hash().asInt());
  }

  public void testParanoid() {
    HashFn hf = new HashFn() {
      @Override public byte[] hash(byte[] input, int seed) {
        Hasher hasher = murmur3_32(seed).newHasher();
        Funnels.byteArrayFunnel().funnel(input, hasher);
        return hasher.hash().asBytes();
      }
    };
    // Murmur3A, MurmurHash3 for x86, 32-bit (MurmurHash3_x86_32)
    // http://code.google.com/p/smhasher/source/browse/trunk/main.cpp
    HashTestUtils.verifyHashFunction(hf, 32, 0xB0F57EE3);
  }

  public void testInvariants() {
    HashTestUtils.assertInvariants(murmur3_32());
  }
}
