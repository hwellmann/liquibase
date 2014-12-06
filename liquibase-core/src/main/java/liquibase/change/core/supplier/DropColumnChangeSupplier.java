package liquibase.change.core.supplier;

import liquibase.change.ColumnConfig;
import liquibase.change.IChange;
import liquibase.change.core.CreateTableChange;
import liquibase.change.core.DropColumnChange;
import liquibase.diff.DiffResult;
import liquibase.sdk.supplier.change.AbstractChangeSupplier;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;
import static liquibase.Assert.assertNotNull;

public class DropColumnChangeSupplier extends AbstractChangeSupplier<DropColumnChange>  {

    public DropColumnChangeSupplier() {
        super(DropColumnChange.class);
    }

    @Override
    public IChange[]  prepareDatabase(DropColumnChange change) throws Exception {
        CreateTableChange createTableChange = new CreateTableChange();
        createTableChange.setCatalogName(change.getCatalogName());
        createTableChange.setSchemaName(change.getSchemaName());
        createTableChange.setTableName(change.getTableName());
        createTableChange.addColumn(new ColumnConfig().setName(change.getColumnName()).setType("int"));
        createTableChange.addColumn(new ColumnConfig().setName("other_col").setType("int"));

        return new IChange[] {createTableChange };
    }

    @Override
    public void checkDiffResult(DiffResult diffResult, DropColumnChange change) {
        assertNotNull(diffResult.getMissingObject(new Column(Table.class, change.getCatalogName(), change.getSchemaName(), change.getTableName(), change.getColumnName())));
    }
}
