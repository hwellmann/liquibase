package liquibase.change.core.supplier;

import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.change.IChange;
import liquibase.change.core.CreateTableChange;
import liquibase.change.core.SQLFileChange;
import liquibase.diff.DiffResult;
import liquibase.sdk.supplier.change.AbstractChangeSupplier;

public class SQLFileChangeSupplier extends AbstractChangeSupplier<SQLFileChange>  {

    public SQLFileChangeSupplier() {
        super(SQLFileChange.class);
    }

    @Override
    public IChange[] prepareDatabase(SQLFileChange change) throws Exception {
        CreateTableChange createTableChange = new CreateTableChange();
        createTableChange.setTableName("person");
        createTableChange.addColumn(new ColumnConfig().setName("id").setType("int").setConstraints(new ConstraintsConfig().setNullable(false).setPrimaryKey(true)));
        createTableChange.addColumn(new ColumnConfig().setName("not_id").setType("int"));

        return new IChange[] {createTableChange };
    }

    @Override
    public void checkDiffResult(DiffResult diffResult, SQLFileChange change) throws Exception {
        //todo
    }
}
