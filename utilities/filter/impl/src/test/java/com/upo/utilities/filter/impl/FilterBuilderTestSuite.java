/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.utilities.filter.impl;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Test suite for all filter builder implementations. Groups together all filter-related test cases
 * for comprehensive testing of the filter building and evaluation functionality.
 */
@Suite
@SuiteDisplayName("Filter Builder Test Suite")
@SelectClasses({
  ComparisonFilterBuilderTest.class,
  EqualityFilterBuilderTest.class,
  ExistenceFilterBuilderTest.class,
  MatchFilterBuilderTest.class,
  NestedFilterBuilderTest.class,
  RangeFilterBuilderTest.class,
  TextMatchFilterBuilderTest.class,
  ComparableValueTest.class
})
public class FilterBuilderTestSuite {
 // The suite is defined by the annotations above
}
