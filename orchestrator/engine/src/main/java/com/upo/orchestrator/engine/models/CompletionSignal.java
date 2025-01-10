/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.models;

import java.util.Map;

/**
 * Contains information about task completion in terminal state. Represents both successful and
 * failed completion scenarios.
 */
public class CompletionSignal {

  /** Type of completion outcome */
  public enum Type {
    /** Task completed successfully */
    SUCCESS,

    /** Task failed with error */
    FAILURE,

    /** Task was explicitly terminated */
    TERMINATED
  }

  private Type type;
  private Map<String, Object> details;
  private String reason;// optional description/message

  public CompletionSignal() {}

  public CompletionSignal(Type type, Map<String, Object> details, String reason) {
    this.type = type;
    this.details = details;
    this.reason = reason;
  }

  /** Static factory methods for common scenarios */
  public static CompletionSignal success() {
    return new CompletionSignal(Type.SUCCESS, null, null);
  }

  public static CompletionSignal success(Map<String, Object> details) {
    return new CompletionSignal(Type.SUCCESS, details, null);
  }

  public static CompletionSignal failure(String reason) {
    return new CompletionSignal(Type.FAILURE, null, reason);
  }

  public static CompletionSignal failure(String reason, Map<String, Object> errorDetails) {
    return new CompletionSignal(Type.FAILURE, errorDetails, reason);
  }

  public static CompletionSignal terminated(String reason) {
    return new CompletionSignal(Type.TERMINATED, null, reason);
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  public Map<String, Object> getDetails() {
    return details;
  }

  public void setDetails(Map<String, Object> details) {
    this.details = details;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }
}
