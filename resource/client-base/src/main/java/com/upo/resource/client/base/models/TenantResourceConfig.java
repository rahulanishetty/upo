/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.resource.client.base.models;

/**
 * Configuration for tenant-specific resource templates. Maps a tenant's resource to its underlying
 * infrastructure resource configuration.
 */
public abstract class TenantResourceConfig extends ResourceConfig {
  /**
   * Identifier of the tenant using this resource. Used to ensure proper tenant isolation in
   * templates.
   */
  private String tenantId;

  /**
   * References the base resource configuration ID. Format: resourceCategory/resourceCategory/suffix
   *
   * <p>Example: For a tenant's process instance redis template: - Template config:
   * resourceCategory: "REDIS_SERVER" resourceType: "PROCESS_INSTANCE" tenantId: "tenant-1" - Points
   * to server config via serverConfigId: serverConfigId: "REDIS_SERVER/REDIS_SERVER/server"
   *
   * <p>This links the tenant's PROCESS_INSTANCE template to the actual Redis server configuration
   * that should be used for client creation.
   */
  private String resourceIdSuffix;

  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  public String getResourceIdSuffix() {
    return resourceIdSuffix;
  }

  public void setResourceIdSuffix(String resourceIdSuffix) {
    this.resourceIdSuffix = resourceIdSuffix;
  }
}
