package ma.markware.charybdis.dsl.delete;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import java.time.Instant;
import ma.markware.charybdis.dsl.CriteriaExpression;
import ma.markware.charybdis.model.metadata.ColumnMetadata;
import ma.markware.charybdis.model.metadata.TableMetadata;
import ma.markware.charybdis.query.DeleteQuery;

public class DeleteImpl implements DeleteInitExpression, DeleteUsingTimestampExpression, DeleteWhereExpression, DeleteExtraWhereExpression, DeleteOnExistExpression,
    DeleteIfExpression, DeleteExtraIfExpression, DeleteExecuteExpression {

  private final CqlSession session;
  private final DeleteQuery deleteQuery;

  public DeleteImpl(final CqlSession session) {
    this.session = session;
    this.deleteQuery = new DeleteQuery();
  }

  public DeleteInitExpression delete() {
    return this;
  }

  public DeleteInitExpression delete(ColumnMetadata... columnsMetadata) {
    deleteQuery.addSelectors(columnsMetadata);
    return this;
  }

  @Override
  public DeleteUsingTimestampExpression from(final TableMetadata table) {
    deleteQuery.addTable(table);
    return this;
  }

  @Override
  public DeleteWhereExpression usingTimestamp(final Instant timestamp) {
    deleteQuery.addTimestamp(timestamp);
    return this;
  }

  @Override
  public DeleteWhereExpression usingTimestamp(final long timestamp) {
    deleteQuery.addTimestamp(timestamp);
    return this;
  }

  @Override
  public DeleteExtraWhereExpression where(final CriteriaExpression condition) {
    deleteQuery.addWhere(condition);
    return this;
  }

  @Override
  public DeleteExtraWhereExpression and(final CriteriaExpression condition) {
    deleteQuery.addWhere(condition);
    return this;
  }

  @Override
  public DeleteExtraIfExpression if_(final CriteriaExpression condition) {
    deleteQuery.addIf(condition);
    return this;
  }

  @Override
  public DeleteExtraIfExpression and_(final CriteriaExpression condition) {
    deleteQuery.addIf(condition);
    return this;
  }

  @Override
  public DeleteExecuteExpression ifExists() {
    deleteQuery.enableIfExists();
    return this;
  }

  @Override
  public boolean execute() {
    ResultSet resultSet = deleteQuery.execute(session);
    return resultSet.wasApplied();
  }
}