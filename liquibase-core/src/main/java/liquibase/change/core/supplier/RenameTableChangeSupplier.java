package liquibase.change.core.supplier;

import liquibase.change.ColumnConfig;
import liquibase.change.IChange;
import liquibase.change.core.CreateTableChange;
import liquibase.change.core.RenameTableChange;
import liquibase.diff.DiffResult;
import liquibase.sdk.supplier.change.AbstractChangeSupplier;
import liquibase.structure.core.Table;
import static liquibase.Assert.assertNotNull;

public class RenameTableChangeSupplier extends AbstractChangeSupplier<RenameTableChange>  {

    public RenameTableChangeSupplier() {
        super(RenameTableChange.class);
    }

    @Override
    public IChange[]  prepareDatabase(RenameTableChange change) throws Exception {
        CreateTableChange createTableChange = new CreateTableChange();
        createTableChange.setCatalogName(change.getCatalogName());
        createTableChange.setSchemaName(change.getSchemaName());
        createTableChange.setTableName(change.getOldTableName());
        createTableChange.addColumn(new ColumnConfig().setName("id").setType("int"));
        createTableChange.addColumn(new ColumnConfig().setName("other_column").setType("varchar(10)"));

        return new IChange[] {createTableChange };
    }

    @Override
    public void checkDiffResult(DiffResult diffResult, RenameTableChange change) {
        assertNotNull(diffResult.getMissingObject(new Table(change.getCatalogName(), change.getSchemaName(), change.getOldTableName())));
        assertNotNull(diffResult.getUnexpectedObject(new Table(change.getCatalogName(), change.getSchemaName(), change.getNewTableName())));
    }
}
