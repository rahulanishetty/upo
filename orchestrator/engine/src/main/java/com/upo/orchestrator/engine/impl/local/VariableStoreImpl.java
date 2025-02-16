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

import com.upo.orchestrator.engine.Variable;
import com.upo.orchestrator.engine.models.ProcessInstance;
import com.upo.orchestrator.engine.models.ProcessVariable;
import com.upo.orchestrator.engine.services.VariableStore;
import com.upo.utilities.ds.CollectionUtils;
import com.upo.utilities.json.Utils;

import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Named("LocalVariableStoreImpl")
@Singleton
public class VariableStoreImpl implements VariableStore {

  private final Map<String, Set<String>> instanceIdVsVariableIdsMap;
  private final Map<String, ProcessVariable> processVariableMap;

  public VariableStoreImpl() {
    this.processVariableMap = new ConcurrentHashMap<>();
    this.instanceIdVsVariableIdsMap = new ConcurrentHashMap<>();
  }

  @Override
  public boolean save(ProcessVariable variable) {
    if (variable == null) {
      return false;
    }
    String variableId = Objects.requireNonNull(variable.getId(), "variable id should be populated");
    String processInstanceId =
        Objects.requireNonNull(
            variable.getProcessInstanceId(), "process instance id needs to be set");
    instanceIdVsVariableIdsMap
        .computeIfAbsent(
            processInstanceId, _ -> Collections.newSetFromMap(new ConcurrentHashMap<>()))
        .add(variableId);
    processVariableMap.put(variableId, copyVariable(variable));
    return true;
  }

  @Override
  public boolean saveMany(Collection<ProcessVariable> variables) {
    if (CollectionUtils.isEmpty(variables)) {
      return false;
    }
    for (ProcessVariable variable : variables) {
      save(variable);
    }
    return true;
  }

  @Override
  public Map<String, ProcessVariable> findByIds(Collection<String> ids) {
    if (CollectionUtils.isEmpty(ids)) {
      return Collections.emptyMap();
    }
    Map<String, ProcessVariable> result = new HashMap<>();
    for (String id : ids) {
      ProcessVariable variable = processVariableMap.get(id);
      if (variable == null) {
        continue;
      }
      result.put(id, copyVariable(variable));
    }
    return result;
  }

  @Override
  public Collection<Variable> findVariablesForInstance(ProcessInstance processInstance) {
    if (processInstance == null) {
      return Collections.emptyList();
    }
    Set<String> variableIds = instanceIdVsVariableIdsMap.get(processInstance.getId());
    if (CollectionUtils.isEmpty(variableIds)) {
      return Collections.emptyList();
    }
    List<Variable> variables = new ArrayList<>();
    for (String variableId : variableIds) {
      ProcessVariable variable = processVariableMap.get(variableId);
      if (variable == null) {
        continue;
      }
      variables.add(copyVariable(variable));
    }
    return variables;
  }

  @Override
  public void deleteProcessVariables(String processInstanceId) {
    Set<String> variableIds = instanceIdVsVariableIdsMap.remove(processInstanceId);
    if (CollectionUtils.isEmpty(variableIds)) {
      return;
    }
    for (String variableId : variableIds) {
      processVariableMap.remove(variableId);
    }
  }

  private ProcessVariable copyVariable(ProcessVariable processVariable) {
    ProcessVariable copy = new ProcessVariable();
    copy.setId(processVariable.getId());
    copy.setProcessInstanceId(processVariable.getProcessInstanceId());
    copy.setType(processVariable.getType());
    copy.setTaskId(processVariable.getTaskId());
    copy.setPayload(Utils.deepCopyViaJson(processVariable.getPayload(), Object.class));
    return copy;
  }
}
