/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.resource.client.base.models;

/**
 * Represents a logical partition within a resource category. Types define the logical separation of
 * data within an infrastructure like: - Database names - Kafka topic prefixes - Redis key prefixes
 *
 * <p>Examples: - PROCESS_INSTANCE - PROCESS_VARIABLES - SIGNALS
 */
public interface ResourceType {
  /**
   * Returns the unique identifier for this resource type.
   *
   * @return type identifier
   */
  String name();
}
