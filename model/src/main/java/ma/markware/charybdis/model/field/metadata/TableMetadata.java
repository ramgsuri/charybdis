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
package ma.markware.charybdis.model.field.metadata;

import com.datastax.oss.driver.api.core.cql.Row;
import java.time.Instant;
import java.util.Map;
import ma.markware.charybdis.model.option.ConsistencyLevel;
import ma.markware.charybdis.model.option.SerialConsistencyLevel;

/**
 * Table metadata.
 *
 * @param <ENTITY> java class representation of a Cql table.
 *
 * @author Oussama Markad
 */
public interface TableMetadata<ENTITY> {

  /**
   * @return table keyspace name.
   */
  String getKeyspaceName();

  /**
   * @return table name.
   */
  String getTableName();

  /**
   * @return Table default write consistency.
   */
  ConsistencyLevel getDefaultWriteConsistency();

  /**
   * @return Table default read consistency.
   */
  ConsistencyLevel getDefaultReadConsistency();

  /**
   * @return Table default serial consistency for LWT queries.
   */
  SerialConsistencyLevel getDefaultSerialConsistency();

  /**
   * @return metadata of a given column.
   */
  ColumnMetadata getColumnMetadata(String columnName);

  /**
   * @return column metadata by column name mapping.
   */
  Map<String, ColumnMetadata> getColumnsMetadata();

  /**
   * @return partition key column metadata by column name mapping.
   */
  Map<String, ColumnMetadata> getPartitionKeyColumns();

  /**
   * @return clustering key column metadata by column name mapping.
   */
  Map<String, ColumnMetadata> getClusteringKeyColumns();

  /**
   * @return primary key column metadata by column name mapping.
   */
  Map<String, ColumnMetadata> getPrimaryKeys();

  /**
   * @return is a given column a primary key.
   */
  boolean isPrimaryKey(String columnName);

  /**
   * @return primary key size.
   */
  int getPrimaryKeySize();

  /**
   * @return number of columns.
   */
  int getColumnsSize();

  /**
   * Set auto-generated values in given entity.
   */
  void setGeneratedValues(ENTITY entity);

  /**
   * Set creation date to given entity.
   */
  void setCreationDate(ENTITY entity, Instant creationDate);

  /**
   * Set last updated date to given entity.
   */
  void setLastUpdatedDate(ENTITY entity, Instant lastUpdatedDate);

  /**
   * @return serialized entity as a column-value map.
   */
  Map<String, Object> serialize(ENTITY entity);

  /**
   * @return deserialized java entity from Cql row.
   */
  ENTITY deserialize(Row row);
}
