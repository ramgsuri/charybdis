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
package ma.markware.charybdis.model.field.aggregation;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.cql.Row;
import ma.markware.charybdis.model.field.metadata.ClusteringKeyColumnMetadata;
import ma.markware.charybdis.model.option.ClusteringOrder;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
class CountAggregationFieldTest {

  @Mock
  private Row row;

  private CountAggregationField countAggregationField;

  @BeforeAll
  void setup() {
    final ClusteringKeyColumnMetadata<Float, Float> columnMetadata = new ClusteringKeyColumnMetadata<Float, Float>() {
      @Override
      public Float deserialize(final Row row) {
        return row.get(getName(), Float.class);
      }

      @Override
      public Class<Float> getFieldClass() {
        return Float.class;
      }

      @Override
      public Float serialize(final Float field) {
        return field;
      }

      @Override
      public String getName() {
        return "column";
      }

      @Override
      public int getClusteringKeyIndex() {
        return 0;
      }

      @Override
      public ClusteringOrder getClusteringOrder() {
        return ClusteringOrder.DESC;
      }
    };

    countAggregationField = new CountAggregationField(columnMetadata);
  }

  @Test
  void create_function_field() {
    Assertions.assertThat(countAggregationField.getName()).isEqualTo("count_column");
    Assertions.assertThat(countAggregationField.getFieldClass()).isEqualTo(long.class);
  }

  @Test
  void deserialize() {
    countAggregationField.deserialize(row);
    verify(row, times(1)).get("\"count_column\"", long.class);
  }

  @Test
  void toSelector() {
    Assertions.assertThat(countAggregationField.toSelector(true).getAlias()).isEqualTo(CqlIdentifier.fromCql("count_column"));
  }
}