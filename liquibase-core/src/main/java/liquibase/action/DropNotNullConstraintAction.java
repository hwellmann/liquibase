package liquibase.action;

import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeStatus;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.change.ExecutableChange;
import liquibase.change.core.DropNotNullConstraintChange;
import liquibase.database.Database;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.SetNullableStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;

import org.kohsuke.MetaInfServices;

/**
 * Drops a not-null constraint from an existing column.
 */
@DatabaseChange(name="dropNotNullConstraint", description = "Makes a column nullable", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "column")
@MetaInfServices(ExecutableChange.class)
public class DropNotNullConstraintAction extends AbstractAction<DropNotNullConstraintChange> {

    public DropNotNullConstraintAction() {
        super(new DropNotNullConstraintChange());
    }

    public DropNotNullConstraintAction(DropNotNullConstraintChange change) {
        super(change);
    }

    public String getCatalogName() {
        return change.getCatalogName();
    }

    public void setCatalogName(String catalogName) {
        change.setCatalogName(catalogName);
    }

    @DatabaseChangeProperty(mustEqualExisting ="notNullConstraint.table.schema")
    public String getSchemaName() {
        return change.getSchemaName();
    }

    public void setSchemaName(String schemaName) {
        change.setSchemaName(schemaName);
    }

    @DatabaseChangeProperty(mustEqualExisting = "notNullConstraint.table", description = "Name of the table containing that the column to drop the constraint from")
    public String getTableName() {
        return change.getTableName();
    }

    public void setTableName(String tableName) {
        change.setTableName(tableName);
    }

    @DatabaseChangeProperty(mustEqualExisting = "notNullConstraint.column", description = "Name of the column to drop the constraint from")
    public String getColumnName() {
        return change.getColumnName();
    }

    public void setColumnName(String columnName) {
        change.setColumnName(columnName);
    }

    @DatabaseChangeProperty(description = "Current data type of the column")
    public String getColumnDataType() {
        return change.getColumnDataType();
    }

    public void setColumnDataType(String columnDataType) {
        change.setColumnDataType(columnDataType);
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {

//todo    	if (database instanceof SQLiteDatabase) {
//    		// return special statements for SQLite databases
//    		return generateStatementsForSQLiteDatabase(database);
//    	}

    	return new SqlStatement[] { new SetNullableStatement(
                getCatalogName(),
    			getSchemaName(),
    			getTableName(), getColumnName(), getColumnDataType(), true)
    	};
    }

    @Override
    public ChangeStatus checkStatus(Database database) {
        try {
            Column snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(new Column(Table.class, getCatalogName(), getSchemaName(), getTableName(), getColumnName()), database);
            Boolean nullable = snapshot.isNullable();
            return new ChangeStatus().assertComplete(nullable == null || nullable, "Column is not null");
        } catch (Exception e) {
            return new ChangeStatus().unknown(e);
        }

    }

//    private SqlStatement[] generateStatementsForSQLiteDatabase(Database database) {
//    	// SQLite does not support this ALTER TABLE operation until now.
//		// For more information see: http://www.sqlite.org/omitted.html.
//		// This is a small work around...
//
//    	List<SqlStatement> statements = new ArrayList<SqlStatement>();
//
//		// define alter table logic
//		AlterTableVisitor rename_alter_visitor = new AlterTableVisitor() {
//			public ColumnConfig[] getColumnsToAdd() {
//				return new ColumnConfig[0];
//			}
//			public boolean copyThisColumn(ColumnConfig column) {
//				return true;
//			}
//			public boolean createThisColumn(ColumnConfig column) {
//				if (column.getName().equals(getColumnName())) {
//					column.getConstraints().setNullable(true);
//				}
//				return true;
//			}
//			public boolean createThisIndex(Index index) {
//				return true;
//			}
//		};
//
//    	try {
//    		// alter table
//			statements.addAll(SQLiteDatabase.getAlterTableStatements(
//					rename_alter_visitor,
//					database,getCatalogName(), getSchemaName(),getTableName()));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return statements.toArray(new SqlStatement[statements.size()]);
//    }

    @Override
    protected ExecutableChange[] createInverses() {
        AddNotNullConstraintAction inverse = new AddNotNullConstraintAction();
        inverse.setColumnName(getColumnName());
        inverse.setSchemaName(getSchemaName());
        inverse.setTableName(getTableName());
        inverse.setColumnDataType(getColumnDataType());

        return new ExecutableChange[]{
                inverse
        };
    }

    @Override
    public String getConfirmationMessage() {
        return "Null constraint dropped from " + getTableName() + "." + getColumnName();
    }
}
