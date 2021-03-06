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

package ma.markware.charybdis.dsl;

import ma.markware.charybdis.QueryBuilder;
import ma.markware.charybdis.batch.Batch;
import ma.markware.charybdis.dsl.delete.DeleteInitExpression;
import ma.markware.charybdis.dsl.delete.DslDeleteBatchImpl;
import ma.markware.charybdis.dsl.insert.DslInsertBatchImpl;
import ma.markware.charybdis.dsl.insert.InsertInitExpression;
import ma.markware.charybdis.dsl.insert.InsertInitWithColumnsExpression;
import ma.markware.charybdis.dsl.update.DslUpdateBatchImpl;
import ma.markware.charybdis.dsl.update.UpdateInitExpression;
import ma.markware.charybdis.model.field.DeletableField;
import ma.markware.charybdis.model.field.metadata.ColumnMetadata;
import ma.markware.charybdis.model.field.metadata.TableMetadata;

/**
 * Implementation of {@link QueryBuilder}, handle cql entities using dsl semantics to create batch queries.
 *
 * @author Oussama Markad
 */
public class DslQueryBatchBuilder implements QueryBuilder {

  private final Batch batch;

  public DslQueryBatchBuilder(Batch batch) {
    this.batch = batch;
  }

  /**
   * Create a new DSL insert expression.
   * <p>
   * Example: <code><pre>
   * dslQuery.insertInto(table)
   *         .set(field1, 100)
   *         .set(field2, "test")
   *         .execute();
   * </pre></code>
   *
   * @param table table of insert query.
   * @return initialized insert expression.
   */
  public InsertInitExpression<Void> insertInto(final TableMetadata table) {
    return new DslInsertBatchImpl(batch).insertInto(table);
  }

  /**
   * Create a new DSL insert expression.
   * <p>
   * Example: <code><pre>
   * dslQuery.insertInto(table, field1, field2)
   *         .values(100, "test")
   *         .execute();
   * </pre></code>
   *
   * @param table table of insert query.
   * @param columns columns to insert.
   * @return initialized insert expression.
   */
  public InsertInitWithColumnsExpression<Void> insertInto(TableMetadata table, ColumnMetadata... columns) {
    return new DslInsertBatchImpl(batch).insertInto(table, columns);
  }

  /**
   * Create a new DSL update expression.
   * <p>
   * Example: <code><pre>
   * dslQuery.update(table)
   *         .set(field1, value1)
   *         .set(field2, value2)
   *         .where(field1.eq(10))
   *         .execute();
   * </pre></code>
   *
   * @param table table of update query.
   * @return initialized update expression.
   */
  public UpdateInitExpression<Void> update(TableMetadata table) {
    return new DslUpdateBatchImpl(batch).update(table);
  }

  /**
   * Create a new DSL delete expression.
   * <p>
   * Example: <code><pre>
   * dslQuery.delete()
   *         .from(table)
   *         .where(field1.eq(0))
   *         .and(field2.gt("abc"))
   *         .execute();
   * </pre></code>
   *
   * @return initialized delete expression.
   */
  public DeleteInitExpression<Void> delete() {
    return new DslDeleteBatchImpl(batch).delete();
  }

  /**
   * Create a new DSL delete expression with fields to delete.
   * <p>
   * Example: <code><pre>
   * dslQuery.delete(field2)
   *         .from(table)
   *         .where(field1.lt(1_000))
   *         .execute();
   * </pre></code>
   *
   * @return initialized delete expression.
   */
  public DeleteInitExpression<Void> delete(final DeletableField... fields) {
    return new DslDeleteBatchImpl(batch).delete(fields);
  }
}
