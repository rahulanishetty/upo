/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.utilities.filter.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.stream.Stream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ComparableValueTest {

  @Nested
  class StringComparableTest {
    @Test
    void shouldCompareEqualStrings() {
      var str1 = ComparableValue.fromString("test");
      var str2 = ComparableValue.fromString("test");

      assertEquals(0, str1.compareTo(str2));
      assertEquals(str1, str2);
      assertEquals(str1.hashCode(), str2.hashCode());
    }

    @Test
    void shouldCompareDifferentStrings() {
      var str1 = ComparableValue.fromString("abc");
      var str2 = ComparableValue.fromString("def");

      assertTrue(str1.compareTo(str2) < 0);
      assertTrue(str2.compareTo(str1) > 0);
      assertNotEquals(str1, str2);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void shouldHandleNonStringComparable() {
      Comparable str = ComparableValue.fromString("test");
      Comparable num = ComparableValue.fromInteger(42);

      assertEquals(-1, str.compareTo(num));
      assertNotEquals(str, num);
    }

    @Test
    void shouldHandleNullString() {
      var str = ComparableValue.fromString(null);
      var regularStr = ComparableValue.fromString("test");

      assertThrows(NullPointerException.class, () -> str.compareTo(regularStr));
    }
  }

  @Nested
  class NumberComparableTest {

    @SuppressWarnings({"rawtypes", "unchecked"})
    @ParameterizedTest
    @MethodSource("provideNumberComparisons")
    void shouldCompareNumbers(Number n1, Number n2, int expectedComparison) {
      Comparable comp1 = createNumberComparable(n1);
      Comparable comp2 = createNumberComparable(n2);

      int result = comp1.compareTo(comp2);
      assertEquals(expectedComparison, Integer.signum(result));
    }

    private static Stream<Arguments> provideNumberComparisons() {
      return Stream.of(
         // Same type comparisons
          Arguments.of(42, 42, 0),
          Arguments.of(42L, 42L, 0),
          Arguments.of(42.0, 42.0, 0),
          Arguments.of(new BigDecimal("42"), new BigDecimal("42"), 0),

         // Cross type comparisons
          Arguments.of(42, 42L, 0),
          Arguments.of(42, 42.0, 0),
          Arguments.of(42L, new BigInteger("42"), 0),

         // Different values
          Arguments.of(42, 43, -1),
          Arguments.of(43, 42, 1),
          Arguments.of(42.5, 42, 1),

         // Edge cases
          Arguments.of(Long.MAX_VALUE, Long.MAX_VALUE - 1, 1),
          Arguments.of(new BigDecimal("0.1"), 0.1, 0));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void shouldHandleNonNumberComparable() {
      Comparable num = ComparableValue.fromInteger(42);
      Comparable str = ComparableValue.fromString("test");

      assertEquals(-1, num.compareTo(str));
      assertNotEquals(num, str);
    }

    @Test
    void shouldHandlePrecisionWithBigDecimal() {
      var bd1 = ComparableValue.fromBigDecimal(new BigDecimal("0.1"));
      var bd2 = ComparableValue.fromBigDecimal(new BigDecimal("0.10"));

      assertEquals(0, bd1.compareTo(bd2));
      assertEquals(bd1, bd2);
    }

    @Test
    void shouldConvertBoolean() {
      var trueValue = ComparableValue.fromBoolean(true);
      var falseValue = ComparableValue.fromBoolean(false);

      assertTrue(trueValue.compareTo(falseValue) > 0);
      assertEquals(ComparableValue.fromInteger(1), trueValue);
      assertEquals(ComparableValue.fromInteger(0), falseValue);
    }

    @Test
    void shouldHandleNullNumber() {
      assertThrows(
          IllegalArgumentException.class, () -> ComparableValue.NumberComparable.compare(null, 42));
      assertThrows(
          IllegalArgumentException.class, () -> ComparableValue.NumberComparable.compare(42, null));
    }

    private ComparableValue<?> createNumberComparable(Number n) {
      if (n instanceof Integer i) return ComparableValue.fromInteger(i);
      if (n instanceof Long l) return ComparableValue.fromLong(l);
      if (n instanceof Double d) return ComparableValue.fromDouble(d);
      if (n instanceof Float f) return ComparableValue.fromFloat(f);
      if (n instanceof BigDecimal bd) return ComparableValue.fromBigDecimal(bd);
      if (n instanceof BigInteger bi) return ComparableValue.fromBigInteger(bi);
      if (n instanceof Short s) return ComparableValue.fromShort(s);
      if (n instanceof Byte b) return ComparableValue.fromByte(b);
      throw new IllegalArgumentException("Unsupported number type: " + n.getClass());
    }
  }
}
