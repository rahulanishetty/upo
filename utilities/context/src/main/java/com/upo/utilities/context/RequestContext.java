/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.utilities.context;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * Represents the context of a current request during process execution. Provides access to
 * request-specific data that needs to be available throughout the execution lifecycle.
 */
@SuppressWarnings("preview")
public interface RequestContext {

  /** Scoped value to store request context for the current execution scope. */
  ScopedValue<RequestContext> CONTEXT = ScopedValue.newInstance();

  /**
   * this can typically be tenantId
   *
   * @return partition key for current context
   */
  String getPartitionKey();

  /**
   * Creates a deep copy of this request context. A new context is created with copied values to
   * maintain isolation between different execution scopes.
   *
   * @return A new RequestContext instance with copied values
   */
  RequestContext copy();

  /**
   * Converts the request context to a map representation. This map typically includes: - Request
   * headers - Security context - Correlation IDs - Tenant information - Request metadata - User
   * context
   *
   * @return map containing all request context data
   */
  Map<String, Object> toMap();

  /**
   * Returns the current request context. This is typically bound to the current thread or execution
   * scope.
   *
   * @return current request context
   */
  static RequestContext get() {
    if (CONTEXT.isBound()) {
      return CONTEXT.get();
    }
    return null;
  }

  /**
   * Executes the given runnable with the specified request context. The context is only available
   * within the scope of the runnable execution.
   *
   * @param ctx request context to use
   * @param runnable code to execute within the context
   */
  static void executeInContext(RequestContext ctx, Runnable runnable) {
    ScopedValue.runWhere(CONTEXT, ctx, runnable);
  }

  /**
   * Executes the given callable with the specified request context and returns its result. The
   * context is only available within the scope of the callable execution.
   *
   * @param ctx request context to use
   * @param callable code to execute within the context
   * @return result of the callable execution
   * @throws Exception if callable throws an exception
   */
  static <T> T callInContext(RequestContext ctx, Callable<T> callable) throws Exception {
    return ScopedValue.callWhere(CONTEXT, ctx, callable);
  }

  /**
   * Executes the given supplier with the specified request context and returns its result. The
   * context is only available within the scope of the supplier execution. Similar to callInContext
   * but for suppliers that don't throw checked exceptions.
   *
   * @param ctx request context to use
   * @param callable supplier to execute within the context
   * @return result of the supplier execution
   */
  static <T> T getInContext(RequestContext ctx, Supplier<T> callable) {
    return ScopedValue.getWhere(CONTEXT, ctx, callable);
  }
}
