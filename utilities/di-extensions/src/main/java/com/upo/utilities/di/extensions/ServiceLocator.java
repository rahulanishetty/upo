/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.utilities.di.extensions;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;

/**
 * The ServiceLocator interface provides a unified way to look up dependency-injected services
 * across different DI frameworks like Quarkus and Spring. It abstracts away framework-specific
 * lookup mechanisms and provides a consistent API for service discovery.
 */
public interface ServiceLocator {

  /** Get service by type */
  <T> T getService(Class<T> serviceType);

  /** Get service by type and qualifier name (e.g. @Named, @Qualifier) */
  <T> T getService(Class<T> serviceType, String qualifierName);

  /** Get all services of a given type */
  <T> List<T> getServices(Class<T> serviceType);

  /** Get service by type and annotation Supports @Qualifier, @Named, custom qualifiers */
  <T> T getService(Class<T> serviceType, Class<? extends Annotation> qualifierType);

  /** Get service by type and annotation instance Useful for qualifiers with attributes */
  <T> T getService(Class<T> serviceType, Annotation qualifier);

  /** Check if service of given type exists */
  boolean hasService(Class<?> serviceType);

  /** Check if service with specific qualifier exists */
  boolean hasService(Class<?> serviceType, String qualifierName);

  /** Get optional service by type Returns Optional.empty() if not found instead of throwing */
  <T> Optional<T> findService(Class<T> serviceType);

  /** Get optional service by type and qualifier */
  <T> Optional<T> findService(Class<T> serviceType, String qualifierName);
}
