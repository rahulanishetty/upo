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

/**
 * Abstract implementation of a resource template factory that extends the resource client factory.
 *
 * @param <Template> The type of template being managed
 * @param <Client> The client type used for template creation
 * @param <TemplateConfig> Configuration for template creation
 * @param <ServerConfig> Configuration for the underlying server/resource
 */
public abstract class ResourceTemplateFactoryImpl<
        Template,
        Client extends Closeable,
        TemplateConfig extends PartitionResourceConfig,
        ServerConfig extends ResourceConfig>
    extends ResourceClientFactoryImpl<Client, ServerConfig>
    implements ResourceTemplateFactory<Template> {

  private final ResourceCategory resourceCategory;
  private final Class<TemplateConfig> templateConfigClz;
  private final Map<String, Optional<Template>> templateCache;

  public ResourceTemplateFactoryImpl(
      ResourceConfigProvider resourceConfigProvider,
      ResourceCategory resourceCategory,
      Class<ServerConfig> serverConfigClz,
      Class<TemplateConfig> templateConfigClz) {
    super(resourceConfigProvider, serverConfigClz);
    this.resourceCategory = resourceCategory;
    this.templateConfigClz = templateConfigClz;
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
          TemplateConfig config =
              resourceConfigProvider.getConfig(partitionResourceId, templateConfigClz);
          if (config == null) {
            return Optional.empty();
          }
          String resourceIdSuffix = config.getResourceIdSuffix();
          return getClient(resourceCategory, resourceIdSuffix)
              .map(client -> createTemplate(client, config));
        });
  }

  /**
   * Creates a specific template instance based on the provided client and configuration.
   *
   * <p>This method must be implemented by subclasses to define how specific types of templates are
   * instantiated.
   *
   * @param client The client used to create the template
   * @param config Configuration used to create the template
   * @return A configured template instance
   */
  protected abstract Template createTemplate(Client client, TemplateConfig config);

  /**
   * Generates a unique identifier for a partition resource.
   *
   * @param resourceType The type of resource
   * @param partitionKey The specific partition identifier
   * @return A concatenated, unique resource identifier
   */
  private String createPartitionResourceId(ResourceType resourceType, String partitionKey) {
    return resourceCategory.name() + "/" + resourceType.name() + "/" + partitionKey;
  }
}
