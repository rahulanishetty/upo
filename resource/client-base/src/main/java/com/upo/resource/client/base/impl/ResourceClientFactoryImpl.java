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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.upo.resource.client.base.*;
import com.upo.resource.client.base.models.ResourceCategory;
import com.upo.resource.client.base.models.ResourceConfig;

public abstract class ResourceClientFactoryImpl<
        Client extends Closeable, Config extends ResourceConfig>
    implements ResourceClientFactory<Client> {

  private final Logger LOGGER = LoggerFactory.getLogger(getClass());

  protected final ResourceConfigProvider resourceConfigProvider;
  private final Map<String, Optional<Client>> clientCache;

  public ResourceClientFactoryImpl(ResourceConfigProvider resourceConfigProvider) {
    this.resourceConfigProvider = resourceConfigProvider;
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
          Config config = resourceConfigProvider.getConfig(serverResourceId);
          if (config == null) {
            return Optional.empty();
          }
          Client client = createClient(config);
          return Optional.of(client);
        });
  }

  @Override
  public void close() throws IOException {
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
  }

  protected abstract Client createClient(Config config);

  private String createServerResourceId(ResourceCategory resourceCategory, String resourceId) {
    return resourceCategory.name() + "/" + resourceCategory.name() + "/" + resourceId;
  }
}
