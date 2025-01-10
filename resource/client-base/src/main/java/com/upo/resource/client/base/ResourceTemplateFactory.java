/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.resource.client.base;

import java.util.Optional;

import com.upo.resource.client.base.models.ResourceType;

/**
 * Factory for creating and managing resource templates. Templates provide higher-level abstractions
 * over resource clients (like database templates) with proper multi-tenancy support.
 *
 * @param <Template> type of template this factory manages
 */
public interface ResourceTemplateFactory<Template> {

  /**
   * Returns a template for the specified resource type and partition. Templates are typically
   * cached and reused for the same type/partition combination to avoid resource overhead.
   *
   * @param resourceType type defining logical resource partition
   * @param partitionKey identifier for the partition requesting the template
   * @return configured template instance
   * @throws ResourceNotFoundException if template creation/retrieval fails
   */
  Template getTemplateOrFail(ResourceType resourceType, String partitionKey);

  /**
   * Returns a template for the specified resource type and partition. Templates are typically
   * cached and reused for the same type/partition combination to avoid resource overhead.
   *
   * @param resourceType type defining logical resource partition
   * @param partitionKey identifier for the partition requesting the template
   * @return configured template instance
   */
  Optional<Template> getTemplate(ResourceType resourceType, String partitionKey);
}
