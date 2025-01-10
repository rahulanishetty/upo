/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.resource.client.base.impl;

import java.io.Closeable;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.upo.resource.client.base.*;
import com.upo.resource.client.base.models.PartitionResourceConfig;
import com.upo.resource.client.base.models.ResourceCategory;
import com.upo.resource.client.base.models.ResourceConfig;
import com.upo.resource.client.base.models.ResourceType;

public abstract class ResourceTemplateFactoryImpl<
        Template,
        Client extends Closeable,
        TemplateConfig extends PartitionResourceConfig,
        ServerConfig extends ResourceConfig>
    extends ResourceClientFactoryImpl<Client, ServerConfig>
    implements ResourceTemplateFactory<Template> {

  private final ResourceCategory resourceCategory;
  private final Map<String, Optional<Template>> templateCache;

  public ResourceTemplateFactoryImpl(
      ResourceConfigProvider resourceConfigProvider, ResourceCategory resourceCategory) {
    super(resourceConfigProvider);
    this.resourceCategory = resourceCategory;
    this.templateCache = new ConcurrentHashMap<>();
  }

  @Override
  public Template getTemplateOrFail(ResourceType resourceType, String partitionKey) {
    return getTemplate(resourceType, partitionKey)
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    "failed to create template for category: "
                        + resourceCategory.name()
                        + ", type: "
                        + resourceType.name()
                        + ", partitionKey: "
                        + partitionKey));
  }

  @Override
  public Optional<Template> getTemplate(ResourceType resourceType, String partitionKey) {
    return templateCache.computeIfAbsent(
        createPartitionResourceId(resourceType, partitionKey),
        partitionResourceId -> {
          TemplateConfig config = resourceConfigProvider.getConfig(partitionResourceId);
          if (config == null) {
            return Optional.empty();
          }
          String resourceIdSuffix = config.getResourceIdSuffix();
          return getClient(resourceCategory, resourceIdSuffix)
              .map(client -> createTemplate(client, config));
        });
  }

  protected abstract Template createTemplate(Client client, TemplateConfig config);

  private String createPartitionResourceId(ResourceType resourceType, String partitionKey) {
    return resourceCategory.name() + "/" + resourceType.name() + "/" + partitionKey;
  }
}
