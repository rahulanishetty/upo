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
  private Map<String, Object> envVariables;

  /**
   * Execution context shared across process instances. Contains runtime information such as: -
   * Tenant information - Security context - Correlation IDs - Runtime metadata - Execution
   * properties
   */
  private Map<String, Object> context;

  /**
   * Services required for process execution. Provides access to infrastructure services like: -
   * Process instance storage - Variable management These services are configured based on the
   * execution strategy (local, distributed, etc.).
   */
  private ProcessServices processServices;

  public Map<String, Object> getEnvVariables() {
    return envVariables;
  }

  public void setEnvVariables(Map<String, Object> envVariables) {
    this.envVariables = envVariables;
  }

  public Map<String, Object> getContext() {
    return context;
  }

  public void setContext(Map<String, Object> context) {
    this.context = context;
  }

  public ProcessServices getProcessServices() {
    return processServices;
  }

  public void setProcessServices(ProcessServices processServices) {
    this.processServices = processServices;
  }
}
