/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.resource.redis;

import java.util.*;

/**
 * Generic repository service interface for managing domain objects. Provides CRUD operations, batch
 * operations, and search capabilities.
 *
 * @param <T> The domain object type
 * @param <ID> The identifier type
 */
public interface RepositoryService<T, ID> {

  /**
   * Inserts a new object if it doesn't exist.
   *
   * @param obj The object to insert
   * @return true if inserted, false if already exists
   */
  boolean insert(T obj);

  /**
   * Inserts multiple objects if none exist. Operation is atomic - either all objects are inserted
   * or none are.
   *
   * @param objects Collection of objects to insert
   * @return true if all inserted, false if any existed
   */
  boolean insertMany(Collection<T> objects);

  /**
   * Saves an object, overwriting if it exists.
   *
   * @param obj The object to save
   * @return true if saved successfully
   */
  boolean save(T obj);

  /**
   * Saves multiple objects, overwriting existing ones. Operation is atomic - all objects are saved
   * together.
   *
   * @param objects Collection of objects to save
   * @return true if all saved successfully
   */
  boolean saveMany(Collection<T> objects);

  /**
   * Retrieves an object by its ID.
   *
   * @param id The object identifier
   * @return Optional containing the object if found
   */
  Optional<T> findById(ID id);

  /**
   * Retrieves multiple objects by their IDs.
   *
   * @param ids Collection of object identifiers
   * @return Map of IDs to found objects (excluding missing ones)
   */
  Map<ID, T> findByIds(Collection<ID> ids);

  /**
   * Deletes an object by its ID.
   *
   * @param id The object identifier
   * @return true if deleted, false if not found
   */
  boolean deleteById(ID id);

  /**
   * Deletes multiple objects by their IDs.
   *
   * @param ids Collection of object identifiers
   * @return The number of objects that were removed.
   */
  long deleteByIds(Collection<ID> ids);

  /**
   * Retrieves and deletes an object by its ID.
   *
   * @param id The object identifier
   * @return Optional containing the object if it existed
   */
  Optional<T> getAndDelete(ID id);

  /**
   * Saves an object with an expiry time.
   *
   * @param obj The object to save
   * @param expirySeconds Time in seconds after which the object will expire
   * @return true if saved successfully
   */
  boolean saveWithExpiry(T obj, long expirySeconds);

  /**
   * Finds all objects matching a pattern.
   *
   * @param pattern The pattern to match
   * @return List of matching objects
   */
  List<T> findByPattern(String pattern);

  /**
   * Checks if an object exists by its ID.
   *
   * @param id The object identifier
   * @return true if exists, false otherwise
   */
  boolean exists(ID id);

  /**
   * Gets the time to live for an object.
   *
   * @param id The object identifier
   * @return Optional containing TTL in seconds if object exists and has expiry
   */
  Optional<Long> getTimeToLive(ID id);

  /**
   * Updates the expiry time for an object.
   *
   * @param id The object identifier
   * @param expirySeconds New expiry time in seconds
   * @return true if updated, false if not found
   */
  boolean updateExpiry(ID id, long expirySeconds);
}
