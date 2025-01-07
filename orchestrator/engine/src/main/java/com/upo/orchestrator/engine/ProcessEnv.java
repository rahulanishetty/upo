/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine;

import java.util.Map;

/**
 * Represents the environment in which processes execute. Provides access to environment variables,
 * execution context, and process services needed for process execution. This environment is shared
 * across all process instances running in the same execution context.
 */
public class ProcessEnv {

  /**
   * Environment variables that are constant across process executions. These typically include: -
   * Configuration values - System properties - Deployment settings - Environment-specific constants
   */
  private Map<String, Object> env;

  /**
   * Execution context shared across process instances. Contains runtime information such as: -
   * Tenant information - Security context - Correlation IDs - Runtime metadata - Execution
   * properties
   */
  private Map<String, Object> context;

  /**
   * Session variables that are shared across the entire execution context. These variables provide
   * a global state mechanism for storing and sharing data between different processes and tasks
   * within the same runtime environment.
   *
   * <p>Key characteristics: - Mutable storage for runtime-level global variables - Shared across
   * all process instances in the current execution context - Typically used for: - Storing
   * transient, context-wide configuration - Caching intermediate results - Sharing state between
   * related processes
   *
   * <p>Lifecycle: Exists for the duration of the execution context and may be reset or cleared
   * between major process flows.
   *
   * <p>Thread Safety: Implementations should ensure thread-safe access and modification.
   *
   * @see ProcessEnv
   * @see ProcessServices
   */
  private Map<String, Object> session;

  /**
   * Services required for process execution. Provides access to infrastructure services like: -
   * Process instance storage - Variable management These services are configured based on the
   * execution strategy (local, distributed, etc.).
   */
  private ProcessServices processServices;

  public Map<String, Object> getEnv() {
    return env;
  }

  public void setEnv(Map<String, Object> env) {
    this.env = env;
  }

  public Map<String, Object> getContext() {
    return context;
  }

  public void setContext(Map<String, Object> context) {
    this.context = context;
  }

  public Map<String, Object> getSession() {
    return session;
  }

  public void setSession(Map<String, Object> session) {
    this.session = session;
  }

  public ProcessServices getProcessServices() {
    return processServices;
  }

  public void setProcessServices(ProcessServices processServices) {
    this.processServices = processServices;
  }
}
