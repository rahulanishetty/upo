/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine;

import java.util.Set;

import com.upo.orchestrator.engine.models.ProcessInstance;
import com.upo.utilities.ds.Pair;

/**
 * Represents a value that can be evaluated within a process context. This interface handles the
 * runtime evaluation of expressions and variable resolution.
 */
public interface ResolvableValue {

  /**
   * Evaluates the value within the given process context.
   *
   * @param context The process instance providing runtime context for evaluation
   * @return The evaluated result which can be: - String with resolved variables and executed
   *     expressions - Number from calculation - Boolean from condition - Map/List of resolved
   *     values
   */
  Object evaluate(ProcessInstance context);

  /**
   * Returns the set of variable references this value depends on for evaluation. This is used to
   * track dependencies between tasks and enable optimizations like: - Early variable resolution -
   * Dependency-based caching - Change detection - Task execution order optimization
   *
   * <p>The returned pairs contain: - taskId: The ID of the task containing the variable - type: The
   * specific variable type within the task (input/output/config) If type is null, it indicates the
   * entire task context is referenced
   *
   * <p>Example dependencies: - ("taskA", INPUT) -> taskA's input variables - ("taskB", OUTPUT) ->
   * taskB's output variables - ("taskC", null) -> all variables in taskC
   *
   * @return Set of task ID and variable type pairs that this value references
   */
  Set<Pair<String, Variable.Type>> getVariableDependencies();
}
