package ma.markware.charybdis.model.field;

import ma.markware.charybdis.model.field.entry.EntryExpression;
import ma.markware.charybdis.model.field.metadata.ColumnMetadata;

public interface NestedField<T> extends Field {

  ColumnMetadata getSourceColumn();

  EntryExpression getEntry();
}
