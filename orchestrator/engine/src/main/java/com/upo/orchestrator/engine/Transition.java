/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine;

import java.util.Optional;
import java.util.function.Supplier;

import com.upo.orchestrator.api.domain.TransitionType;
import com.upo.orchestrator.engine.models.ProcessInstance;
import com.upo.utilities.filter.impl.FilterEvaluator;

public interface Transition {

  TransitionType getType();

  TaskRuntime getNextTaskRuntime();

  Optional<FilterEvaluator<ProcessInstance>> getPredicate();

  static Transition defaultTransition(Supplier<TaskRuntime> nextRuntime) {
    return new Transition() {
      @Override
      public TransitionType getType() {
        return TransitionType.DEFAULT;
      }

      @Override
      public TaskRuntime getNextTaskRuntime() {
        return nextRuntime.get();
      }

      @Override
      public Optional<FilterEvaluator<ProcessInstance>> getPredicate() {
        return Optional.empty();
      }
    };
  }
}
