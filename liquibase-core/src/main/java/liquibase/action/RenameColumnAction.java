package liquibase.action;

import java.util.ArrayList;
import java.util.List;

import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeStatus;
import liquibase.change.ColumnConfig;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.change.ExecutableChange;
import liquibase.change.core.RenameColumnChange;
import liquibase.database.Database;
import liquibase.database.core.SQLiteDatabase;
import liquibase.database.core.SQLiteDatabase.AlterTableVisitor;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.RenameColumnStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.Index;
import liquibase.structure.core.Table;

import org.kohsuke.MetaInfServices;

/**
 * Renames an existing column.
 */
@DatabaseChange(name="renameColumn", description = "Renames an existing column", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "column")
@MetaInfServices(ExecutableChange.class)
public class RenameColumnAction extends AbstractAction<RenameColumnChange> {

    public RenameColumnAction() {
        super(new RenameColumnChange());
    }

    public RenameColumnAction(RenameColumnChange change) {
        super(change);
    }

    public String getCatalogName() {
        return change.getCatalogName();
    }

    public void setCatalogName(String catalogName) {
        change.setCatalogName(catalogName);
    }

    @DatabaseChangeProperty(mustEqualExisting ="column.relation.schema")
    public String getSchemaName() {
        return change.getSchemaName();
    }

    public void setSchemaName(String schemaName) {
        change.setSchemaName(schemaName);
    }

    @DatabaseChangeProperty(mustEqualExisting = "column.relation", description = "Name of the table containing that the column to rename")
    public String getTableName() {
        return change.getTableName();
    }

    public void setTableName(String tableName) {
        change.setTableName(tableName);
    }

    @DatabaseChangeProperty(mustEqualExisting = "column", exampleValue = "name", description = "Name of the existing column to rename")
    public String getOldColumnName() {
        return change.getOldColumnName();
    }

    public void setOldColumnName(String oldColumnName) {
        change.setOldColumnName(oldColumnName);
    }

    @DatabaseChangeProperty(description = "Name to rename the column to", exampleValue = "full_name")
    public String getNewColumnName() {
        return change.getNewColumnName();
    }

    public void setNewColumnName(String newColumnName) {
        change.setNewColumnName(newColumnName);
    }

    @DatabaseChangeProperty(description = "Data type of the column")
    public String getColumnDataType() {
        return change.getColumnDataType();
    }

    public void setColumnDataType(String columnDataType) {
        change.setColumnDataType(columnDataType);
    }

    @DatabaseChangeProperty(description = "Remarks of the column")
    public String getRemarks() {
        return change.getRemarks();
    }

    public void setRemarks(String remarks) {
        change.setRemarks(remarks);
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
//todo    	if (database instanceof SQLiteDatabase) {
//    		// return special statements for SQLite databases
//    		return generateStatementsForSQLiteDatabase(database);
//        }

    	return new SqlStatement[] { new RenameColumnStatement(
                getCatalogName(),
                getSchemaName(),
    			getTableName(), getOldColumnName(), getNewColumnName(),
    			getColumnDataType(),getRemarks())
        };
    }

    @Override
    public ChangeStatus checkStatus(Database database) {
        try {
            ChangeStatus changeStatus = new ChangeStatus();
            Column newColumn = SnapshotGeneratorFactory.getInstance().createSnapshot(new Column(Table.class, getCatalogName(), getSchemaName(), getTableName(), getNewColumnName()), database);
            Column oldColumn = SnapshotGeneratorFactory.getInstance().createSnapshot(new Column(Table.class, getCatalogName(), getSchemaName(), getTableName(), getOldColumnName()), database);

            if (newColumn == null && oldColumn == null) {
                return changeStatus.unknown("Neither column exists");
            }
            if (newColumn != null && oldColumn != null) {
                return changeStatus.unknown("Both columns exist");
            }
            changeStatus.assertComplete(newColumn != null, "New column does not exist");

            return changeStatus;
        } catch (Exception e) {
            return new ChangeStatus().unknown(e);
        }
    }

    private SqlStatement[] generateStatementsForSQLiteDatabase(Database database) {

    	// SQLite does not support this ALTER TABLE operation until now.
		// For more information see: http://www.sqlite.org/omitted.html.
		// This is a small work around...

    	List<SqlStatement> statements = new ArrayList<SqlStatement>();

    	// define alter table logic
		AlterTableVisitor rename_alter_visitor =
		new AlterTableVisitor() {
			@Override
            public ColumnConfig[] getColumnsToAdd() {
				return new ColumnConfig[0];
			}
			@Override
            public boolean copyThisColumn(ColumnConfig column) {
				return true;
			}
			@Override
            public boolean createThisColumn(ColumnConfig column) {
				if (column.getName().equals(getOldColumnName())) {
					column.setName(getNewColumnName());
				}
				return true;
			}
			@Override
            public boolean createThisIndex(Index index) {
				if (index.getColumns().contains(getOldColumnName())) {
					index.getColumns().remove(getOldColumnName());
					index.addColumn(new Column(getNewColumnName()).setRelation(index.getTable()));
				}
				return true;
			}
		};

    	try {
    		// alter table
			statements.addAll(SQLiteDatabase.getAlterTableStatements(
					rename_alter_visitor,
					database,getCatalogName(), getSchemaName(),getTableName()));
		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}

    	return statements.toArray(new SqlStatement[statements.size()]);
    }

    @Override
    protected ExecutableChange[] createInverses() {
        RenameColumnAction inverse = new RenameColumnAction();
        inverse.setSchemaName(getSchemaName());
        inverse.setTableName(getTableName());
        inverse.setOldColumnName(getNewColumnName());
        inverse.setNewColumnName(getOldColumnName());
        inverse.setColumnDataType(getColumnDataType());

        return new ExecutableChange[]{
                inverse
        };
    }

    @Override
    public String getConfirmationMessage() {
        return "Column "+ getTableName() +"."+ getOldColumnName() + " renamed to " + getNewColumnName();
    }
}
