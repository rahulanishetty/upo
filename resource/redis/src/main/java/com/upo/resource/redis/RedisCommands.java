/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.resource.redis;

import java.io.Closeable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Function;

import io.lettuce.core.cluster.api.sync.RedisClusterCommands;

/**
 * Extends Redis cluster commands with additional lifecycle management.
 *
 * <h2>Purpose</h2>
 *
 * Provides a specialized interface for executing Redis commands with:
 *
 * <ul>
 *   <li>Support for Redis cluster operations
 *   <li>Automatic resource management
 *   <li>Streamlined connection handling
 * </ul>
 *
 * <h2>Usage Example</h2>
 *
 * <pre>{@code
 * try (RedisCommands commands = client.getRedisCommands()) {
 *     commands.set("key", "value");
 *     String value = commands.get("key");
 * }
 * }</pre>
 *
 * @see RedisClusterCommands
 * @see Closeable
 */
public interface RedisCommands extends RedisClusterCommands<String, String>, Closeable {

  /**
   * Closes the Redis connection or returns it back to the pool
   *
   * <p>Overrides the close method to ensure proper connection cleanup.
   */
  @Override
  void close();

  /**
   * Creates a dynamically proxied instance of RedisCommands from a given connection.
   *
   * @param <T> The type of AutoCloseable connection
   * @param connection The connection to be managed
   * @param function Function to retrieve Redis cluster commands from the connection
   * @return A dynamically proxied RedisCommands instance
   * @see Proxy#newProxyInstance(ClassLoader, Class[], InvocationHandler)
   * @see CommandsInvocationHandler
   */
  static <T extends AutoCloseable> RedisCommands from(
      T connection, Function<T, RedisClusterCommands<String, String>> function) {
    return (RedisCommands)
        Proxy.newProxyInstance(
            RedisClient.class.getClassLoader(),
            new Class[] {RedisCommands.class},
            new CommandsInvocationHandler<>(connection, function));
  }

  /**
   * Dynamic invocation handler for Redis commands with advanced connection management.
   *
   * @param <Connection> The type of connection being managed
   */
  class CommandsInvocationHandler<Connection extends AutoCloseable> implements InvocationHandler {

    /**
     * The underlying connection to be managed. Supports any {@link AutoCloseable} connection type.
     */
    private final Connection connection;

    /**
     * Function to retrieve Redis cluster commands from the connection. Provides flexible command
     * retrieval strategy.
     */
    private final Function<Connection, RedisClusterCommands<String, String>> commandsFunction;

    public CommandsInvocationHandler(
        Connection connection,
        Function<Connection, RedisClusterCommands<String, String>> commandsFunction) {
      this.connection = connection;
      this.commandsFunction = commandsFunction;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      if (method.getDeclaringClass().equals(RedisCommands.class)) {
        connection.close();
        return null;
      }
      return invoke(method, args);
    }

    /**
     * Internal method to invoke commands on the underlying Redis connection.
     *
     * @param method Method to invoke
     * @param args Method arguments
     * @return Result of method invocation
     * @throws Throwable If an error occurs during invocation
     */
    private Object invoke(Method method, Object[] args) throws Throwable {
      try {
        return method.invoke(commandsFunction.apply(connection), args);
      } catch (InvocationTargetException eX) {
        throw eX.getTargetException();
      }
    }
  }
}
