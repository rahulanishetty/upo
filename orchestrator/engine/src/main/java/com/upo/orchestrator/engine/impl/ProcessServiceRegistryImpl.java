/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.impl;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import com.upo.orchestrator.engine.ExecutionStrategy;
import com.upo.orchestrator.engine.ProcessServiceRegistry;
import com.upo.orchestrator.engine.ProcessServices;

public class ProcessServiceRegistryImpl implements ProcessServiceRegistry {

  private final Map<ExecutionStrategy, ProcessServices> registry;

  public ProcessServiceRegistryImpl() {
    this.registry = new ConcurrentHashMap<>();
  }

  @Override
  public void register(ExecutionStrategy strategy, ProcessServices processServices) {
    Objects.requireNonNull(strategy, "strategy cannot be null");
    ProcessServices existing =
        this.registry.putIfAbsent(
            strategy, Objects.requireNonNull(processServices, "processServices cannot be null"));
    if (existing != null) {
      throw new IllegalArgumentException(
          "duplicate process services for strategy : " + strategy.name());
    }
  }

  @Override
  public ProcessServices getServices(ExecutionStrategy strategy) {
    return Objects.requireNonNull(
        registry.get(strategy), "no process services found for strategy: " + strategy);
  }
}
