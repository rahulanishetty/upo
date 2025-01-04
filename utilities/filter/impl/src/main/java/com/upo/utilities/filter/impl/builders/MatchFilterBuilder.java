/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.utilities.filter.impl.builders;

import com.upo.utilities.filter.api.Filter;
import com.upo.utilities.filter.api.MatchAllFilter;
import com.upo.utilities.filter.api.MatchNoneFilter;
import com.upo.utilities.filter.impl.FilterBuilder;
import com.upo.utilities.filter.impl.FilterContext;
import com.upo.utilities.filter.impl.FilterEvaluator;

public class MatchFilterBuilder implements FilterBuilder {

  @Override
  public <Type> FilterEvaluator<Type> toEvaluator(Filter filter, FilterContext<Type> context) {
    return switch (filter.getType()) {
      case MatchAllFilter.TYPE -> buildMatchAllEvaluator();
      case MatchNoneFilter.TYPE -> buildMatchNoneEvaluator();
      default ->
          throw new IllegalArgumentException("Expected match filter but got: " + filter.getType());
    };
  }

  /** Creates an evaluator that always returns true. */
  private <Type> FilterEvaluator<Type> buildMatchAllEvaluator() {
    return record -> true;
  }

  /** Creates an evaluator that always returns false. */
  private <Type> FilterEvaluator<Type> buildMatchNoneEvaluator() {
    return record -> false;
  }
}
