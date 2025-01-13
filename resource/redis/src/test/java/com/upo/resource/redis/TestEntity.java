/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.resource.redis;

import java.util.Objects;

public class TestEntity {
  private String id;
  private String name;
  private Long version;
  private String status;

  public TestEntity() {}

  public TestEntity(String id) {
    this.id = id;
  }

  public TestEntity(String id, String name) {
    this.id = id;
    this.name = name;
  }

  public TestEntity(String id, String name, Long version) {
    this.id = id;
    this.name = name;
    this.version = version;
  }

  public TestEntity(String id, String name, Long version, String status) {
    this.id = id;
    this.name = name;
    this.version = version;
    this.status = status;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  @Override
  public boolean equals(Object object) {
    if (object == null || getClass() != object.getClass()) return false;
    TestEntity that = (TestEntity) object;
    return Objects.equals(id, that.id)
        && Objects.equals(name, that.name)
        && Objects.equals(version, that.version)
        && Objects.equals(status, that.status);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, version, status);
  }
}
