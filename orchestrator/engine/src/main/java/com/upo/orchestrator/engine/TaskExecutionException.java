/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine;

public class TaskExecutionException extends RuntimeException {

  public TaskExecutionException(String message) {
    super(message);
  }

  public TaskExecutionException(String message, Throwable cause) {
    super(message, cause);
  }
}
