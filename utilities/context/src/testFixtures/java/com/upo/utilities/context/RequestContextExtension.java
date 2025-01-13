/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.utilities.context;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Optional;

import org.junit.jupiter.api.extension.*;

/**
 * JUnit 5 extension to manage RequestContext scope for tests. Implements InvocationInterceptor to
 * properly handle ScopedValue binding.
 */
public class RequestContextExtension implements InvocationInterceptor {

  @Override
  public void interceptTestMethod(
      Invocation<Void> invocation,
      ReflectiveInvocationContext<Method> invocationContext,
      ExtensionContext extensionContext)
      throws Throwable {

    WithRequestContext annotation = getAnnotation(extensionContext);
    if (annotation == null) {
      invocation.proceed();
      return;
    }
    RequestContext requestContext = createRequestContext(annotation);

   // Run test method within the RequestContext scope
    RequestContext.callInContext(
        requestContext,
        () -> {
          try {
            invocation.proceed();
            return null;
          } catch (Exception e) {
            throw e;
          } catch (Throwable e) {
            throw new RuntimeException(e);
          }
        });
  }

  @Override
  public void interceptTestTemplateMethod(
      Invocation<Void> invocation,
      ReflectiveInvocationContext<Method> invocationContext,
      ExtensionContext extensionContext)
      throws Throwable {
    interceptTestMethod(invocation, invocationContext, extensionContext);
  }

  private WithRequestContext getAnnotation(ExtensionContext context) {
   // Try to get method-level annotation
    Optional<WithRequestContext> methodAnnotation =
        context.getTestMethod().map(method -> method.getAnnotation(WithRequestContext.class));

    return methodAnnotation.orElseGet(
        () ->
            context
                .getTestClass()
                .map(clazz -> clazz.getAnnotation(WithRequestContext.class))
                .orElse(null));

   // Fall back to class-level annotation
  }

  private WithRequestContext createDefaultAnnotation() {
    return new WithRequestContext() {
      @Override
      public String partitionKey() {
        return "1";
      }

      @Override
      public Class<? extends Annotation> annotationType() {
        return WithRequestContext.class;
      }
    };
  }

  private RequestContext createRequestContext(WithRequestContext annotation) {
    return new TestRequestContext(annotation.partitionKey());
  }
}
