/*
 * Copyright (C) 2017 The Guava Authors
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

package com.google.common.primitives;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.Beta;
import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import com.google.errorprone.annotations.Immutable;
import java.io.Serializable;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.RandomAccess;
import javax.annotation.Nullable;

/**
 * An immutable array of {@code double} values, with an API resembling {@link List}.
 *
 * <p>Advantages compared to {@code double[]}:
 *
 * <ul>
 *   <li>All the many well-known advantages of immutability (read <i>Effective Java</i>, second
 *       edition, Item 15).
 *   <li>Has the value-based (not identity-based) {@link #equals}, {@link #hashCode}, and {@link
 *       #toString} behavior you expect.
 *   <li>Offers useful operations beyond just {@code get} and {@code length}, so you don't have to
 *       hunt through classes like {@link Arrays} and {@link Doubles} for them.
 *   <li>Supports a copy-free {@link #subArray} view, so methods that accept this type don't need to
 *       add overloads that accept start and end indexes.
 *   <li>Access to all collection-based utilities via {@link #asList} (though at the cost of
 *       allocating garbage).
 * </ul>
 *
 * <p>Disadvantages compared to {@code double[]}:
 *
 * <ul>
 *   <li>Memory footprint has a fixed overhead (about 24 bytes per instance).
 *   <li><i>Some</i> construction use cases force the data to be copied (though several construction
 *       APIs are offered that don't).
 *   <li>Can't be passed directly to methods that expect {@code double[]} (though the most common
 *       utilities do have replacements here).
 *   <li>Dependency on {@code com.google.common} / Guava.
 * </ul>
 *
 * <p>Advantages compared to {@link com.google.common.collect.ImmutableList ImmutableList}{@code
 * <Double>}:
 *
 * <ul>
 *   <li>Improved memory compactness and locality.
 *   <li>Can be queried without allocating garbage.
 * </ul>
 *
 * <p>Disadvantages compared to {@code ImmutableList<Double>}:
 *
 * <ul>
 *   <li>Can't be passed directly to methods that expect {@code Iterable}, {@code Collection}, or
 *       {@code List} (though the most common utilities do have replacements here, and there is a
 *       lazy {@link #asList} view).
 * </ul>
 *
 * @since 22.0
 */
@Beta
@GwtCompatible
@Immutable
public final class ImmutableDoubleArray implements Serializable {
  private static final ImmutableDoubleArray EMPTY = new ImmutableDoubleArray(new double[0]);

  /** Returns the empty array. */
  public static ImmutableDoubleArray of() {
    return EMPTY;
  }

  /** Returns an immutable array containing a single value. */
  public static ImmutableDoubleArray of(double e0) {
    return new ImmutableDoubleArray(new double[] {e0});
  }

  /** Returns an immutable array containing the given values, in order. */
  public static ImmutableDoubleArray of(double e0, double e1) {
    return new ImmutableDoubleArray(new double[] {e0, e1});
  }

  /** Returns an immutable array containing the given values, in order. */
  public static ImmutableDoubleArray of(double e0, double e1, double e2) {
    return new ImmutableDoubleArray(new double[] {e0, e1, e2});
  }

  /** Returns an immutable array containing the given values, in order. */
  public static ImmutableDoubleArray of(double e0, double e1, double e2, double e3) {
    return new ImmutableDoubleArray(new double[] {e0, e1, e2, e3});
  }

  /** Returns an immutable array containing the given values, in order. */
  public static ImmutableDoubleArray of(double e0, double e1, double e2, double e3, double e4) {
    return new ImmutableDoubleArray(new double[] {e0, e1, e2, e3, e4});
  }

  /** Returns an immutable array containing the given values, in order. */
  public static ImmutableDoubleArray of(
      double e0, double e1, double e2, double e3, double e4, double e5) {
    return new ImmutableDoubleArray(new double[] {e0, e1, e2, e3, e4, e5});
  }

  // TODO(kevinb): go up to 11?

  /** Returns an immutable array containing the given values, in order. */
  // Use (first, rest) so that `of(someDoubleArray)` won't compile (they should use copyOf), which
  // is okay since we have to copy the just-created array anyway.
  public static ImmutableDoubleArray of(double first, double... rest) {
    double[] array = new double[rest.length + 1];
    array[0] = first;
    System.arraycopy(rest, 0, array, 1, rest.length);
    return new ImmutableDoubleArray(array);
  }

