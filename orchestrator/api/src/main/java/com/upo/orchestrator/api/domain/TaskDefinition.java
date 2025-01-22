/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.api.domain;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.upo.utilities.filter.api.Filter;

/**
 * Defines a single unit of work within a process. A task represents an atomic operation that needs
 * to be performed as part of the larger process. Tasks are connected to other tasks through
 * transitions, forming the process flow.
 */
public interface TaskDefinition {

  /**
   * Returns the unique identifier of the task within its process. This ID is used to reference the
   * task in transitions and process execution.
   *
   * @return the task identifier
   */
  String getId();

  /**
   * Returns the scope identifier for this task. Tasks within the same execution scope (like
   * parallel tasks, or tasks within the same conditional block) share the same scope ID. This helps
   * in: - Grouping related tasks - Identifying task boundaries in conditional flows - Managing task
   * execution context
   *
   * <p>Example scopes: - Main process flow tasks share a scope - Tasks within an if-else block have
   * their own scope - Parallel tasks within a parallel block share a scope
   *
   * @return the scope identifier for this task
   */
  String getScopeId();

  /**
   * Returns configuration details needed to construct and execute this task at runtime.
   * Configuration details vary based on task type and operator
   *
   * @return configuration map for task runtime construction
   */
  TaskConfiguration getConfiguration();

  /**
   * Returns the JSON Schema definition for task inputs. The schema follows JSON Schema latest
   * specification and defines: - Required input fields - Data types and formats - Validation rules
   * and constraints - Nested object structures
   *
   * <p>Example schema: { "type": "object", "properties": { "orderId": { "type": "string" },
   * "amount": { "type": "number", "minimum": 0 } }, "required": ["orderId", "amount"] }
   *
   * @return JSON Schema definition for task inputs
   */
  Map<String, Object> getInputSchema();

  /**
   * Returns the JSON Schema definition for task outputs. The schema follows JSON Schema latest
   * specification and defines: - Expected output fields - Data types and formats - Output structure
   * and constraints - Complex type definitions
   *
   * <p>Example schema: { "type": "object", "properties": { "transactionId": { "type": "string" },
   * "status": { "type": "string", "enum": ["SUCCESS", "FAILURE"] } }, "required": ["transactionId",
   * "status"] }
   *
   * @return JSON Schema definition for task outputs
   */
  Map<String, Object> getOutputSchema();

  /**
   * @return JSON Schema definition for task errors
   */
  Map<String, Object> getErrorSchema();

  /**
   * Returns the operator type of this task. The operator determines how this task affects process
   * flow: - TASK: Regular task execution - CONDITIONAL: Conditional branching -
   * LOOP/BREAK/CONTINUE: Iterative execution - FORK/JOIN: Parallel execution - RETURN: Termination
   *
   * <p>Control flow operators (everything except TASK) might have special requirements or
   * behaviors: - IF requires a condition in its input schema - LOOP requires iteration control in
   * its output schema - FORK might specify number of parallel branches - JOIN might define
   * completion criteria
   *
   * @return the operator type for this task
   */
  TaskOperator getOperator();

  /**
   * Returns the input parameters required for this task's execution. The map contains parameter
   * names and their corresponding specifications. These inputs must be provided when the task is
   * executed.
   *
   * @return map of input parameter names to their specifications
   */
  Map<String, Object> getInput();

  /**
   * Returns the ordered list of transitions from this task. The order of transitions is significant
   * for conditional branching: - First matching transition in the list is taken - For regular
   * tasks, typically contains single transition - For conditional tasks, contains transitions in
   * evaluation order
   *
   * @return ordered list of transitions from this task
   */
  List<Transition> getNextTransitions();

  /**
   * Returns the condition that determines whether this task should be skipped. If the condition
   * evaluates to true, the task is skipped and execution proceeds to the next transition. If null
   * or evaluates to false, the task will be executed.
   *
   * <p>The skip condition can use: - Process variables - Previous task outputs - Runtime context
   *
   * <p>Example uses: - Skip retry task if max retries reached - Skip cleanup if no temporary
   * resources were created - Skip validation for certain data types - Skip task based on
   * configuration flags
   *
   * @return the skip condition, null if task should never be skipped
   */
  Optional<Filter> getSkipPredicate();
}
