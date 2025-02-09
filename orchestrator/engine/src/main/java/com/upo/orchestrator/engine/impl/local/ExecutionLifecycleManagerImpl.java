/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.impl.local;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.upo.orchestrator.engine.impl.AbstractExecutionLifecycleManager;
import com.upo.orchestrator.engine.impl.events.LifecycleEvent;
import com.upo.orchestrator.engine.services.LifecycleEventHandler;
import com.upo.utilities.context.RequestContext;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

/**
 * Local implementation of ExecutionLifecycleManager that handles events in virtual threads.
 * Executes lifecycle events asynchronously while maintaining request context propagation.
 */
@Named("LocalExecutionLifecycleManagerImpl")
@Singleton
public class ExecutionLifecycleManagerImpl extends AbstractExecutionLifecycleManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionLifecycleManagerImpl.class);

  private final Executor executor;
  private final LifecycleEventHandler lifecycleEventHandler;

  @Inject
  public ExecutionLifecycleManagerImpl(LifecycleEventHandler lifecycleEventHandler) {
    this.executor = Executors.newThreadPerTaskExecutor(createVirtualThreadFactory());
    this.lifecycleEventHandler = lifecycleEventHandler;
  }

  /**
   * Handles lifecycle events asynchronously in virtual threads. Maintains request context across
   * thread boundaries.
   *
   * @param lifecycleEvent Event to be handled
   */
  @Override
  protected void handleEvent(LifecycleEvent lifecycleEvent) {
    RequestContext requestContext = cloneContext();
    this.executor.execute(
        () ->
            RequestContext.executeInContext(
                requestContext, () -> lifecycleEventHandler.handle(lifecycleEvent)));
  }

  /** Creates a copy of the current request context for thread propagation. */
  private static RequestContext cloneContext() {
    RequestContext requestContext = RequestContext.get();
    if (requestContext == null) {
      return null;
    }
    return requestContext.copy();
  }

  /** Creates a virtual thread factory with naming and error handling. */
  private static ThreadFactory createVirtualThreadFactory() {
    return Thread.ofVirtual()
        .name("local-execution-manager-")
        .uncaughtExceptionHandler(
            (t, e) -> LOGGER.error("uncaught exception in thread: {}", t.getName(), e))
        .factory();
  }
}