  /** Returns an immutable array containing the given values, in order. */
  public static ImmutableDoubleArray copyOf(double[] values) {
    return values.length == 0
        ? EMPTY
        : new ImmutableDoubleArray(Arrays.copyOf(values, values.length));
  }

  /** Returns an immutable array containing the given values, in order. */
  public static ImmutableDoubleArray copyOf(Collection<Double> values) {
    return values.isEmpty() ? EMPTY : new ImmutableDoubleArray(Doubles.toArray(values));
  }

  /**
   * Returns an immutable array containing the given values, in order.
   *
   * <p><b>Performance note:</b> this method delegates to {@link #copyOf(Collection)} if {@code
   * values} is a {@link Collection}. Otherwise it creates a {@link #builder} and uses {@link
   * Builder#addAll(Iterable)}, with all the performance implications associated with that.
   */
  public static ImmutableDoubleArray copyOf(Iterable<Double> values) {
    if (values instanceof Collection) {
      return copyOf((Collection<Double>) values);
    }
    return builder().addAll(values).build();
  }

  /**
   * Returns a new, empty builder for {@link ImmutableDoubleArray} instances, sized to hold up to
   * {@code initialCapacity} values without resizing. The returned builder is not thread-safe.
   *
   * <p><b>Performance note:</b> When feasible, {@code initialCapacity} should be the exact number
   * of values that will be added, if that knowledge is readily available. It is better to guess a
   * value slightly too high than slightly too low. If the value is not exact, the {@link
   * ImmutableDoubleArray} that is built will very likely occupy more memory than strictly
   * necessary; to trim memory usage, build using {@code builder.build().trimmed()}.
   */
  public static Builder builder(int initialCapacity) {
    checkArgument(initialCapacity >= 0, "Invalid initialCapacity: %s", initialCapacity);
    return new Builder(initialCapacity);
  }

  /**
   * Returns a new, empty builder for {@link ImmutableDoubleArray} instances, with a default initial
   * capacity. The returned builder is not thread-safe.
   *
   * <p><b>Performance note:</b> The {@link ImmutableDoubleArray} that is built will very likely
   * occupy more memory than necessary; to trim memory usage, build using {@code
   * builder.build().trimmed()}.
   */
  public static Builder builder() {
    return new Builder(10);
  }

  /**
   * A builder for {@link ImmutableDoubleArray} instances; obtained using {@link
   * ImmutableDoubleArray#builder}.
   */
  @CanIgnoreReturnValue
  public static final class Builder {
    private double[] array;
    private int count = 0; // <= array.length

    Builder(int initialCapacity) {
      array = new double[initialCapacity];
    }

    /**
     * Appends {@code value} to the end of the values the built {@link ImmutableDoubleArray} will
     * contain.
     */
    public Builder add(double value) {
      ensureRoomFor(1);
      array[count] = value;
      count += 1;
      return this;
    }

    /**
     * Appends {@code values}, in order, to the end of the values the built {@link
     * ImmutableDoubleArray} will contain.
     */
    public Builder addAll(double[] values) {
      ensureRoomFor(values.length);
      System.arraycopy(values, 0, array, count, values.length);
      count += values.length;
      return this;
    }

    /**
     * Appends {@code values}, in order, to the end of the values the built {@link
     * ImmutableDoubleArray} will contain.
     */
    public Builder addAll(Iterable<Double> values) {
      if (values instanceof Collection) {
        return addAll((Collection<Double>) values);
      }
      for (Double value : values) {
        add(value);
      }
      return this;
    }

    /**
     * Appends {@code values}, in order, to the end of the values the built {@link
     * ImmutableDoubleArray} will contain.
     */
    public Builder addAll(Collection<Double> values) {
      ensureRoomFor(values.size());
      for (Double value : values) {
        array[count++] = value;
      }
      return this;
    }

    /**
     * Appends {@code values}, in order, to the end of the values the built {@link
     * ImmutableDoubleArray} will contain.
     */
    public Builder addAll(ImmutableDoubleArray values) {
      ensureRoomFor(values.length());
      System.arraycopy(values.array, values.start, array, count, values.length());
      count += values.length();
      return this;
    }

    private void ensureRoomFor(int numberToAdd) {
      int newCount = count + numberToAdd; // TODO(kevinb): check overflow now?
      if (newCount > array.length) {
        double[] newArray = new double[expandedCapacity(array.length, newCount)];
        System.arraycopy(array, 0, newArray, 0, count);
        this.array = newArray;
      }
    }

