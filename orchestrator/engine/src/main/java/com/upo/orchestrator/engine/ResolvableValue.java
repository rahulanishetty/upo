/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine;

import com.upo.orchestrator.engine.models.ProcessInstance;

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
}
