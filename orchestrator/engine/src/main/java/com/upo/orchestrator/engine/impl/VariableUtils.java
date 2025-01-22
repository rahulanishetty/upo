/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.impl;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.upo.orchestrator.engine.Variable;
import com.upo.orchestrator.engine.VariableContainer;
import com.upo.orchestrator.engine.models.ProcessInstance;
import com.upo.orchestrator.engine.models.ProcessVariable;
import com.upo.orchestrator.engine.services.VariableStore;
import com.upo.utilities.ds.CollectionUtils;
import com.upo.utilities.ds.Pair;

public class VariableUtils {

  /**
   * Loads missing referenced variables from persistent storage into the process instance's memory.
   *
   * <p>This method checks if all variables required by the current operation (tracked in
   * dependencies) are available in the process instance's variable container. If any required
   * variables are missing, it loads them from the persistent variable store.
   *
   * @param processInstance The current process instance requiring variable access
   */
  public static void loadMissingReferencedVariables(
      ProcessInstance processInstance, Set<Pair<String, Variable.Type>> dependencies) {
    if (CollectionUtils.isEmpty(dependencies)) {
      return;
    }

    VariableContainer variableContainer = processInstance.getVariableContainer();
    Set<String> missingVariableIds = new HashSet<>();

   // Identify which required variables are missing from memory
    for (Pair<String, Variable.Type> dependency : dependencies) {
      boolean present =
          variableContainer.containsVariable(
              dependency.getFirstElement(), dependency.getSecondElement());
      if (present) {
        continue;
      }
      missingVariableIds.add(
          ProcessVariable.getId(
              processInstance, dependency.getFirstElement(), dependency.getSecondElement()));
    }

   // Bulk load missing variables from persistent store
    if (CollectionUtils.isNotEmpty(missingVariableIds)) {
      VariableStore variableStore =
          processInstance.getProcessEnv().getProcessServices().getService(VariableStore.class);
      Map<String, ProcessVariable> loadedVariables = variableStore.findByIds(missingVariableIds);

     // Restore loaded variables into process instance memory
      if (CollectionUtils.isNotEmpty(loadedVariables)) {
        for (ProcessVariable variable : loadedVariables.values()) {
          variableContainer.restoreVariable(
              variable.getTaskId(), variable.getType(), variable.getPayload());
        }
      }
    }
  }
}