    // Unfortunately this is pasted from ImmutableCollection.Builder.
    private static int expandedCapacity(int oldCapacity, int minCapacity) {
      if (minCapacity < 0) {
        throw new AssertionError("cannot store more than MAX_VALUE elements");
      }
      // careful of overflow!
      int newCapacity = oldCapacity + (oldCapacity >> 1) + 1;
      if (newCapacity < minCapacity) {
        newCapacity = Integer.highestOneBit(minCapacity - 1) << 1;
      }
      if (newCapacity < 0) {
        newCapacity = Integer.MAX_VALUE; // guaranteed to be >= newCapacity
      }
      return newCapacity;
    }

    /**
     * Returns a new immutable array. The builder can continue to be used after this call, to append
     * more values and build again.
     *
     * <p><b>Performance note:</b> the returned array is backed by the same array as the builder, so
     * no data is copied as part of this step, but this may occupy more memory than strictly
     * necessary. To copy the data to a right-sized backing array, use {@code .build().trimmed()}.
     */
    @CheckReturnValue
    public ImmutableDoubleArray build() {
      return count == 0 ? EMPTY : new ImmutableDoubleArray(array, 0, count);
    }
  }

  // Instance stuff here

  // The array is never mutated after storing in this field and the construction strategies ensure
  // it doesn't escape this class
  @SuppressWarnings("Immutable")
  private final double[] array;

  /*
   * TODO(kevinb): evaluate the trade-offs of going bimorphic to save these two fields from most
   * instances. Note that the instances that would get smaller are the right set to care about
   * optimizing, because the rest have the option of calling `trimmed`.
   */

  private final transient int start; // it happens that we only serialize instances where this is 0
  private final int end; // exclusive

  private ImmutableDoubleArray(double[] array) {
    this(array, 0, array.length);
  }

  private ImmutableDoubleArray(double[] array, int start, int end) {
    this.array = array;
    this.start = start;
    this.end = end;
  }

  /** Returns the number of values in this array. */
  public int length() {
    return end - start;
  }

  /** Returns {@code true} if there are no values in this array ({@link #length} is zero). */
  public boolean isEmpty() {
    return end == start;
  }

  /**
   * Returns the {@code double} value present at the given index.
   *
   * @throws IndexOutOfBoundsException if {@code index} is negative, or greater than or equal to
   *     {@link #length}
   */
  public double get(int index) {
    Preconditions.checkElementIndex(index, length());
    return array[start + index];
  }

  /**
   * Returns the smallest index for which {@link #get} returns {@code target}, or {@code -1} if no
   * such index exists. Values are compared as if by {@link Double#equals}. Equivalent to {@code
   * asList().indexOf(target)}.
   */
  public int indexOf(double target) {
    for (int i = start; i < end; i++) {
      if (areEqual(array[i], target)) {
        return i - start;
      }
    }
    return -1;
  }

  /**
   * Returns the largest index for which {@link #get} returns {@code target}, or {@code -1} if no
   * such index exists. Values are compared as if by {@link Double#equals}. Equivalent to {@code
   * asList().lastIndexOf(target)}.
   */
  public int lastIndexOf(double target) {
    for (int i = end - 1; i >= start; i--) {
      if (areEqual(array[i], target)) {
        return i - start;
      }
    }
    return -1;
  }

  /**
   * Returns {@code true} if {@code target} is present at any index in this array. Values are
   * compared as if by {@link Double#equals}. Equivalent to {@code asList().contains(target)}.
   */
  public boolean contains(double target) {
    return indexOf(target) >= 0;
  }

  /** Returns a new, mutable copy of this array's values, as a primitive {@code double[]}. */
  public double[] toArray() {
    return Arrays.copyOfRange(array, start, end);
  }

  /**
   * Returns a new immutable array containing the values in the specified range.
   *
   * <p><b>Performance note:</b> The returned array has the same full memory footprint as this one
   * does (no actual copying is performed). To reduce memory usage, use {@code subArray(start,
   * end).trimmed()}.
   */
  public ImmutableDoubleArray subArray(int startIndex, int endIndex) {
    Preconditions.checkPositionIndexes(startIndex, endIndex, length());
    return startIndex == endIndex
        ? EMPTY
        : new ImmutableDoubleArray(array, start + startIndex, start + endIndex);
  }

