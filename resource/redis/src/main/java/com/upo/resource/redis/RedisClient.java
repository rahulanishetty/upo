/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.resource.redis;

import static com.upo.resource.redis.Utils.*;

import java.io.Closeable;
import java.util.List;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import com.upo.resource.redis.models.RedisServerConfig;
import com.upo.utilities.ds.IOUtils;

import io.lettuce.core.ReadFrom;
import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.masterreplica.MasterReplica;
import io.lettuce.core.masterreplica.StatefulRedisMasterReplicaConnection;
import io.lettuce.core.support.ConnectionPoolSupport;

/**
 * Represents a Redis client with connection management capabilities.
 *
 * <h2>Supported Client Types</h2>
 *
 * <ul>
 *   <li>Standalone Redis servers
 *   <li>Redis Sentinel configurations
 *   <li>Redis Cluster deployments
 * </ul>
 *
 * <h2>Resource Management</h2>
 *
 * <ul>
 *   <li>Implements {@link Closeable} for proper resource cleanup
 *   <li>Utilizes connection pooling for efficient resource utilization
 *   <li>Supports automatic connection management
 * </ul>
 */
public interface RedisClient extends Closeable {

  /**
   * Retrieves a set of Redis commands for executing operations.
   *
   * <h2>Connection Behavior</h2>
   *
   * <ul>
   *   <li>Borrows a connection from the connection pool
   *   <li>Creates a proxy for executing Redis commands
   *   <li>Automatically manages connection lifecycle
   * </ul>
   *
   * <h2>Error Handling</h2>
   *
   * <ul>
   *   <li>Throws {@link RuntimeException} if connection borrowing fails
   *   <li>Provides transparent connection management
   * </ul>
   *
   * @return A set of executable Redis commands
   * @throws RuntimeException if connection cannot be borrowed
   */
  RedisCommands getRedisCommands();

  /**
   * Static factory method for creating a Redis client based on server configuration.
   *
   * <h2>Client Creation Strategy</h2>
   *
   * <ul>
   *   <li>Supports multiple Redis deployment architectures
   *   <li>Dynamically creates appropriate client based on configuration
   *   <li>Configures read preferences and connection pooling
   * </ul>
   *
   * <h2>Supported Configurations</h2>
   *
   * <ul>
   *   <li>Cluster mode
   *   <li>Standalone server
   *   <li>Sentinel configuration
   * </ul>
   *
   * @param config Redis server configuration
   * @return Configured {@link RedisClient} instance
   */
  static RedisClient create(RedisServerConfig config) {
    ReadFrom readFrom = null;
    if (config.getReadPreference() != null) {
      readFrom = ReadFrom.valueOf(config.getReadPreference());
    }
    GenericObjectPoolConfig<?> poolConfig = createPoolConfig(config);
    return switch (config.getClientType()) {
      case CLUSTER -> {
        List<RedisURI> redisURIS = createRedisURIForCluster(config);
        var redisClient = RedisClusterClient.create(createClientResources(), redisURIS);
        yield from(redisClient, readFrom, poolConfig);
      }
      case STANDALONE, SENTINEL -> {
        RedisURI redisURI = createRedisURI(config);
        var redisClient = io.lettuce.core.RedisClient.create(createClientResources(), redisURI);
        yield from(redisClient, redisURI, readFrom, poolConfig);
      }
    };
  }

  /**
   * Internal factory method for creating a Redis client from a cluster client.
   *
   * <h2>Connection Pool Characteristics</h2>
   *
   * <ul>
   *   <li>Creates a connection pool for cluster connections
   *   <li>Supports custom read preferences
   *   <li>Provides automatic connection management
   * </ul>
   *
   * <h2>Lifecycle Management</h2>
   *
   * <ul>
   *   <li>Borrows and returns connections from pool
   *   <li>Handles connection shutdown
   * </ul>
   *
   * @param client Redis cluster client
   * @param readFrom Read preference configuration
   * @param poolConfig Connection pool configuration
   * @return Configured {@link RedisClient} instance
   */
  private static RedisClient from(
      RedisClusterClient client, ReadFrom readFrom, GenericObjectPoolConfig<?> poolConfig) {
    return new RedisClient() {
      private final GenericObjectPool<StatefulRedisClusterConnection<String, String>>
          connectionPool = createConnectionPool(client, readFrom, poolConfig);

      @Override
      public RedisCommands getRedisCommands() {
        try {
          var connection = connectionPool.borrowObject();
          return RedisCommands.from(connection, StatefulRedisClusterConnection::sync);
        } catch (Exception e) {
          throw new RuntimeException("failed to borrow connection from connection pool", e);
        }
      }

      @Override
      public void close() {
        IOUtils.closeQuietly(connectionPool, client, client::shutdown);
      }

      private static GenericObjectPool<StatefulRedisClusterConnection<String, String>>
          createConnectionPool(
              RedisClusterClient client, ReadFrom readFrom, GenericObjectPoolConfig<?> poolConfig) {
       //noinspection unchecked
        return ConnectionPoolSupport.createGenericObjectPool(
            () -> {
              var connection = client.connect();
              if (readFrom != null) {
                connection.setReadFrom(readFrom);
              }
              return connection;
            },
            (GenericObjectPoolConfig<StatefulRedisClusterConnection<String, String>>) poolConfig);
      }
    };
  }

  /**
   * Internal factory method for creating a Redis client from a standard Redis client.
   *
   * <h2>Connection Pool Characteristics</h2>
   *
   * <ul>
   *   <li>Creates a connection pool for master-replica connections
   *   <li>Supports custom read preferences
   *   <li>Provides automatic connection management
   * </ul>
   *
   * <h2>Lifecycle Management</h2>
   *
   * <ul>
   *   <li>Borrows and returns connections from pool
   *   <li>Handles connection shutdown
   * </ul>
   *
   * @param client Standard Redis client
   * @param redisURI Redis URI configuration
   * @param readFrom Read preference configuration
   * @param poolConfig Connection pool configuration
   * @return Configured {@link RedisClient} instance
   */
  private static RedisClient from(
      io.lettuce.core.RedisClient client,
      RedisURI redisURI,
      ReadFrom readFrom,
      GenericObjectPoolConfig<?> poolConfig) {
    return new RedisClient() {

      private final GenericObjectPool<StatefulRedisMasterReplicaConnection<String, String>>
          connectionPool = createConnectionPool(client, redisURI, readFrom, poolConfig);

      @Override
      public RedisCommands getRedisCommands() {
        try {
          var connection = connectionPool.borrowObject();
          return RedisCommands.from(connection, StatefulRedisMasterReplicaConnection::sync);
        } catch (Exception e) {
          throw new RuntimeException("failed to borrow connection from pool", e);
        }
      }

      @Override
      public void close() {
        IOUtils.closeQuietly(connectionPool, client, client::shutdown);
      }

      private static GenericObjectPool<StatefulRedisMasterReplicaConnection<String, String>>
          createConnectionPool(
              io.lettuce.core.RedisClient client,
              RedisURI redisURI,
              ReadFrom readFrom,
              GenericObjectPoolConfig<?> poolConfig) {
       //noinspection unchecked
        return ConnectionPoolSupport.createGenericObjectPool(
            () -> {
              var connection = MasterReplica.connect(client, StringCodec.UTF8, redisURI);
              if (readFrom != null) {
                connection.setReadFrom(readFrom);
              }
              return connection;
            },
            (GenericObjectPoolConfig<StatefulRedisMasterReplicaConnection<String, String>>)
                poolConfig);
      }
    };
  }
}
