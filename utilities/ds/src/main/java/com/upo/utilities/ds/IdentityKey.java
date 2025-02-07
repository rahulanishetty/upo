/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.utilities.ds;

public record IdentityKey<T>(T value) {

  public static <T> IdentityKey<T> of(T value) {
    return new IdentityKey<>(value);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof IdentityKey<?> that)) return false;
    return value == that.value;// Identity comparison
  }

  @Override
  public int hashCode() {
    return System.identityHashCode(value);
  }
}
