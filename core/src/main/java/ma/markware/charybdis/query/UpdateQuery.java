package ma.markware.charybdis.query;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.update.Update;
import com.datastax.oss.driver.api.querybuilder.update.UpdateStart;
import com.datastax.oss.driver.api.querybuilder.update.UpdateWithAssignments;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import ma.markware.charybdis.dsl.CriteriaExpression;
import ma.markware.charybdis.model.metadata.ColumnMetadata;
import ma.markware.charybdis.model.metadata.TableMetadata;
import ma.markware.charybdis.query.clause.AssignmentClause;
import ma.markware.charybdis.query.clause.ConditionClause;
import ma.markware.charybdis.query.clause.WhereClause;
import org.apache.commons.lang3.ArrayUtils;

public class UpdateQuery extends AbstractQuery {

  private String keyspace;
  private String table;
  private List<AssignmentClause> assignmentClauses = new ArrayList<>();
  private List<WhereClause> whereClauses = new ArrayList<>();
  private List<ConditionClause> conditionClauses = new ArrayList<>();
  private Integer ttl;
  private Long timestamp;
  private boolean ifExists;

  public void setTable(TableMetadata tableMetadata) {
    keyspace = tableMetadata.getKeyspaceName();
    table = tableMetadata.getTableName();
  }

  public <T> void setAssignment(ColumnMetadata<T> columnMetadata, T value) {
    assignmentClauses.add(AssignmentClause.from(columnMetadata, value));
  }

  public void setAssignments(Map<String, Object> columnNameValues) {
    for (Entry<String, Object> entry : columnNameValues.entrySet()) {
      assignmentClauses.add(AssignmentClause.from(entry.getKey(), entry.getValue()));
    }
  }

  public void setWhere(CriteriaExpression criteriaExpression) {
    whereClauses.add(WhereClause.from(criteriaExpression));
  }

  public void setIf(CriteriaExpression criteriaExpression) {
    conditionClauses.add(ConditionClause.from(criteriaExpression));
  }

  public void setTtl(int ttl) {
    this.ttl = ttl;
  }

  public void setTimestamp(Instant timestamp) {
    this.timestamp = timestamp.toEpochMilli();
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public void enableIfExists() {
    this.ifExists = true;
  }

  @Override
  public ResultSet execute(final CqlSession session) {
    UpdateStart updateStart = QueryBuilder.update(keyspace, table);

    if (ttl != null) {
      updateStart = updateStart.usingTtl(ttl);
    }

    if (timestamp != null) {
      updateStart = updateStart.usingTimestamp(timestamp);
    }

    UpdateWithAssignments updateWithAssignments = updateStart.set(QueryHelper.extractAssignments(assignmentClauses));

    Update update = updateWithAssignments.where(QueryHelper.extractRelations(whereClauses));

    if (ifExists) {
      update = update.ifExists();
    }

    update = update.if_(QueryHelper.extractConditions(conditionClauses));

    SimpleStatement simpleStatement = update.build();
    return executeStatement(session, simpleStatement, ArrayUtils.addAll(QueryHelper.extractAssignmentBindValues(assignmentClauses),
                                                                        QueryHelper.extractWhereBindValues(whereClauses),
                                                                        QueryHelper.extractConditionBindValues(conditionClauses)));
  }
}
