/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.resource.client.base.models;

/**
 * Represents a category of resource infrastructure. Categories define the infrastructure level
 * configuration like: - Connection strings - Server endpoints - Authentication credentials -
 * Connection properties
 *
 * <p>Examples: - KAFKA_CLUSTER - MONGODB_CLUSTER - REDIS_CLUSTER
 */
public interface ResourceCategory {

  /**
   * Returns the unique identifier for this resource category.
   *
   * @return category identifier
   */
  String name();
}
