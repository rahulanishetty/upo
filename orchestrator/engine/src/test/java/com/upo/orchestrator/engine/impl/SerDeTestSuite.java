/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.orchestrator.engine.impl;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/** Test suite for all serialization and deserialization related to orchestrator beans. */
@Suite
@SuiteDisplayName("Serialization and Deserialization test suite")
@SelectClasses({
  SignalSerializationTest.class,
})
public class SerDeTestSuite {}
