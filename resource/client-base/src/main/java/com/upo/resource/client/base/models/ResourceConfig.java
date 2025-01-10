/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.resource.client.base.models;

/**
 * Base configuration class for resource definitions. Supports two types of configurations: 1.
 * Server/Infrastructure Configuration (like connection properties) 2. Resource-specific
 * Configuration (like database names)
 */
public abstract class ResourceConfig {
  /**
   * Unique identifier for this resource configuration. Follows the format:
   * resourceCategory/resourceType/identifier Examples: Server Config:
   * REDIS_SERVER/REDIS_SERVER/redis-us-prod Resource Config:
   * REDIS_SERVER/PROCESS_INSTANCE/partitionKey
   */
  private String id;

  /**
   * Resource category identifier. For server configs: Identifies infrastructure (e.g.,
   * "REDIS_SERVER") For resource configs: Identifies parent infrastructure (e.g., "REDIS_SERVER")
   */
  private String resourceCategory;

  /**
   * Resource type identifier. For server configs: Same as category (e.g., "REDIS_SERVER") For
   * resource configs: Identifies resource (e.g., "PROCESS_INSTANCE", "PROCESS_VARIABLES")
   */
  private String resourceType;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getResourceCategory() {
    return resourceCategory;
  }

  public void setResourceCategory(String resourceCategory) {
    this.resourceCategory = resourceCategory;
  }

  public String getResourceType() {
    return resourceType;
  }

  public void setResourceType(String resourceType) {
    this.resourceType = resourceType;
  }
}
