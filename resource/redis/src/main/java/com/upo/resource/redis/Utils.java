/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.resource.redis;

import static com.upo.resource.redis.models.RedisServerConfig.ClientType.STANDALONE;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import com.upo.resource.redis.models.Credentials;
import com.upo.resource.redis.models.HostPort;
import com.upo.resource.redis.models.RedisServerConfig;

import io.lettuce.core.RedisURI;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;

/** Utility class providing helper methods for Redis configuration and connection management. */
public class Utils {

  /**
   * Creates client resources with virtual thread support.
   *
   * @return Configured {@link ClientResources} instance
   */
  public static ClientResources createClientResources() {
    DefaultClientResources.Builder builder = DefaultClientResources.builder();
    return builder
        .threadFactoryProvider(poolName -> Thread.ofVirtual().name(poolName).factory())
        .build();
  }

  /**
   * Creates a generic object pool configuration based on Redis server configuration.
   *
   * <h2>Pool Configuration Parameters</h2>
   *
   * <ul>
   *   <li>Maximum total connections
   *   <li>Time between eviction runs
   *   <li>Maximum wait time for connection
   * </ul>
   *
   * @param config Redis server configuration
   * @return Configured {@link GenericObjectPoolConfig}
   */
  public static GenericObjectPoolConfig<?> createPoolConfig(RedisServerConfig config) {
    var poolConfig = new GenericObjectPoolConfig<>();
    if (config.getMaxPoolSize() != null) {
      poolConfig.setMaxTotal(config.getMaxPoolSize());
    }
    if (config.getPoolCleanerInterval() != null) {
      poolConfig.setTimeBetweenEvictionRuns(Duration.ofSeconds(config.getPoolCleanerInterval()));
    } else {
      poolConfig.setTimeBetweenEvictionRuns(Duration.ofSeconds(30));
    }
    if (config.getPoolMaxWaitTime() != null) {
      poolConfig.setMaxWait(Duration.ofSeconds(config.getPoolMaxWaitTime()));
    } else {
      poolConfig.setMaxWait(Duration.ofSeconds(5));
    }
    return poolConfig;
  }

  /**
   * Creates Redis URIs for cluster configuration.
   *
   * <h2>Conversion Process</h2>
   *
   * <ul>
   *   <li>Converts host and port configurations to Redis URIs
   *   <li>Validates presence of host and port configurations
   * </ul>
   *
   * @param config Redis server configuration
   * @return List of {@link RedisURI} for cluster configuration
   * @throws NullPointerException if host ports are not configured
   */
  public static List<RedisURI> createRedisURIForCluster(RedisServerConfig config) {
    List<RedisURI> redisURIS = new ArrayList<>();
    for (HostPort hostPort :
        Objects.requireNonNull(config.getHostPorts(), "host & ports are required")) {
      redisURIS.add(createRedisURI(config, STANDALONE, Collections.singletonList(hostPort)));
    }
    return redisURIS;
  }

  /**
   * Creates a Redis URI based on server configuration.
   *
   * <h2>Configuration Mapping</h2>
   *
   * <ul>
   *   <li>Converts server configuration to Redis URI
   *   <li>Handles different client types (STANDALONE, SENTINEL)
   * </ul>
   *
   * @param config Redis server configuration
   * @return Configured {@link RedisURI}
   */
  public static RedisURI createRedisURI(RedisServerConfig config) {
    List<HostPort> hostPorts = config.getHostPorts();
    RedisServerConfig.ClientType clientType = config.getClientType();
    return createRedisURI(config, clientType, hostPorts);
  }

  /**
   * Internal method to create Redis URI with comprehensive configuration.
   *
   * <h2>Configuration Steps</h2>
   *
   * <ul>
   *   <li>Sets client name
   *   <li>Configures host and ports based on client type
   *   <li>Applies SSL settings
   *   <li>Adds authentication credentials
   * </ul>
   *
   * @param config Redis server configuration
   * @param clientType Type of Redis client
   * @param hostPorts List of host and port configurations
   * @return Fully configured {@link RedisURI}
   * @throws IllegalArgumentException for invalid client types
   */
  private static RedisURI createRedisURI(
      RedisServerConfig config, RedisServerConfig.ClientType clientType, List<HostPort> hostPorts) {
    RedisURI.Builder builder = RedisURI.builder().withClientName(config.getId());
    builder =
        switch (clientType) {
          case STANDALONE -> addStandaloneHostAndPort(builder, hostPorts, config);
          case SENTINEL -> addSentinelHostAndPorts(builder, hostPorts, config);
          default -> throw new IllegalArgumentException("Invalid client type:" + clientType);
        };
    builder =
        builder.withSsl(config.isSsl()).withVerifyPeer(!config.isDisableHostNameVerification());
    builder = decorateWithCredentials(builder, config.getCredentials());
    return builder.build();
  }

  /**
   * Adds authentication credentials to Redis URI builder.
   *
   * <h2>Credential Handling</h2>
   *
   * <ul>
   *   <li>Supports STATIC credential type
   *   <li>Throws exception for unsupported credential types
   * </ul>
   *
   * @param builder Redis URI builder
   * @param credentials Authentication credentials
   * @return Credentials-decorated URI builder
   * @throws UnsupportedOperationException for unsupported credential types
   */
  private static RedisURI.Builder decorateWithCredentials(
      RedisURI.Builder builder, Credentials credentials) {
    if (credentials == null) {
      return builder;
    }
   //noinspection SwitchStatementWithTooFewBranches
    return switch (credentials.getType()) {
      case "STATIC" ->
          builder.withAuthentication(credentials.getUserName(), credentials.getPassword());
      default ->
          throw new UnsupportedOperationException(
              "Add support for credentials of type: " + credentials.getType());
    };
  }

  /**
   * Configures Redis URI builder with Sentinel host and ports.
   *
   * <h2>Sentinel Configuration</h2>
   *
   * <ul>
   *   <li>Adds multiple sentinel hosts and ports
   *   <li>Requires Sentinel master ID
   * </ul>
   *
   * @param builder Redis URI builder
   * @param hostPorts List of Sentinel host and port configurations
   * @param config Redis server configuration
   * @return Configured Sentinel URI builder
   * @throws NullPointerException if host ports or master ID are not configured
   */
  private static RedisURI.Builder addSentinelHostAndPorts(
      RedisURI.Builder builder, List<HostPort> hostPorts, RedisServerConfig config) {
    for (HostPort host : Objects.requireNonNull(hostPorts, "host & ports are required")) {
      builder = builder.withSentinel(host.getHost(), host.getPort());
    }
    builder.withSentinelMasterId(
        Objects.requireNonNull(config.getSentinelMasterId(), "sentinel masterId is required"));
    return builder;
  }

  /**
   * Configures Redis URI builder with standalone host and port.
   *
   * <h2>Standalone Configuration</h2>
   *
   * <ul>
   *   <li>Uses first host and port from configuration
   *   <li>Supports single Redis server setup
   * </ul>
   *
   * @param builder Redis URI builder
   * @param hostPorts List of host and port configurations
   * @param config Redis server configuration
   * @return Configured standalone URI builder
   * @throws NullPointerException if host ports are not configured
   */
  private static RedisURI.Builder addStandaloneHostAndPort(
      RedisURI.Builder builder, List<HostPort> hostPorts, RedisServerConfig config) {
    HostPort hostPort = Objects.requireNonNull(hostPorts, "host & ports are required").getFirst();
    return builder.withHost(hostPort.getHost()).withPort(hostPort.getPort());
  }
}
