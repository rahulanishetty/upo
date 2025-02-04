/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.impl.rt;

import java.util.Set;

import com.upo.orchestrator.engine.*;
import com.upo.orchestrator.engine.impl.ProcessFilterEvaluatorFactory;
import com.upo.orchestrator.engine.models.ProcessInstance;
import com.upo.orchestrator.engine.models.ProcessVariable;
import com.upo.utilities.ds.Pair;
import com.upo.utilities.filter.api.Filter;
import com.upo.utilities.filter.impl.FilterEvaluator;

/**
 * Base abstract class that provides core state management and configuration for task runtime
 * implementations. This class maintains the fundamental properties and settings required for task
 * execution but does not implement any execution logic.
 *
 * <p>This class is designed to be extended by more specific task runtime implementations that add
 * execution behavior and control flow logic.
 */
public abstract class AbstractTaskRuntime implements TaskRuntime {

  protected final ProcessRuntime parent;
  protected final String taskId;
  protected FilterEvaluator<ProcessInstance> skipCondition;
  protected ResolvableValue inputs;
  protected Set<Pair<String, Variable.Type>> dependencies;
  protected TransitionResolver outgoingTransitions;

  public AbstractTaskRuntime(ProcessRuntime parent, String taskId) {
    this.parent = parent;
    this.taskId = taskId;
  }

  @Override
  public String getTaskId() {
    return taskId;
  }

  public void setInputs(Object inputs) {
    this.inputs = getInputValueResolver().resolve(inputs);
    if (this.inputs == null) {
      this.dependencies = null;
    } else {
      this.dependencies = this.inputs.getVariableDependencies();
    }
  }

  public void setSkipCondition(Filter filter) {
    if (filter != null) {
      this.skipCondition =
          ProcessFilterEvaluatorFactory.createEvaluator(filter, getInputValueResolver());
    } else {
      this.skipCondition = null;
    }
  }

  public void setOutgoingTransitions(TransitionResolver outgoingTransitions) {
    this.outgoingTransitions = outgoingTransitions;
  }

  protected ProcessVariable toVariable(
      ProcessInstance processInstance, Variable.Type type, Object payload) {
    ProcessVariable processVariable = new ProcessVariable();
    processVariable.setPayload(payload);
    processVariable.setTaskId(taskId);
    processVariable.setType(type);
    if (processInstance != null) {
      processVariable.initId(processInstance);
    }
    return processVariable;
  }

  protected InputValueResolver getInputValueResolver() {
    return parent.getCoreServices().getService(InputValueResolver.class);
  }

  protected ProcessServices getServices(ProcessInstance processInstance) {
    return processInstance.getProcessEnv().getProcessServices();
  }
}
