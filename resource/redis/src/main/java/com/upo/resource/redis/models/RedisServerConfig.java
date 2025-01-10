/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.resource.redis.models;

import com.upo.resource.client.base.models.ResourceConfig;

import io.vertx.redis.client.RedisClientType;
import io.vertx.redis.client.RedisReplicas;
import io.vertx.redis.client.RedisRole;

public class RedisServerConfig extends ResourceConfig {

  private String host;
  private int port;
  private boolean ssl;
  private boolean disableHostNameVerification;

  /** {@link RedisClientType} */
  private String clientType;

  /** {@link RedisRole} */
  private String role;

  /** {@link RedisReplicas} */
  private String useReplicas;

  private String masterName;
  private String password;

  private Integer maxWaitingHandlers;
  private Integer maxNestedArrays;

  /** Pool Options */
  private Integer poolCleanerInterval;

  private Integer maxPoolSize;
  private Integer poolMaxWaiting;
  private Integer poolRecycleTimeout;

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
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

  public String getClientType() {
    return clientType;
  }

  public void setClientType(String clientType) {
    this.clientType = clientType;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public String getUseReplicas() {
    return useReplicas;
  }

  public void setUseReplicas(String useReplicas) {
    this.useReplicas = useReplicas;
  }

  public String getMasterName() {
    return masterName;
  }

  public void setMasterName(String masterName) {
    this.masterName = masterName;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public Integer getMaxWaitingHandlers() {
    return maxWaitingHandlers;
  }

  public void setMaxWaitingHandlers(Integer maxWaitingHandlers) {
    this.maxWaitingHandlers = maxWaitingHandlers;
  }

  public Integer getMaxNestedArrays() {
    return maxNestedArrays;
  }

  public void setMaxNestedArrays(Integer maxNestedArrays) {
    this.maxNestedArrays = maxNestedArrays;
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

  public Integer getPoolMaxWaiting() {
    return poolMaxWaiting;
  }

  public void setPoolMaxWaiting(Integer poolMaxWaiting) {
    this.poolMaxWaiting = poolMaxWaiting;
  }

  public Integer getPoolRecycleTimeout() {
    return poolRecycleTimeout;
  }

  public void setPoolRecycleTimeout(Integer poolRecycleTimeout) {
    this.poolRecycleTimeout = poolRecycleTimeout;
  }
}
