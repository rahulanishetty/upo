/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.resource.client.base;

import java.io.Closeable;
import java.util.Optional;

import com.upo.resource.client.base.models.ResourceCategory;

/**
 * Factory for creating and managing resource clients. Provides access to infrastructure-level
 * clients (like database connections).
 *
 * @param <Client> type of client this factory manages
 */
public interface ResourceClientFactory<Client extends Closeable> extends Closeable {

  /**
   * Returns a client for the specified resource category and resource ID. Clients are typically
   * cached and reused for the same category/resource combination to avoid resource overhead.
   *
   * <p>Example usages: - MongoDB: getClient(MongoCategory.PROD_CLUSTER, "orders-db") - Redis:
   * getClient(RedisCategory.CACHE_CLUSTER, "user-cache") - Kafka:
   * getClient(KafkaCategory.EVENT_CLUSTER, "notification-broker")
   *
   * @param resourceCategory category defining infrastructure configuration
   * @param resourceId identifier for the specific resource (e.g., database name, cluster ID)
   * @return configured client instance
   * @throws ResourceNotFoundException if client creation fails
   */
  Client getClientOrFail(ResourceCategory resourceCategory, String resourceId);

  /**
   * Returns a client for the specified resource category and resource ID. Clients are typically
   * cached and reused for the same category/resource combination to avoid resource overhead.
   *
   * <p>Example usages: - MongoDB: getClient(MongoCategory.PROD_CLUSTER, "orders-db") - Redis:
   * getClient(RedisCategory.CACHE_CLUSTER, "user-cache") - Kafka:
   * getClient(KafkaCategory.EVENT_CLUSTER, "notification-broker")
   *
   * @param resourceCategory category defining infrastructure configuration
   * @param resourceId identifier for the specific resource (e.g., database name, cluster ID)
   * @return configured client instance
   */
  Optional<Client> getClient(ResourceCategory resourceCategory, String resourceId);
}