  /**
   * Returns an immutable <i>view</i> of this array's values as a {@code List}; note that {@code
   * double} values are boxed into {@link Double} instances on demand, which can be very expensive.
   * The returned list should be used once and discarded. For any usages beyond that, pass the
   * returned list to {@link com.google.common.collect.ImmutableList#copyOf(Collection)
   * ImmutableList.copyOf} and use that list instead.
   */
  public List<Double> asList() {
    /*
     * Typically we cache this kind of thing, but much repeated use of this view is a performance
     * anti-pattern anyway. If we cache, then everyone pays a price in memory footprint even if
     * they never use this method.
     */
    return new AsList(this);
  }

  static class AsList extends AbstractList<Double> implements RandomAccess, Serializable {
    private final ImmutableDoubleArray parent;

    private AsList(ImmutableDoubleArray parent) {
      this.parent = parent;
    }

    // inherit: isEmpty, containsAll, toArray x2, iterator, listIterator, mutations

    @Override
    public int size() {
      return parent.length();
    }

    @Override
    public Double get(int index) {
      return parent.get(index);
    }

    @Override
    public boolean contains(Object target) {
      return indexOf(target) >= 0;
    }

    @Override
    public int indexOf(Object target) {
      return target instanceof Double ? parent.indexOf((Double) target) : -1;
    }

    @Override
    public int lastIndexOf(Object target) {
      return target instanceof Double ? parent.lastIndexOf((Double) target) : -1;
    }

    @Override
    public List<Double> subList(int fromIndex, int toIndex) {
      return parent.subArray(fromIndex, toIndex).asList();
    }

    @Override
    public boolean equals(@Nullable Object object) {
      if (object instanceof AsList) {
        AsList that = (AsList) object;
        return this.parent.equals(that.parent);
      }
      // We could delegate to super now but it would still box too much
      if (!(object instanceof List)) {
        return false;
      }
      List<?> that = (List<?>) object;
      if (this.size() != that.size()) {
        return false;
      }
      int i = parent.start;
      // Since `that` is very likely RandomAccess we could avoid allocating this iterator...
      for (Object element : that) {
        if (!(element instanceof Double) || !areEqual(parent.array[i++], (Double) element)) {
          return false;
        }
      }
      return true;
    }

    // Because we happen to use the same formula. If that changes, just don't override this.
    @Override
    public int hashCode() {
      return parent.hashCode();
    }

    @Override
    public String toString() {
      return parent.toString();
    }
  }

  /**
   * Returns {@code true} if {@code object} is an {@code ImmutableDoubleArray} containing the same
   * values as this one, in the same order. Values are compared as if by {@link Double#equals}.
   */
  @Override
  public boolean equals(@Nullable Object object) {
    if (object == this) {
      return true;
    }
    if (!(object instanceof ImmutableDoubleArray)) {
      return false;
    }
    ImmutableDoubleArray that = (ImmutableDoubleArray) object;
    if (this.length() != that.length()) {
      return false;
    }
    for (int i = 0; i < length(); i++) {
      if (!areEqual(this.get(i), that.get(i))) {
        return false;
      }
    }
    return true;
  }

  // Match the behavior of Double.equals()
  private static boolean areEqual(double a, double b) {
    return Double.doubleToLongBits(a) == Double.doubleToLongBits(b);
  }

  /** Returns an unspecified hash code for the contents of this immutable array. */
  @Override
  public int hashCode() {
    int hash = 1;
    for (int i = start; i < end; i++) {
      hash *= 31;
      hash += Doubles.hashCode(array[i]);
    }
    return hash;
  }

  /**
   * Returns a string representation of this array in the same form as {@link
   * Arrays#toString(double[])}, for example {@code "[1, 2, 3]"}.
   */
  @Override
  public String toString() {
    if (isEmpty()) {
      return "[]";
    }
    StringBuilder builder = new StringBuilder(length() * 5); // rough estimate is fine
    builder.append('[').append(array[start]);

    for (int i = start + 1; i < end; i++) {
      builder.append(", ").append(array[i]);
    }
    builder.append(']');
    return builder.toString();
  }

  /**
   * Returns an immutable array containing the same values as {@code this} array. This is logically
   * a no-op, and in some circumstances {@code this} itself is returned. However, if this instance
   * is a {@link #subArray} view of a larger array, this method will copy only the appropriate range
   * of values, resulting in an equivalent array with a smaller memory footprint.
   */
  public ImmutableDoubleArray trimmed() {
    return isPartialView() ? new ImmutableDoubleArray(toArray()) : this;
  }

  private boolean isPartialView() {
    return start > 0 || end < array.length;
  }

  Object writeReplace() {
    return trimmed();
  }

  Object readResolve() {
    return isEmpty() ? EMPTY : this;
  }
}
