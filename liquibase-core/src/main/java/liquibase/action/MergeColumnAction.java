package liquibase.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import liquibase.change.AddColumnConfig;
import liquibase.change.ChangeMetaData;
import liquibase.change.ColumnConfig;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.change.ExecutableChange;
import liquibase.change.core.DropColumnChange;
import liquibase.change.core.MergeColumnChange;
import liquibase.database.Database;
import liquibase.database.core.DerbyDatabase;
import liquibase.database.core.SQLiteDatabase;
import liquibase.database.core.SQLiteDatabase.AlterTableVisitor;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.RawSqlStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.Index;

import org.kohsuke.MetaInfServices;

/**
 * Combines data from two existing columns into a new column and drops the original columns.
 */
@DatabaseChange(name="mergeColumns", description = "Concatenates the values in two columns, joins them by with string, and stores the resulting value in a new column.", priority = ChangeMetaData.PRIORITY_DEFAULT)
@MetaInfServices(ExecutableChange.class)
public class MergeColumnAction extends AbstractAction<MergeColumnChange> {

    public MergeColumnAction() {
        super(new MergeColumnChange());
    }

    public MergeColumnAction(MergeColumnChange change) {
        super(change);
    }

    @Override
    public boolean supports(Database database) {
        return super.supports(database) && !(database instanceof DerbyDatabase);
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

    public String getColumn1Name() {
        return change.getColumn1Name();
    }

    public void setColumn1Name(String column1Name) {
        change.setColumn1Name(column1Name);
    }

    @DatabaseChangeProperty(description = "String to place include between the values from column1 and column2 (may be empty)", exampleValue = " ")
    public String getJoinString() {
        return change.getJoinString();
    }

    public void setJoinString(String joinString) {
        change.setJoinString(joinString);
    }

    @DatabaseChangeProperty(description = "Name of the column containing the second half of the data", exampleValue = "last_name")
    public String getColumn2Name() {
        return change.getColumn2Name();
    }

    public void setColumn2Name(String column2Name) {
        change.setColumn2Name(column2Name);
    }

    @DatabaseChangeProperty(description = "Name of the column to create", exampleValue = "full_name")
    public String getFinalColumnName() {
        return change.getFinalColumnName();
    }

    public void setFinalColumnName(String finalColumnName) {
        change.setFinalColumnName(finalColumnName);
    }

    @DatabaseChangeProperty(description = "Data type of the column to create", exampleValue = "varchar(255)")
    public String getFinalColumnType() {
        return change.getFinalColumnType();
    }

    public void setFinalColumnType(String finalColumnType) {
        change.setFinalColumnType(finalColumnType);
    }

    @Override
    public boolean generateStatementsVolatile(Database database) {
        if (database instanceof SQLiteDatabase) {
            return true;
        }
        return false;
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        List<SqlStatement> statements = new ArrayList<SqlStatement>();

        AddColumnAction addNewColumnAction = new AddColumnAction();
        addNewColumnAction.setSchemaName(getSchemaName());
        addNewColumnAction.setTableName(getTableName());
        AddColumnConfig columnConfig = new AddColumnConfig();
        columnConfig.setName(getFinalColumnName());
        columnConfig.setType(getFinalColumnType());
        addNewColumnAction.addColumn(columnConfig);
        statements.addAll(Arrays.asList(addNewColumnAction.generateStatements(database)));

        String updateStatement = "UPDATE " + database.escapeTableName(getCatalogName(), getSchemaName(), getTableName()) +
                " SET " + database.escapeObjectName(getFinalColumnName(), Column.class)
                + " = " + database.getConcatSql(database.escapeObjectName(getColumn1Name(), Column.class)
                , "'" + getJoinString() + "'", database.escapeObjectName(getColumn2Name(), Column.class));

        statements.add(new RawSqlStatement(updateStatement));

        if (database instanceof SQLiteDatabase) {
            // SQLite does not support this ALTER TABLE operation until now.
			// For more information see: http://www.sqlite.org/omitted.html
			// This is a small work around...

			// define alter table logic
    		AlterTableVisitor rename_alter_visitor = new AlterTableVisitor() {
    			@Override
                public ColumnConfig[] getColumnsToAdd() {
    				ColumnConfig[] new_columns = new ColumnConfig[1];
    				ColumnConfig new_column = new ColumnConfig();
    		        new_column.setName(getFinalColumnName());
    		        new_column.setType(getFinalColumnType());
    				new_columns[0] = new_column;
    				return new_columns;
    			}
    			@Override
                public boolean copyThisColumn(ColumnConfig column) {
    				return !(column.getName().equals(getColumn1Name()) ||
    						column.getName().equals(getColumn2Name()));
    			}
    			@Override
                public boolean createThisColumn(ColumnConfig column) {
    				return !(column.getName().equals(getColumn1Name()) ||
    						column.getName().equals(getColumn2Name()));
    			}
    			@Override
                public boolean createThisIndex(Index index) {
    				return !(index.getColumns().contains(getColumn1Name()) ||
    						index.getColumns().contains(getColumn2Name()));
    			}
    		};

        	try {
        		// alter table
				statements.addAll(SQLiteDatabase.getAlterTableStatements(
						rename_alter_visitor,
						database,getCatalogName(), getSchemaName(),getTableName()));
    		} catch (Exception e) {
				e.printStackTrace();
			}

        } else {
        	// ...if it is not a SQLite database

	        DropColumnAction dropColumn1Action = new DropColumnAction();
	        DropColumnChange dropColumn1Change = dropColumn1Action.getChange();
	        dropColumn1Change.setSchemaName(getSchemaName());
	        dropColumn1Change.setTableName(getTableName());
	        dropColumn1Change.setColumnName(getColumn1Name());
	        statements.addAll(Arrays.asList(dropColumn1Action.generateStatements(database)));

	        DropColumnAction dropColumn2Action = new DropColumnAction();
                DropColumnChange dropColumn2Change = dropColumn2Action.getChange();
	        dropColumn2Change.setSchemaName(getSchemaName());
	        dropColumn2Change.setTableName(getTableName());
	        dropColumn2Change.setColumnName(getColumn2Name());
	        statements.addAll(Arrays.asList(dropColumn2Action.generateStatements(database)));

        }
        return statements.toArray(new SqlStatement[statements.size()]);

    }

    @Override
    public String getConfirmationMessage() {
        return "Columns "+getTableName()+"."+getColumn1Name()+" and "+getTableName()+"."+getColumn2Name()+" merged";
    }
}
