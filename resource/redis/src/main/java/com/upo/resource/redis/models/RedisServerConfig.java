/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.resource.redis.models;

import java.util.List;

import com.upo.resource.client.base.models.ResourceConfig;

/**
 * Configuration class for Redis server connections with comprehensive connection settings.
 *
 * <h2>Purpose</h2>
 *
 * Provides a flexible and extensible configuration mechanism for Redis connections, supporting
 * multiple connection strategies and authentication methods.
 *
 * <h2>Supported Client Types</h2>
 *
 * <ul>
 *   <li>STANDALONE: Single Redis server connection
 *   <li>SENTINEL: Redis high availability configuration
 *   <li>CLUSTER: Distributed Redis cluster configuration
 * </ul>
 *
 * <h2>Key Configuration Options</h2>
 *
 * <ul>
 *   <li>Multiple connection endpoints
 *   <li>SSL configuration
 *   <li>Authentication credentials
 *   <li>Client type selection
 *   <li>Read preference configuration
 * </ul>
 *
 * <h2>Usage Example</h2>
 *
 * <pre>{@code
 * RedisServerConfig config = new RedisServerConfig();
 * config.setClientType(RedisServerConfig.ClientType.SENTINEL);
 * config.setHostPorts(Arrays.asList(
 *     new HostPort("redis1.example.com", 6379),
 *     new HostPort("redis2.example.com", 6379)
 * ));
 * config.setSsl(true);
 * config.setCredentials(credentials);
 * }</pre>
 *
 * @see Credentials
 * @see HostPort
 */
public class RedisServerConfig extends ResourceConfig {
  /** Enumeration of supported Redis client connection types. */
  public enum ClientType {
    /** Single Redis server connection */
    STANDALONE,
    /** Redis high availability configuration with sentinels */
    SENTINEL,
    /** Distributed Redis cluster configuration */
    CLUSTER;
  }

  /** List of server endpoints for connection. */
  private List<HostPort> hostPorts;

  /** Flag to enable SSL connection. */
  private boolean ssl;

  /** Flag to disable hostname verification for SSL connections. */
  private boolean disableHostNameVerification;

  /** Type of Redis client connection. */
  private ClientType clientType;

  /**
   * Configuration for read preferences in distributed setups.
   *
   * @see io.lettuce.core.ReadFrom
   */
  private String readPreference;

  /** Sentinel master ID for high availability configurations. */
  private String sentinelMasterId;

  /** Authentication credentials for the Redis connection. */
  private Credentials credentials;

  private Integer maxPoolSize;

  /** time duration between pool cleanup in seconds. default value = 30 seconds */
  private Integer poolCleanerInterval;

  /** maximum time a request can wait for a connection. default value = 5 seconds */
  private Integer poolMaxWaitTime;

  public List<HostPort> getHostPorts() {
    return hostPorts;
  }

  public void setHostPorts(List<HostPort> hostPorts) {
    this.hostPorts = hostPorts;
  }

  public boolean isSsl() {
    return ssl;
  }

  public void setSsl(boolean ssl) {
    this.ssl = ssl;
  }

  public boolean isDisableHostNameVerification() {
    return disableHostNameVerification;
  }

  public void setDisableHostNameVerification(boolean disableHostNameVerification) {
    this.disableHostNameVerification = disableHostNameVerification;
  }

  public ClientType getClientType() {
    return clientType;
  }

  public void setClientType(ClientType clientType) {
    this.clientType = clientType;
  }

  public String getReadPreference() {
    return readPreference;
  }

  public void setReadPreference(String readPreference) {
    this.readPreference = readPreference;
  }

  public String getSentinelMasterId() {
    return sentinelMasterId;
  }

  public void setSentinelMasterId(String sentinelMasterId) {
    this.sentinelMasterId = sentinelMasterId;
  }

  public Credentials getCredentials() {
    return credentials;
  }

  public void setCredentials(Credentials credentials) {
    this.credentials = credentials;
  }

  public Integer getPoolCleanerInterval() {
    return poolCleanerInterval;
  }

  public void setPoolCleanerInterval(Integer poolCleanerInterval) {
    this.poolCleanerInterval = poolCleanerInterval;
  }

  public Integer getMaxPoolSize() {
    return maxPoolSize;
  }

  public void setMaxPoolSize(Integer maxPoolSize) {
    this.maxPoolSize = maxPoolSize;
  }

  public Integer getPoolMaxWaitTime() {
    return poolMaxWaitTime;
  }

  public void setPoolMaxWaitTime(Integer poolMaxWaitTime) {
    this.poolMaxWaitTime = poolMaxWaitTime;
  }
}
