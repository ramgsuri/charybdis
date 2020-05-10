package ma.markware.charybdis.model.field.aggregation;

import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.querybuilder.select.Selector;
import ma.markware.charybdis.model.field.SelectableField;
import ma.markware.charybdis.model.utils.StringUtils;

public class MinAggregationField<T> implements SelectableField<T> {

  private final SelectableField<T> aggregatedField;

  public MinAggregationField(final SelectableField<T> aggregatedField) {
    this.aggregatedField = aggregatedField;
  }

  @Override
  public T deserialize(final Row row) {
    return row.get(resolveAlias(), getFieldClass());
  }

  @Override
  public Class<T> getFieldClass() {
    return aggregatedField.getFieldClass();
  }

  @Override
  public String getName() {
    return "min_" + aggregatedField.getName();
  }

  @Override
  public Selector toSelector(boolean useAlias) {
    Selector minSelector = Selector.function("min", aggregatedField.toSelector(false));
    return useAlias ? minSelector.as(resolveAlias()) : minSelector;
  }

  private String resolveAlias() {
    return StringUtils.quoteString(getName());
  }
}