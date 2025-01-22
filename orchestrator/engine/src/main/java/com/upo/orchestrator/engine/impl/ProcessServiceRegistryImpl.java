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

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Singleton
public class ProcessServiceRegistryImpl implements ProcessServiceRegistry {

  private final Map<ExecutionStrategy, ProcessServicesImpl> registry;
  private final ProcessServices coreRuntimeServices;

  @Inject
  public ProcessServiceRegistryImpl(
      @Named("coreRuntimeServicesImpl") ProcessServices coreRuntimeServices) {
    this.registry = new ConcurrentHashMap<>();
    this.coreRuntimeServices = coreRuntimeServices;
  }

  @Override
  public <T> void register(ExecutionStrategy strategy, Class<T> clz, T service) {
    Objects.requireNonNull(strategy, "strategy cannot be null");
    ProcessServicesImpl processServices =
        registry.computeIfAbsent(strategy, _ -> new ProcessServicesImpl(coreRuntimeServices));
    processServices.registerService(clz, service);
  }

  @Override
  public ProcessServices getServices(ExecutionStrategy strategy) {
    return Objects.requireNonNull(
        registry.get(strategy), "no process services found for strategy: " + strategy);
  }

  @Override
  public ProcessServices getCoreServices() {
    return coreRuntimeServices;
  }
}
