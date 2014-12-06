package liquibase.change.core.supplier;

import liquibase.change.ColumnConfig;
import liquibase.change.IChange;
import liquibase.change.core.CreateTableChange;
import liquibase.change.core.DropDefaultValueChange;
import liquibase.diff.DiffResult;
import liquibase.diff.ObjectDifferences;
import liquibase.sdk.supplier.change.AbstractChangeSupplier;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;
import static liquibase.Assert.assertEquals;
import static liquibase.Assert.assertNotNull;

public class DropDefaultValueChangeSupplier extends AbstractChangeSupplier<DropDefaultValueChange>  {

    public DropDefaultValueChangeSupplier() {
        super(DropDefaultValueChange.class);
    }

    @Override
    public IChange[]  prepareDatabase(DropDefaultValueChange change) throws Exception {
        CreateTableChange createTableChange = new CreateTableChange();
        createTableChange.setCatalogName(change.getCatalogName());
        createTableChange.setSchemaName(change.getSchemaName());
        createTableChange.setTableName(change.getTableName());
        createTableChange.addColumn(new ColumnConfig().setName("other_col").setType("int"));
        String dataType = change.getColumnDataType();
        if (dataType == null) {
            dataType = "int";
        }
        createTableChange.addColumn(new ColumnConfig().setName(change.getColumnName()).setType(dataType).setDefaultValue("1"));

        return new IChange[] {createTableChange };

    }

    @Override
    public void checkDiffResult(DiffResult diffResult, DropDefaultValueChange change) {
        ObjectDifferences diff = diffResult.getChangedObject(new Column(Table.class, change.getCatalogName(), change.getSchemaName(), change.getTableName(), change.getColumnName()));
        assertNotNull(diff);

        assertNotNull(diff.getDifference("defaultValue").getReferenceValue());
        assertEquals("NULL", diff.getDifference("defaultValue").getComparedValue().toString());

    }
}
