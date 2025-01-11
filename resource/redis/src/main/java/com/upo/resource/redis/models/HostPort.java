/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.resource.redis.models;

/**
 * Represents a host and port combination for network connectivity.
 *
 * <h2>Purpose</h2>
 *
 * Provides a simple, immutable representation of a network endpoint with a host address and port
 * number.
 *
 * <h2>Usage Example</h2>
 *
 * <pre>{@code
 * HostPort endpoint = new HostPort();
 * endpoint.setHost("redis.example.com");
 * endpoint.setPort(6379);
 * }</pre>
 *
 * @see RedisServerConfig
 */
public class HostPort {
  /** Hostname or IP address of the server. */
  private String host;

  /** Port number for the server. */
  private int port;

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
}
