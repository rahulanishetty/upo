/*
* Copyright (c) 2025 Rahul Anishetty
*
* This program is dual-licensed under either AGPL-3.0 or a commercial license.
* For commercial licensing options, please contact the author.
* For AGPL-3.0 licensing details, see the LICENSE file in the repository root.
*/
package com.upo.utilities.json.path;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class JsonPathTest {

  private Map<String, Object> testData;

  @BeforeEach
  void setUp() {
    testData = new HashMap<>();

   // Setup nested data structure
    List<Map<String, Object>> addresses = new ArrayList<>();
    Map<String, Object> address1 = new HashMap<>();
    address1.put("city", "New York");
    Map<String, Object> address2 = new HashMap<>();
    address2.put("city", "Boston");
    addresses.add(address1);
    addresses.add(address2);

    Map<String, Object> customer = new HashMap<>();
    customer.put("addresses", addresses);
    customer.put("phone.number", "123-456-7890");

    List<String> ids = Arrays.asList("ID1", "ID2", "ID3");
    customer.put("special.ids", ids);

    testData.put("customer", customer);
  }

  @Test
  void testSimplePath() {
    JsonPath path = JsonPath.create("customer.addresses[0].city");
    assertEquals("New York", path.read(testData));
  }

  @Test
  void testQuotedKey() {
    JsonPath path = JsonPath.create("customer.\"phone.number\"");
    assertEquals("123-456-7890", path.read(testData));
  }

  @Test
  void testQuotedKeyWithArray() {
    JsonPath path = JsonPath.create("customer.\"special.ids\"[1]");
    assertEquals("ID2", path.read(testData));
  }

  @Test
  void testNonExistentPath() {
    JsonPath path = JsonPath.create("customer.nonexistent");
    assertNull(path.read(testData));
  }

  @Test
  void testInvalidArrayIndex() {
    JsonPath path = JsonPath.create("customer.addresses[99].city");
    assertNull(path.read(testData));
  }

  @Test
  void testTokenization() {
    JsonPath path = JsonPath.create("customer.addresses[1].city");
    assertEquals("customer", path.getToken(0));
    assertEquals("addresses", path.getToken(1));
    assertEquals("city", path.getToken(2));
  }

  @Test
  void testTokenizationWithDotNotation() {
    JsonPath path = JsonPath.create("customer.addresses.1.city");
    assertEquals("customer", path.getToken(0));
    assertEquals("addresses", path.getToken(1));
    assertEquals("1", path.getToken(2));
    assertEquals("city", path.getToken(3));
  }

  @Test
  void testInvalidPathSyntax() {
    assertThrows(IllegalArgumentException.class, () -> JsonPath.create("customer..addresses"));
  }

  @Test
  void testEmptyPath() {
    assertThrows(IllegalArgumentException.class, () -> JsonPath.create(""));
  }

  @Test
  void testAllArraySelection() {
    JsonPath dotPath = JsonPath.create("customer.addresses.city");
    assertEquals(List.of("New York", "Boston"), dotPath.read(testData));
  }
}
