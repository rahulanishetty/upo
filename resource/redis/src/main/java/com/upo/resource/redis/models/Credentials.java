/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.resource.redis.models;

/**
 * Represents a set of credentials for authenticating with a Redis server.
 *
 * <h2>Purpose</h2>
 *
 * Provides a flexible mechanism for storing authentication information for Redis connection
 * configurations.
 *
 * <h2>Credential Types</h2>
 *
 * The current implementation supports:
 *
 * <ul>
 *   <li>STATIC: Traditional username/password authentication
 *   <li>Placeholder for future authentication mechanisms (IAM, etc.)
 * </ul>
 *
 * @see RedisServerConfig
 */
public class Credentials {
  /**
   * Type of credentials (e.g., STATIC, IAM). Currently, supports STATIC, with placeholders for
   * future authentication types.
   */
  private String type;

  /** Username for authentication. */
  private String userName;

  /** Password for authentication. */
  private String password;

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}
