package liquibase.action;

import java.util.ArrayList;
import java.util.List;

import liquibase.change.ChangeMetaData;
import liquibase.change.ColumnConfig;
import liquibase.change.DatabaseChange;
import liquibase.change.ExecutableChange;
import liquibase.change.core.AddNotNullConstraintChange;
import liquibase.change.core.DropNotNullConstraintChange;
import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.database.core.SQLiteDatabase;
import liquibase.database.core.SQLiteDatabase.AlterTableVisitor;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.ReorganizeTableStatement;
import liquibase.statement.core.SetNullableStatement;
import liquibase.statement.core.UpdateStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.Index;

import org.kohsuke.MetaInfServices;

/**
 * Adds a not-null constraint to an existing column.
 */
@DatabaseChange(name="addNotNullConstraint",
        description = "Adds a not-null constraint to an existing table. If a defaultNullValue attribute is passed, all null values for the column will be updated to the passed value before the constraint is applied.",
        priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "column")
@MetaInfServices(ExecutableChange.class)
public class AddNotNullConstraintAction extends AbstractAction<AddNotNullConstraintChange> {


    public AddNotNullConstraintAction() {
        super(new AddNotNullConstraintChange());
    }

    public AddNotNullConstraintAction(AddNotNullConstraintChange change) {
        super(change);
    }

    public String getCatalogName() {
        return change.getCatalogName();
    }

    public void setCatalogName(String catalogName) {
        change.setCatalogName(catalogName);
    }

    public String getSchemaName() {
        return change.getSchemaName();
    }

    public void setSchemaName(String schemaName) {
        change.setSchemaName(schemaName);
    }

    public String getTableName() {
        return change.getTableName();
    }

    public void setTableName(String tableName) {
        change.setTableName(tableName);
    }

    public String getColumnName() {
        return change.getColumnName();
    }

    public void setColumnName(String columnName) {
        change.setColumnName(columnName);
    }

    public String getDefaultNullValue() {
        return change.getDefaultNullValue();
    }

    public void setDefaultNullValue(String defaultNullValue) {
        change.setDefaultNullValue(defaultNullValue);
    }

    public String getColumnDataType() {
        return change.getColumnDataType();
    }

    public void setColumnDataType(String columnDataType) {
        change.setColumnDataType(columnDataType);
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {

////        if (database instanceof SQLiteDatabase) {
//    		// return special statements for SQLite databases
//    		return generateStatementsForSQLiteDatabase(database);
//        }

    	List<SqlStatement> statements = new ArrayList<SqlStatement>();

        if (getDefaultNullValue() != null) {
            statements.add(new UpdateStatement(getCatalogName(), getSchemaName(), getTableName())
                    .addNewColumnValue(getColumnName(), getDefaultNullValue())
                    .setWhereClause(database.escapeObjectName(getColumnName(), Column.class) + " IS NULL"));
        }

    	statements.add(new SetNullableStatement(getCatalogName(), getSchemaName(), getTableName(), getColumnName(), getColumnDataType(), false));
        if (database instanceof DB2Database) {
            statements.add(new ReorganizeTableStatement(getCatalogName(), getSchemaName(), getTableName()));
        }

        return statements.toArray(new SqlStatement[statements.size()]);
    }

    private SqlStatement[] generateStatementsForSQLiteDatabase(Database database) {

    	// SQLite does not support this ALTER TABLE operation until now.
		// For more information see: http://www.sqlite.org/omitted.html.
		// This is a small work around...

    	List<SqlStatement> statements = new ArrayList<SqlStatement>();

        if (getDefaultNullValue() != null) {
            statements.add(new UpdateStatement(getCatalogName(), getSchemaName(), getTableName())
                    .addNewColumnValue(getColumnName(), getDefaultNullValue())
                    .setWhereClause(getColumnName() + " IS NULL"));
        }

//		// ... test if column contains NULL values
//		if (getDefaultNullValue() == null) {
//			List<Map> null_rows = null;
//			try {
//				null_rows = database.getExecutor().
//					queryForList(new RawSqlStatement(
//						"SELECT * FROM `"+
//						database.escapeTableName(getSchemaName(), getTableName())+
//						"` WHERE `"+getColumnName()+"` IS NULL;"));
//			} catch (DatabaseException e) {
//				e.printStackTrace();
//			}
//    		if (null_rows.size()>0) {
//    			throw new UnsupportedChangeException(
//    					"Failed to add a Not-Null-Constraint because " +
//    					"some values are null. Use the " +
//    					"defaultNullValue attribute to define default " +
//    					"values for the existing null values.");
//    		}
//    	}

		// define alter table logic
		AlterTableVisitor rename_alter_visitor = new AlterTableVisitor() {
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
				if (column.getName().equals(getColumnName())) {
					column.getConstraints().setNullable(false);
				}
				return true;
			}
			@Override
            public boolean createThisIndex(Index index) {
				return true;
			}
		};

		try {
    		// alter table
			statements.addAll(SQLiteDatabase.getAlterTableStatements(rename_alter_visitor, database,getCatalogName(), getSchemaName(),getTableName()));
    	} catch (Exception e) {
			e.printStackTrace();
		}

    	return statements.toArray(new SqlStatement[statements.size()]);
    }

    @Override
    protected ExecutableChange[] createInverses() {
        DropNotNullConstraintChange inverse = new DropNotNullConstraintChange();
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
        return "Null constraint has been added to " + getTableName() + "." + getColumnName();
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}