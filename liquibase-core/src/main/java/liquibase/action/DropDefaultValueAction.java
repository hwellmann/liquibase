package liquibase.action;

import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeStatus;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.change.ExecutableChange;
import liquibase.change.core.DropDefaultValueChange;
import liquibase.database.Database;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropDefaultValueStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;

import org.kohsuke.MetaInfServices;

/**
 * Removes the default value from an existing column.
 */
@DatabaseChange(name="dropDefaultValue", description="Removes the database default value for a column", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "column")
@MetaInfServices(ExecutableChange.class)
public class DropDefaultValueAction extends AbstractAction<DropDefaultValueChange> {

    public DropDefaultValueAction() {
        super(new DropDefaultValueChange());
    }

    public DropDefaultValueAction(DropDefaultValueChange change) {
        super(change);
    }

    @DatabaseChangeProperty(mustEqualExisting ="column.relation.catalog", since = "3.0")
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

    @DatabaseChangeProperty(mustEqualExisting = "column.relation", description = "Name of the table to containing the column")
    public String getTableName() {
        return change.getTableName();
    }

    public void setTableName(String tableName) {
        change.setTableName(tableName);
    }

    @DatabaseChangeProperty(mustEqualExisting = "column", description = "Name of column to drop the default value from")
    public String getColumnName() {
        return change.getColumnName();
    }

    public void setColumnName(String columnName) {
        change.setColumnName(columnName);
    }

    @DatabaseChangeProperty()
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
//        }

        return new SqlStatement[]{
                new DropDefaultValueStatement(getCatalogName(), getSchemaName(), getTableName(), getColumnName(), getColumnDataType()),
        };
    }

    @Override
    public ChangeStatus checkStatus(Database database) {
        try {
            Column snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(new Column(Table.class, getCatalogName(), getSchemaName(), getTableName(), getColumnName()), database);
            return new ChangeStatus().assertComplete(snapshot.getDefaultValue() == null, "Column has a default value");
        } catch (Exception e) {
            return new ChangeStatus().unknown(e);
        }

    }

//    private SqlStatement[] generateStatementsForSQLiteDatabase(Database database) {
//
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
//					column.setDefaultValue(null);
//					column.setDefaultValueBoolean(null);
//					column.setDefaultValueDate((Date)null);
//					column.setDefaultValueNumeric((Number)null);
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
//    	} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//    	return statements.toArray(new SqlStatement[statements.size()]);
//    }

    @Override
    public String getConfirmationMessage() {
        return "Default value dropped from "+getTableName()+"."+getColumnName();
    }
}
