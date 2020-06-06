package ma.markware.charybdis.model.field.metadata;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import ma.markware.charybdis.model.assignment.AssignmentOperation;
import ma.markware.charybdis.model.assignment.AssignmentSetValue;
import ma.markware.charybdis.model.criteria.CriteriaExpression;
import ma.markware.charybdis.model.criteria.CriteriaOperator;

public interface SetColumnMetadata<T> extends CollectionColumnMetadata<Set<T>> {

  default CriteriaExpression contains(T value) {
    return new CriteriaExpression(this, CriteriaOperator.CONTAINS, value);
  }

  default AssignmentSetValue<T> append(T... values) {
    return append(Arrays.stream(values).collect(Collectors.toSet()));
  }

  default AssignmentSetValue<T> append(Set<T> values) {
    return new AssignmentSetValue<>(this, AssignmentOperation.APPEND, serialize(values));
  }

  default AssignmentSetValue<T> prepend(T... values) {
    return prepend(Arrays.stream(values).collect(Collectors.toSet()));
  }

  default AssignmentSetValue<T> prepend(Set<T> values) {
    return new AssignmentSetValue<T>(this, AssignmentOperation.PREPEND, serialize(values));
  }

  default AssignmentSetValue<T> remove(T... values) {
    return remove(Arrays.stream(values).collect(Collectors.toSet()));
  }

  default AssignmentSetValue<T> remove(Set<T> values) {
    return new AssignmentSetValue<>(this, AssignmentOperation.REMOVE, values);
  }
}
