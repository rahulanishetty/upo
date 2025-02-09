/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.impl;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import com.upo.orchestrator.api.domain.ProcessDefinition;
import com.upo.orchestrator.api.domain.TaskDefinition;
import com.upo.orchestrator.engine.*;
import com.upo.orchestrator.engine.services.ProcessServiceRegistry;
import com.upo.orchestrator.engine.services.ProcessServices;
import com.upo.utilities.filter.api.Filter;
import com.upo.utilities.filter.impl.FilterBuilderRegistry;
import com.upo.utilities.filter.impl.FilterEvaluator;

public class ProcessRuntimeImpl implements ProcessRuntime {

  private final ProcessDetails processDetails;
  private final ProcessDefinition processDefinition;
  private final FilterEvaluator<Map<String, Object>> processPredicate;
  private final ConcurrentHashMap<String, TaskRuntime> taskRuntimes;
  private final ProcessServiceRegistry processServiceRegistry;

  public ProcessRuntimeImpl(
      ProcessDetails processDetails,
      ProcessDefinition processDefinition,
      ProcessServiceRegistry processServiceRegistry) {
    this.processDetails = processDetails;
    this.processDefinition = processDefinition;
    this.processPredicate = createProcessPredicate(processDefinition);
    this.processServiceRegistry = processServiceRegistry;
    this.taskRuntimes = new ConcurrentHashMap<>();
  }

  @Override
  public ProcessDetails getDetails() {
    return processDetails;
  }

  @Override
  public ProcessDefinition getDefinition() {
    return processDefinition;
  }

  @Override
  public Optional<FilterEvaluator<Map<String, Object>>> getPredicate() {
    return Optional.ofNullable(processPredicate);
  }

  @Override
  public TaskRuntime getOrCreateTaskRuntime(String taskId) {
    return taskRuntimes.computeIfAbsent(
        taskId,
        new Function<String, TaskRuntime>() {
          @Override
          public TaskRuntime apply(String taskId) {
            TaskDefinition taskDefinition = processDefinition.getTaskDefinitions().get(taskId);
            if (taskDefinition == null) {
              throw new IllegalArgumentException(
                  "no task found for taskId: "
                      + taskId
                      + ", in process: "
                      + processDefinition.getId());
            }
            return null;
          }
        });
  }

  @Override
  public ProcessExecutor createExecutor(ExecutionStrategy strategy) {
    ProcessServices processServices = processServiceRegistry.getServices(strategy);
    return new ProcessExecutorImpl(processServices, this, strategy);
  }

  @Override
  public ProcessServices getCoreServices() {
    return processServiceRegistry.getCoreServices();
  }

  private FilterEvaluator<Map<String, Object>> createProcessPredicate(
      ProcessDefinition processDefinition) {
    Optional<Filter> predicate = processDefinition.getPredicate();
    if (predicate.isEmpty()) {
      return null;
    }
    Filter filter = predicate.get();
    return FilterBuilderRegistry.getInstance()
        .buildEvaluator(
            filter,
            field -> {
              throw new UnsupportedOperationException("TODO Implement this");
            });
  }
}
