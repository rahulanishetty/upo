/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.utilities.filter.impl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

/**
 * Represents a value that can be compared with other values of the same type. This interface
 * provides type safety for comparison operations while maintaining the comparable nature of the
 * wrapped value.
 *
 * @param <Value> the type of the wrapped comparable value
 */
public interface ComparableValue<Value extends Comparable<Value>>
    extends Comparable<ComparableValue<Value>> {

  static StringComparable fromString(String value) {
    if (value == null) {
      return null;
    }
    return new StringComparable(value);
  }

  static NumberComparable<Integer> fromInteger(Integer value) {
    if (value == null) {
      return null;
    }
    return new NumberComparable<>(value);
  }

  static NumberComparable<Integer> fromInteger(int value) {
    return new NumberComparable<>(value);
  }

  static NumberComparable<Long> fromLong(Long value) {
    if (value == null) {
      return null;
    }
    return new NumberComparable<>(value);
  }

  static NumberComparable<Long> fromLong(long value) {
    return new NumberComparable<>(value);
  }

  static NumberComparable<Byte> fromByte(Byte value) {
    if (value == null) {
      return null;
    }
    return new NumberComparable<>(value);
  }

  static NumberComparable<Byte> fromByte(byte value) {
    return new NumberComparable<>(value);
  }

  static NumberComparable<Short> fromShort(Short value) {
    if (value == null) {
      return null;
    }
    return new NumberComparable<>(value);
  }

  static NumberComparable<Short> fromShort(short value) {
    return new NumberComparable<>(value);
  }

  static NumberComparable<Double> fromDouble(Double value) {
    if (value == null) {
      return null;
    }
    return new NumberComparable<>(value);
  }

  static NumberComparable<Double> fromDouble(double value) {
    return new NumberComparable<>(value);
  }

  static NumberComparable<Float> fromFloat(Float value) {
    if (value == null) {
      return null;
    }
    return new NumberComparable<>(value);
  }

  static NumberComparable<Float> fromFloat(float value) {
    return new NumberComparable<>(value);
  }

  static NumberComparable<BigInteger> fromBigInteger(BigInteger value) {
    if (value == null) {
      return null;
    }
    return new NumberComparable<>(value);
  }

  static NumberComparable<BigDecimal> fromBigDecimal(BigDecimal value) {
    if (value == null) {
      return null;
    }
    return new NumberComparable<>(value);
  }

  static NumberComparable<Integer> fromBoolean(boolean value) {
    return new NumberComparable<>(value ? 1 : 0);
  }

  static NumberComparable<Integer> fromBoolean(Boolean value) {
    if (value == null) {
      return null;
    }
    return new NumberComparable<>(Boolean.TRUE.equals(value) ? 1 : 0);
  }

  class StringComparable implements ComparableValue<String> {

    private final String value;

    public StringComparable(String value) {
      this.value = value;
    }

    @Override
    public int compareTo(@SuppressWarnings("NullableProblems") ComparableValue<String> other) {
      if (other == null) {
        throw new NullPointerException("Cannot compare with null");
      }
      if (!(other instanceof StringComparable stringComparable)) {
        return -1;
      }
      if (this.value == null) {
        return stringComparable.value == null ? 0 : -1;
      }
      return this.value.compareTo((stringComparable.value));
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(value);
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof StringComparable comparable)) {
        return false;
      }
      return this.compareTo(comparable) == 0;
    }
  }

  class NumberComparable<T extends Number & Comparable<T>> implements ComparableValue<T> {

    private final T number;

    public NumberComparable(T number) {
      this.number = number;
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(number);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof NumberComparable comparable)) {
        return false;
      }
      return this.compareTo(comparable) == 0;
    }

    @Override
    public int compareTo(@SuppressWarnings("NullableProblems") ComparableValue<T> other) {
      if (other == null) {
        throw new NullPointerException("Cannot compare with null");
      }

      if (!(other instanceof NumberComparable<?> numberComparable)) {
        return -1;
      }

      return compare(this.number, numberComparable.number);
    }

    /**
     * Compares two numbers of potentially different types. Handles conversion and comparison
     * between different numeric types.
     *
     * @param n1 First number to compare
     * @param n2 Second number to compare
     * @return -1 if n1 < n2, 0 if n1 == n2, 1 if n1 > n2
     * @throws IllegalArgumentException if either argument is null or not a supported numeric type
     */
    public static int compare(Number n1, Number n2) {
      if (n1 == null || n2 == null) {
        throw new IllegalArgumentException("Cannot compare null numbers");
      }

     // If both are the same type, use natural comparison
      if (n1.getClass().equals(n2.getClass())) {
        if (n1 instanceof Comparable) {
         //noinspection unchecked,rawtypes
          return ((Comparable) n1).compareTo(n2);
        }
      }

     // Handle BigDecimal specially for precision
      if (n1 instanceof BigDecimal || n2 instanceof BigDecimal) {
        BigDecimal bd1 = toBigDecimal(n1);
        BigDecimal bd2 = toBigDecimal(n2);
        return bd1.compareTo(bd2);
      }

     // Handle BigInteger specially
      if (n1 instanceof BigInteger || n2 instanceof BigInteger) {
        BigInteger bi1 = toBigInteger(n1);
        BigInteger bi2 = toBigInteger(n2);
        return bi1.compareTo(bi2);
      }

     // Handle integral types using long comparison to avoid double precision loss
      if (isIntegralType(n1) && isIntegralType(n2)) {
        return Long.compare(n1.longValue(), n2.longValue());
      }

     // For floating point types, use double comparison
      return Double.compare(n1.doubleValue(), n2.doubleValue());
    }

    /** Converts a number to BigDecimal, maintaining precision where possible. */
    private static BigDecimal toBigDecimal(Number n) {
      if (n instanceof BigDecimal) {
        return (BigDecimal) n;
      }
      if (n instanceof BigInteger) {
        return new BigDecimal((BigInteger) n);
      }
      if (isIntegralType(n)) {
        return BigDecimal.valueOf(n.longValue());
      }
     // For floating point types, use string representation to avoid precision loss
      return new BigDecimal(n.toString());
    }

    /** Checks if a number is an integral type (Long, Integer, Short, Byte) */
    private static boolean isIntegralType(Number n) {
      return n instanceof Long || n instanceof Integer || n instanceof Short || n instanceof Byte;
    }

    private static BigInteger toBigInteger(Number n) {
      if (n instanceof BigInteger) {
        return (BigInteger) n;
      }
      if (n instanceof BigDecimal) {
        return ((BigDecimal) n).toBigInteger();
      }
      return BigInteger.valueOf(n.longValue());
    }
  }
}
