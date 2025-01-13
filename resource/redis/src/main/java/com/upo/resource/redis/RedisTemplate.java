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
 * Template interface for Redis operations providing type-safe access to Redis commands. Supports
 * key-value operations, collections, expiry management, and pattern-based operations.
 */
public interface RedisTemplate {
  /**
   * Inserts a value if the key doesn't exist (atomic operation).
   *
   * @param id The key identifier
   * @param value The value to insert
   * @return true if inserted, false if key already exists
   */
  boolean insert(String id, String value);

  /**
   * Inserts a value with expiry if the key doesn't exist (atomic operation).
   *
   * @param id The key identifier
   * @param value The value to insert
   * @param expirySeconds Time in seconds after which the key will expire
   * @return true if inserted, false if key already exists
   */
  boolean insertWithExpiry(String id, String value, long expirySeconds);

  /**
   * Atomically inserts multiple key-value pairs if none of the keys exist. Either all insertions
   * succeed, or none do (transaction).
   *
   * @param entries Map of key-value pairs to insert
   * @return true if all entries were inserted, false if any key existed
   */
  boolean insertMany(Map<String, String> entries);

  /**
   * Saves a value, overwriting if the key exists.
   *
   * @param id The key identifier
   * @param value The value to save
   * @return true if saved successfully
   */
  boolean save(String id, String value);

  /**
   * Saves a value with expiry, overwriting if the key exists.
   *
   * @param id The key identifier
   * @param value The value to save
   * @param expirySeconds Time in seconds after which the key will expire
   * @return true if saved successfully
   */
  boolean saveWithExpiry(String id, String value, long expirySeconds);

  /**
   * Atomically saves multiple key-value pairs, overwriting any existing values. All operations are
   * performed in a single transaction.
   *
   * @param entries Map of key-value pairs to save
   * @return true if all entries were saved successfully
   */
  boolean saveMany(Map<String, String> entries);

  /**
   * Atomically sets a key to a new value and returns the old value.
   *
   * @param id The key identifier
   * @param value The new value to set
   * @return Optional containing the old value, empty if key didn't exist
   */
  Optional<String> getSet(String id, String value);

  /**
   * Retrieves a value by its key.
   *
   * @param id The key identifier
   * @return Optional containing the value if found, empty otherwise
   */
  Optional<String> get(String id);

  /**
   * Retrieves multiple values by their keys in a single operation. Keys that don't exist will have
   * null values in the result map.
   *
   * @param ids Collection of keys to retrieve
   * @return Map of key-value pairs for existing keys
   */
  Map<String, String> getMany(Collection<String> ids);

  /**
   * Atomically retrieves a value and deletes the key.
   *
   * @param id The key identifier
   * @return Optional containing the value before deletion, empty if key didn't exist
   */
  Optional<String> getDel(String id);

  /**
   * Atomically retrieves a value and expires the key after the specified time.
   *
   * @param id The key identifier
   * @param expirySeconds Time in seconds after which the key will expire
   * @return Optional containing the value, empty if key didn't exist
   */
  Optional<String> getEx(String id, long expirySeconds);

  /**
   * Atomically retrieves and persists a value (removes expiry).
   *
   * @param id The key identifier
   * @return Optional containing the value, empty if key didn't exist
   */
  Optional<String> getPersist(String id);

  /**
   * Updates the expiration time of an existing key.
   *
   * @param id The key identifier
   * @param expirySeconds New expiration time in seconds
   * @return true if expiry was set, false if key doesn't exist
   */
  boolean updateExpiry(String id, long expirySeconds);

  /**
   * Deletes a key and its associated value.
   *
   * @param id The key identifier
   * @return true if deleted, false if key didn't exist
   */
  boolean delete(String id);

  /**
   * Deletes keys and its associated values.
   *
   * @param ids The key identifier
   * @return The number of keys that were removed.
   */
  long deleteMany(Collection<String> ids);

  /**
   * Adds a value to the end of a list.
   *
   * @param id The list identifier
   * @param value The value to add
   * @return The length of the list after addition
   */
  long addToList(String id, String value);

  /**
   * Adds multiple values to the end of a list.
   *
   * @param id The list identifier
   * @param values The values to add
   * @return The length of the list after addition
   */
  long addAllToList(String id, List<String> values);

  /**
   * Retrieves and removes the first element of a list.
   *
   * @param id The list identifier
   * @return Optional containing the popped value, empty if list is empty
   */
  Optional<String> popFromList(String id);

  /**
   * Retrieves and removes the last element of a list.
   *
   * @param id The list identifier
   * @return Optional containing the popped value, empty if list is empty
   */
  Optional<String> popFromListEnd(String id);

  /**
   * Gets all elements in a list.
   *
   * @param id The list identifier
   * @return List of elements, empty list if key doesn't exist
   */
  List<String> getList(String id);

  /**
   * Adds one or more values to a set.
   *
   * @param id The set identifier
   * @param values The values to add
   * @return Number of new elements added (excluding duplicates)
   */
  long addToSet(String id, String... values);

  /**
   * Removes values from a set.
   *
   * @param id The set identifier
   * @param values The values to remove
   * @return Number of elements removed
   */
  long removeFromSet(String id, String... values);

  /**
   * Gets all members of a set.
   *
   * @param id The set identifier
   * @return Set of elements, empty set if key doesn't exist
   */
  Set<String> getSetMembers(String id);

  /**
   * Checks if a value is a member of a set.
   *
   * @param id The set identifier
   * @param value The value to check
   * @return true if the value is in the set
   */
  boolean isSetMember(String id, String value);

  /**
   * Atomically increments a counter.
   *
   * @param id The key identifier
   * @return The new value after increment
   */
  long increment(String id);

  /**
   * Atomically increments a counter by a specific amount.
   *
   * @param id The key identifier
   * @param amount The amount to increment by
   * @return The new value after increment
   */
  long incrementBy(String id, long amount);

  /**
   * Atomically decrements a counter.
   *
   * @param id The key identifier
   * @return The new value after decrement
   */
  long decrement(String id);

  /**
   * Finds keys matching a pattern.
   *
   * @param pattern The pattern to match (e.g., "user:*")
   * @return List of matching keys
   */
  List<String> findKeysByPattern(String pattern);

  /**
   * Checks if a key exists.
   *
   * @param id The key identifier
   * @return true if key exists, false otherwise
   */
  boolean exists(String id);

  /**
   * Gets the remaining time to live for a key.
   *
   * @param id The key identifier
   * @return Optional containing TTL in seconds, empty if key doesn't exist
   */
  Optional<Long> getTimeToLive(String id);

  /**
   * Returns the namespace prefix used for all Redis keys managed by this template. The namespace
   * helps prevent key collisions and organizes keys by their domain.
   *
   * <p>The returned prefix is guaranteed to: - Never be null - Always end with a forward slash
   * ('/') - Be consistent across all operations
   *
   * <p>Prefix: "<prefixAtResourceCategory>/<partitionKey>/<resourceType>"
   *
   * @return The namespace prefix that is prepended to all Redis keys
   */
  String getKeyNamespace();

  /**
   * Loads a predefined script into Redis and returns its digest. If the script is already loaded,
   * returns the existing digest. The script is identified by a scriptId which maps to standard
   * scripts maintained by the template.
   *
   * <p>Standard scripts include: - "update-if" : Conditional update with JSON path comparison -
   * "batch-ops" : Atomic batch operations
   *
   * @param scriptId The identifier for the standard script to load
   * @param force forces internal refresh if requested
   * @return The digest of the loaded script, used for execution
   */
  String loadStandardScript(String scriptId, boolean force);
}
