/*
 * Charybdis - Cassandra ORM framework
 *
 * Copyright (C) 2020 Charybdis authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package ma.markware.charybdis.dsl.insert;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import ma.markware.charybdis.query.InsertQuery;
import ma.markware.charybdis.test.entities.TestEnum;
import ma.markware.charybdis.test.entities.TestNestedUdt;
import ma.markware.charybdis.test.entities.TestUdt;
import ma.markware.charybdis.test.metadata.TestEntity_Table;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
abstract class AbstractDslInsertTest<T extends AbstractDslInsert> {

  private T instance;
  private TestUdt udt1, udt2;

  abstract T getInstance();

  @BeforeEach
  void setup() {
    instance = getInstance();
    TestNestedUdt nestedUdt1 = new TestNestedUdt("nestedName1", "nestedValue1", Arrays.asList(12, 13));
    TestNestedUdt nestedUdt2 = new TestNestedUdt("nestedName2", "nestedValue2", Arrays.asList(14, 15, 16));
    TestNestedUdt nestedUdt3 = new TestNestedUdt("nestedName3", "nestedValue3", Arrays.asList(17, 18));
    TestNestedUdt nestedUdt4 = new TestNestedUdt("nestedName4", "nestedValue4", Arrays.asList(19, 20, 21));
    TestNestedUdt nestedUdt5 = new TestNestedUdt("nestedName5", "nestedValue5", Arrays.asList(22, 23, 24));
    udt1 = new TestUdt(1, "test1", Arrays.asList(nestedUdt1, nestedUdt2), Collections.singleton(Arrays.asList(nestedUdt3, nestedUdt4)),
                       ImmutableMap.of(TestEnum.TYPE_A, Arrays.asList(nestedUdt1, nestedUdt5), TestEnum.TYPE_B, Collections.singletonList(nestedUdt4)),
                       new TestNestedUdt());
    udt2 = new TestUdt(2, "test2", Arrays.asList(nestedUdt2, nestedUdt3, nestedUdt4), Collections.singleton(Collections.singletonList(nestedUdt5)),
                       ImmutableMap.of(TestEnum.TYPE_A, Arrays.asList(nestedUdt5, nestedUdt3), TestEnum.TYPE_B, Arrays.asList(nestedUdt1, nestedUdt2, nestedUdt3)),
                       nestedUdt1);
  }

  @Test
  void insertInto_without_columns() {
    instance.insertInto(TestEntity_Table.test_entity);

    InsertQuery insertQuery = instance.getInsertQuery();
    assertThat(insertQuery.getKeyspace()).isEqualTo(TestEntity_Table.KEYSPACE_NAME);
    assertThat(insertQuery.getTable()).isEqualTo(TestEntity_Table.TABLE_NAME);
  }

  @Test
  void insertInto_with_columns() {
    instance.insertInto(TestEntity_Table.test_entity, TestEntity_Table.id, TestEntity_Table.date, TestEntity_Table.list, TestEntity_Table.udt);

    InsertQuery insertQuery = instance.getInsertQuery();

    assertThat(insertQuery.getKeyspace()).isEqualTo(TestEntity_Table.KEYSPACE_NAME);
    assertThat(insertQuery.getTable()).isEqualTo(TestEntity_Table.TABLE_NAME);
    Map<Integer, Pair<String, Object>> columnNameValuePairs = insertQuery.getColumnNameValueMapping()
                                                                         .getColumnNameValuePairs();
    assertThat(columnNameValuePairs).hasSize(4);
    assertThat(columnNameValuePairs).containsEntry(0, Pair.of(TestEntity_Table.id.getName(), null))
                                    .containsEntry(1, Pair.of(TestEntity_Table.date.getName(), null))
                                    .containsEntry(2, Pair.of(TestEntity_Table.list.getName(), null))
                                    .containsEntry(3, Pair.of(TestEntity_Table.udt.getName(), null));
  }

  @Test
  void values() {
    UUID uuid = UUID.randomUUID();
    Instant now = Instant.now();
    List<String> stringList = Arrays.asList("value1", "value2");
    instance.insertInto(TestEntity_Table.test_entity, TestEntity_Table.id, TestEntity_Table.date, TestEntity_Table.list, TestEntity_Table.udt)
              .values(uuid, now, stringList, udt1);

    InsertQuery insertQuery = instance.getInsertQuery();
    Map<Integer, Pair<String, Object>> columnNameValuePairs = insertQuery.getColumnNameValueMapping()
                                                                         .getColumnNameValuePairs();
    assertThat(columnNameValuePairs).hasSize(4);
    assertThat(columnNameValuePairs).containsEntry(0, Pair.of(TestEntity_Table.id.getName(), uuid))
                                    .containsEntry(1, Pair.of(TestEntity_Table.date.getName(), now))
                                    .containsEntry(2, Pair.of(TestEntity_Table.list.getName(), stringList))
                                    .containsEntry(3, Pair.of(TestEntity_Table.udt.getName(), TestEntity_Table.udt.serialize(udt1)));
  }

  @Test
  void set() {
    UUID uuid = UUID.randomUUID();
    Instant now = Instant.now();
    List<String> stringList = Arrays.asList("value1", "value2");
    instance.insertInto(TestEntity_Table.test_entity)
              .set(TestEntity_Table.id, uuid)
              .set(TestEntity_Table.date, now)
              .set(TestEntity_Table.list, stringList)
              .set(TestEntity_Table.udt, udt2);

    InsertQuery insertQuery = instance.getInsertQuery();
    Map<Integer, Pair<String, Object>> columnNameValuePairs = insertQuery.getColumnNameValueMapping()
                                                                         .getColumnNameValuePairs();
    assertThat(columnNameValuePairs).hasSize(4);
    assertThat(columnNameValuePairs).containsEntry(0, Pair.of(TestEntity_Table.id.getName(), uuid))
                                    .containsEntry(1, Pair.of(TestEntity_Table.date.getName(), now))
                                    .containsEntry(2, Pair.of(TestEntity_Table.list.getName(), stringList))
                                    .containsEntry(3, Pair.of(TestEntity_Table.udt.getName(), TestEntity_Table.udt.serialize(udt2)));
  }

  @Test
  void usingTtl() {
    UUID uuid = UUID.randomUUID();
    Instant now = Instant.now();
    List<String> stringList = Arrays.asList("value1", "value2");
    instance.insertInto(TestEntity_Table.test_entity, TestEntity_Table.id, TestEntity_Table.date, TestEntity_Table.list, TestEntity_Table.udt)
              .values(uuid, now, stringList, udt1)
              .usingTtl(10000);

    InsertQuery insertQuery = instance.getInsertQuery();

    assertThat(insertQuery.getTtl()).isEqualTo(10000);
  }

  @Test
  void usingTimestamp() {
    UUID uuid = UUID.randomUUID();
    Instant now = Instant.now();
    List<String> stringList = Arrays.asList("value1", "value2");
    instance.insertInto(TestEntity_Table.test_entity, TestEntity_Table.id, TestEntity_Table.date, TestEntity_Table.list, TestEntity_Table.udt)
              .values(uuid, now, stringList, udt1)
              .usingTimestamp(now.plus(1, ChronoUnit.DAYS));

    InsertQuery insertQuery = instance.getInsertQuery();

    assertThat(insertQuery.getTimestamp()).isEqualTo(now.plus(1, ChronoUnit.DAYS).toEpochMilli());
  }

  @Test
  void usingTimestamp_epochMilli() {
    UUID uuid = UUID.randomUUID();
    Instant now = Instant.now();
    List<String> stringList = Arrays.asList("value1", "value2");
    instance.insertInto(TestEntity_Table.test_entity, TestEntity_Table.id, TestEntity_Table.date, TestEntity_Table.list, TestEntity_Table.udt)
              .values(uuid, now, stringList, udt1)
              .usingTimestamp(now.plus(1, ChronoUnit.DAYS).toEpochMilli());

    InsertQuery insertQuery = instance.getInsertQuery();

    assertThat(insertQuery.getTimestamp()).isEqualTo(now.plus(1, ChronoUnit.DAYS).toEpochMilli());
  }

  @Test
  void ifNotExists() {
    UUID uuid = UUID.randomUUID();
    Instant now = Instant.now();
    List<String> stringList = Arrays.asList("value1", "value2");
    instance.insertInto(TestEntity_Table.test_entity, TestEntity_Table.id, TestEntity_Table.date, TestEntity_Table.list, TestEntity_Table.udt)
              .values(uuid, now, stringList, udt1)
              .ifNotExists();

    InsertQuery insertQuery = instance.getInsertQuery();

    assertThat(insertQuery.isIfNotExists()).isTrue();
  }
}
