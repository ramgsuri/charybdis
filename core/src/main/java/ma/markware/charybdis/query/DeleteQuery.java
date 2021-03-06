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
package ma.markware.charybdis.query;

import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.delete.Delete;
import com.datastax.oss.driver.api.querybuilder.delete.DeleteSelection;
import com.datastax.oss.driver.api.querybuilder.select.Selector;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import ma.markware.charybdis.ExecutionContext;
import ma.markware.charybdis.model.criteria.CriteriaExpression;
import ma.markware.charybdis.model.field.DeletableField;
import ma.markware.charybdis.model.field.metadata.TableMetadata;
import ma.markware.charybdis.query.clause.ConditionClause;
import ma.markware.charybdis.query.clause.WhereClause;

/**
 * Delete query.
 *
 * @author Oussama Markad
 */
public class DeleteQuery extends AbstractQuery {

  private String keyspace;
  private String table;
  private List<Selector> selectors = new ArrayList<>();
  private List<WhereClause> whereClauses = new ArrayList<>();
  private List<ConditionClause> conditionClauses = new ArrayList<>();
  private Long timestamp;

  public DeleteQuery(@Nonnull ExecutionContext executionContext) {
    super(executionContext);
  }

  public DeleteQuery() {
    super(new ExecutionContext());
  }

  public String getKeyspace() {
    return keyspace;
  }

  public String getTable() {
    return table;
  }

  public List<Selector> getSelectors() {
    return selectors;
  }

  public List<WhereClause> getWhereClauses() {
    return whereClauses;
  }

  public List<ConditionClause> getConditionClauses() {
    return conditionClauses;
  }

  public Long getTimestamp() {
    return timestamp;
  }

  public void setTable(TableMetadata tableMetadata) {
    keyspace = tableMetadata.getKeyspaceName();
    table = tableMetadata.getTableName();
    executionContext.setDefaultConsistencyLevel(tableMetadata.getDefaultWriteConsistency());
  }

  public void setSelectors(DeletableField... fields) {
    for(DeletableField field : fields) {
      this.selectors.add(field.toDeletableSelector());
    }
  }

  public void setTimestamp(Instant timestamp) {
    this.timestamp = timestamp.toEpochMilli();
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public void setWhere(CriteriaExpression criteriaExpression) {
    whereClauses.add(WhereClause.from(criteriaExpression));
  }

  public void setIf(CriteriaExpression criteriaExpression) {
    conditionClauses.add(ConditionClause.from(criteriaExpression));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public StatementTuple buildStatement() {
    DeleteSelection deleteSelection = QueryBuilder.deleteFrom(keyspace, table);

    if (!selectors.isEmpty()) {
      deleteSelection = deleteSelection.selectors(this.selectors);
    }

    if (timestamp != null) {
      deleteSelection = deleteSelection.usingTimestamp(timestamp);
    }

    Delete delete = deleteSelection.where(QueryHelper.extractRelations(whereClauses));

    delete = delete.if_(QueryHelper.extractConditions(conditionClauses));

    SimpleStatement simpleStatement = delete.build();
    return new StatementTuple(simpleStatement, Stream.of(QueryHelper.extractWhereBindValues(whereClauses),
                                                                QueryHelper.extractConditionBindValues(conditionClauses))
                                                            .flatMap(Function.identity())
                                                            .toArray());
  }
}
