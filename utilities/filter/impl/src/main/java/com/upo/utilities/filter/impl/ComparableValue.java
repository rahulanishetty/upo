/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.utilities.filter.impl;

/**
 * Represents a value that can be compared with other values of the same type. This interface
 * provides type safety for comparison operations while maintaining the comparable nature of the
 * wrapped value.
 *
 * @param <Value> the type of the wrapped comparable value
 */
public interface ComparableValue<Value extends Comparable<Value>> {}
