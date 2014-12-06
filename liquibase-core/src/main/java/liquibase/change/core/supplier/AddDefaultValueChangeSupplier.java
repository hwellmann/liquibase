package liquibase.change.core.supplier;

import liquibase.change.ColumnConfig;
import liquibase.change.IChange;
import liquibase.change.core.AddDefaultValueChange;
import liquibase.change.core.CreateTableChange;
import liquibase.diff.DiffResult;
import liquibase.diff.ObjectDifferences;
import liquibase.sdk.supplier.change.AbstractChangeSupplier;
import liquibase.structure.core.Column;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;
import static liquibase.Assert.assertNull;
import static liquibase.Assert.assertNotNull;

public class AddDefaultValueChangeSupplier extends AbstractChangeSupplier<AddDefaultValueChange> {

    public AddDefaultValueChangeSupplier() {
        super(AddDefaultValueChange.class);
    }

    @Override
    public IChange[] prepareDatabase(AddDefaultValueChange change) throws Exception {
        CreateTableChange createTableChange = new CreateTableChange();
        createTableChange.setCatalogName(change.getCatalogName());
        createTableChange.setSchemaName(change.getSchemaName());
        createTableChange.setTableName(change.getTableName());
        createTableChange.addColumn(new ColumnConfig().setName("other_col").setType("int"));

        String type = change.getColumnDataType();
        if (type == null) {
            if (change.getDefaultValueDate() != null) {
                type = "datetime";
            } else if (change.getDefaultValueBoolean() != null) {
                type = "boolean";
            } else if (change.getDefaultValueNumeric() != null) {
                type = "decimal";
            } else {
                type = "varchar(255)";
            }
        }
        createTableChange.addColumn(new ColumnConfig().setName(change.getColumnName()).setType(type));

        return new IChange[]{createTableChange};
    }

    @Override
    public void checkDiffResult(DiffResult diffResult, AddDefaultValueChange change) {
        Column example = new Column().setName(change.getColumnName()).setRelation(new Table().setName(change.getTableName()).setSchema(new Schema(change.getCatalogName(), change.getSchemaName())));

        ObjectDifferences diff = diffResult.getChangedObject(example);
        assertNotNull(diff);
        assertNotNull(diff.getDifference("defaultValue"));
        assertNull(diff.getDifference("defaultValue").getReferenceValue());
        assertNotNull(diff.getDifference("defaultValue").getComparedValue());
    }
}
