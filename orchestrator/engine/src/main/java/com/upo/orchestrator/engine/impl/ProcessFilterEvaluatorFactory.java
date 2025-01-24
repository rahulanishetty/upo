/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.impl;

import java.util.Set;

import com.upo.orchestrator.engine.InputValueResolver;
import com.upo.orchestrator.engine.Variable;
import com.upo.orchestrator.engine.models.ProcessInstance;
import com.upo.orchestrator.engine.utils.VariableUtils;
import com.upo.utilities.ds.Pair;
import com.upo.utilities.filter.api.Filter;
import com.upo.utilities.filter.impl.FilterBuilderRegistry;
import com.upo.utilities.filter.impl.FilterEvaluator;

/**
 * Factory for creating process-specific filter evaluators. Manages creation and caching of
 * evaluators for process filters.
 */
public class ProcessFilterEvaluatorFactory {

  /**
   * Creates a process-specific filter evaluator that can evaluate filters against process runtime
   * context.
   *
   * @param filter The filter definition to evaluate
   * @return ProcessFilterEvaluator configured for the given filter
   */
  public static FilterEvaluator<ProcessInstance> createEvaluator(
      Filter filter, InputValueResolver inputValueResolver) {
    ProcessFilterEvaluationContext context = new ProcessFilterEvaluationContext(inputValueResolver);
    FilterEvaluator<ProcessInstance> evaluator =
        FilterBuilderRegistry.getInstance().buildEvaluator(filter, context);
    return instance -> {
      Set<Pair<String, Variable.Type>> dependencies = context.getVariableDependencies();
      VariableUtils.loadMissingReferencedVariables(instance, dependencies);
      return evaluator.evaluate(instance);
    };
  }
}
