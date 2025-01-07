/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.utilities.ds;

import java.util.Objects;

/**
 * A generic container class representing a pair of two related objects.
 *
 * @param <K> the type of the first element in the pair
 * @param <V> the type of the second element in the pair
 */
public class Pair<K, V> {

  private final K firstElement;
  private final V secondElement;

  public Pair(K firstElement, V secondElement) {
    this.firstElement = firstElement;
    this.secondElement = secondElement;
  }

  public static <K, V> Pair<K, V> of(K one, V two) {
    return new Pair<>(one, two);
  }

  public K getFirstElement() {
    return firstElement;
  }

  public V getSecondElement() {
    return secondElement;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    Pair<?, ?> pair = (Pair<?, ?>) o;
    return Objects.equals(firstElement, pair.firstElement)
        && Objects.equals(secondElement, pair.secondElement);
  }

  @Override
  public int hashCode() {
    return Objects.hash(firstElement, secondElement);
  }
}
