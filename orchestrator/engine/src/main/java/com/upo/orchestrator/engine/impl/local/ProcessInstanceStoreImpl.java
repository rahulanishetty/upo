/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.impl.local;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.upo.orchestrator.engine.ProcessFlowStatus;
import com.upo.orchestrator.engine.models.ProcessInstance;
import com.upo.orchestrator.engine.services.ProcessInstanceStore;
import com.upo.utilities.ds.CollectionUtils;

import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Named("LocalProcessInstanceStoreImpl")
@Singleton
public class ProcessInstanceStoreImpl implements ProcessInstanceStore {

  private final Map<String, ProcessInstance> processInstanceMap;
  private final Map<String, Set<String>> waitingOnInstancesMap;

  public ProcessInstanceStoreImpl() {
    this.processInstanceMap = new ConcurrentHashMap<>();
    this.waitingOnInstancesMap = new ConcurrentHashMap<>();
  }

  @Override
  public boolean save(ProcessInstance processInstance) {
    if (processInstance == null) {
      return false;
    }
    processInstanceMap.put(processInstance.getId(), new ProcessInstance(processInstance));
    return true;
  }

  @Override
  public boolean saveMany(Collection<ProcessInstance> processInstances) {
    if (CollectionUtils.isEmpty(processInstances)) {
      return false;
    }
    for (ProcessInstance processInstance : processInstances) {
      processInstanceMap.put(processInstance.getId(), new ProcessInstance(processInstance));
    }
    return true;
  }

  @Override
  public boolean save(ProcessInstance processInstance, ProcessFlowStatus expectedStatus) {
    return false;
  }

  @Override
  public Optional<ProcessInstance> findById(String id) {
    ProcessInstance processInstance = processInstanceMap.get(id);
    if (processInstance == null) {
      return Optional.empty();
    }
    return Optional.of(new ProcessInstance(processInstance));
  }

  @Override
  public boolean deleteById(String processInstanceId) {
    ProcessInstance removed = processInstanceMap.remove(processInstanceId);
    return removed != null;
  }

  @Override
  public Optional<ProcessInstance> findById(String id, ProcessFlowStatus expectedStatus) {
    ProcessInstance processInstance = processInstanceMap.get(id);
    if (processInstance == null) {
      return Optional.empty();
    }
    if (Objects.equals(processInstance.getStatus(), expectedStatus)) {
      return Optional.of(new ProcessInstance(processInstance));
    }
    return Optional.empty();
  }

  @Override
  public void addWaitingOnInstanceIds(
      ProcessInstance parentInstance, Collection<String> waitOnInstanceIds) {
    if (CollectionUtils.isEmpty(waitOnInstanceIds)) {
      return;
    }
    waitingOnInstancesMap
        .computeIfAbsent(
            parentInstance.getId(), _ -> Collections.newSetFromMap(new ConcurrentHashMap<>()))
        .addAll(waitOnInstanceIds);
  }

  @Override
  public boolean removeCompletedInstanceId(
      ProcessInstance parentInstance, String completedInstanceId) {
    Set<String> waitingOnInstanceIds = waitingOnInstancesMap.get(parentInstance.getId());
    if (CollectionUtils.isEmpty(waitingOnInstanceIds)) {
      return false;
    }
    return waitingOnInstanceIds.remove(completedInstanceId);
  }

  @Override
  public Set<String> getRemainingChildren(ProcessInstance processInstance) {
    Set<String> waitingOnInstanceIds = waitingOnInstancesMap.get(processInstance.getId());
    if (CollectionUtils.isEmpty(waitingOnInstanceIds)) {
      return Collections.emptySet();
    }
    return new HashSet<>(waitingOnInstanceIds);
  }
}
