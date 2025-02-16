/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.resource.client.base.impl;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.upo.resource.client.base.*;
import com.upo.resource.client.base.models.ResourceCategory;
import com.upo.resource.client.base.models.ResourceConfig;

/**
 * Abstract implementation of a resource client factory with caching and configuration management.
 *
 * @param <Client> The type of client being managed (must implement {@link Closeable})
 * @param <Config> The configuration type for client creation
 */
public abstract class ResourceClientFactoryImpl<
        Client extends Closeable, Config extends ResourceConfig>
    implements ResourceClientFactory<Client> {

  private final Logger LOGGER = LoggerFactory.getLogger(getClass());

  protected final ResourceConfigProvider resourceConfigProvider;
  private final Map<String, Optional<Client>> clientCache;
  private final Class<Config> configClz;
  private final AtomicBoolean closed = new AtomicBoolean(false);

  public ResourceClientFactoryImpl(
      ResourceConfigProvider resourceConfigProvider, Class<Config> configClz) {
    this.resourceConfigProvider = resourceConfigProvider;
    this.configClz = configClz;
    this.clientCache = new ConcurrentHashMap<>();
  }

  @Override
  public Client getClientOrFail(ResourceCategory resourceCategory, String resourceId) {
    return getClient(resourceCategory, resourceId)
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    "No Resource found for resourceCategory: "
                        + resourceCategory
                        + ", resourceId: "
                        + resourceId));
  }

  @Override
  public Optional<Client> getClient(ResourceCategory resourceCategory, String resourceId) {
    return clientCache.computeIfAbsent(
        createServerResourceId(resourceCategory, resourceId),
        serverResourceId -> {
          checkNotClosed();
          Config config = resourceConfigProvider.getConfig(serverResourceId, configClz);
          if (config == null) {
            return Optional.empty();
          }
          Client client = createClient(config);
          return Optional.of(client);
        });
  }

  @Override
  public void close() throws IOException {
    if (!closed.compareAndSet(false, true)) {
      return;// Already closed
    }
    for (Map.Entry<String, Optional<Client>> entry : clientCache.entrySet()) {
      entry
          .getValue()
          .ifPresent(
              client -> {
                try {
                  client.close();
                } catch (Exception eX) {
                  LOGGER.error("Failed to close client for resource: {}", entry.getKey(), eX);
                }
              });
    }
    clientCache.clear();
  }

  protected void checkNotClosed() {
    if (closed.get()) {
      throw new IllegalStateException("Factory is closed");
    }
  }

  /**
   * Creates a specific client instance based on the provided configuration.
   *
   * <p>This method must be implemented by subclasses to define how specific types of clients are
   * instantiated.
   *
   * @param config Configuration used to create the client
   * @return A configured client instance
   */
  protected abstract Client createClient(Config config);

  /**
   * Generates a unique identifier for a server resource.
   *
   * @param resourceCategory The category of the resource
   * @param resourceId The specific resource identifier
   * @return A concatenated, unique resource identifier
   */
  private String createServerResourceId(ResourceCategory resourceCategory, String resourceId) {
    return resourceCategory.name() + "/" + resourceCategory.name() + "/" + resourceId;
  }
}
