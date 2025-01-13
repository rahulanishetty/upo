/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.utilities.context;

import java.util.HashMap;
import java.util.Map;

/** Test implementation of RequestContext */
public class TestRequestContext implements RequestContext {
  private final String partitionKey;
  private final Map<String, Object> contextData;

  public TestRequestContext(String partitionKey) {
    this.partitionKey = partitionKey;
    this.contextData = new HashMap<>();
    contextData.put("partitionKey", partitionKey);
    contextData.put("timestamp", System.currentTimeMillis());
  }

  @Override
  public String getPartitionKey() {
    return partitionKey;
  }

  @Override
  public Map<String, Object> toMap() {
    return new HashMap<>(contextData);
  }
}
