package liquibase.action;

import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeStatus;
import liquibase.change.ColumnConfig;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.change.ExecutableChange;
import liquibase.change.core.AddUniqueConstraintChange;
import liquibase.database.Database;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AddUniqueConstraintStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.UniqueConstraint;

import org.kohsuke.MetaInfServices;

/**
 * Adds a unique constraint to an existing column.
 */
@DatabaseChange(name="addUniqueConstraint", description = "Adds a unique constrant to an existing column or set of columns.", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "column")
@MetaInfServices(ExecutableChange.class)
public class AddUniqueConstraintAction extends AbstractAction<AddUniqueConstraintChange> {

    public AddUniqueConstraintAction() {
        super(new AddUniqueConstraintChange());
    }

    public AddUniqueConstraintAction(AddUniqueConstraintChange change) {
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

    public String getColumnNames() {
        return change.getColumnNames();
    }

    public void setColumnNames(String columnNames) {
        change.setColumnNames(columnNames);
    }

    @DatabaseChangeProperty(description = "Name of the unique constraint")
    public String getConstraintName() {
        return change.getConstraintName();
    }

    public void setConstraintName(String constraintName) {
        change.setConstraintName(constraintName);
    }


    public String getTablespace() {
        return change.getTablespace();
    }

    public void setTablespace(String tablespace) {
        change.setTablespace(tablespace);
    }

    public Boolean getDeferrable() {
        return change.getDeferrable();
    }

    public void setDeferrable(Boolean deferrable) {
        change.setDeferrable(deferrable);
    }

    public Boolean getInitiallyDeferred() {
        return change.getInitiallyDeferred();
    }

    public void setInitiallyDeferred(Boolean initiallyDeferred) {
        change.setInitiallyDeferred(initiallyDeferred);
    }

    public Boolean getDisabled() {
        return change.getDisabled();
    }

    public void setDisabled(Boolean disabled) {
        change.setDisabled(disabled);
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {

//todo    	if (database instanceof SQLiteDatabase) {
//    		// return special statements for SQLite databases
//    		return generateStatementsForSQLiteDatabase(database);
//        }

        boolean deferrable = false;
        if (getDeferrable() != null) {
            deferrable = getDeferrable();
        }

        boolean initiallyDeferred = false;
        if (getInitiallyDeferred() != null) {
            initiallyDeferred = getInitiallyDeferred();
        }
        boolean disabled = false;
        if (getDisabled() != null) {
            disabled = getDisabled();
        }

    	AddUniqueConstraintStatement statement = new AddUniqueConstraintStatement(getCatalogName(), getSchemaName(), getTableName(), ColumnConfig.arrayFromNames(getColumnNames()), getConstraintName());
        statement.setTablespace(getTablespace())
                        .setDeferrable(deferrable)
                        .setInitiallyDeferred(initiallyDeferred)
                        .setDisabled(disabled);

        return new SqlStatement[] { statement };
    }


    @Override
    public ChangeStatus checkStatus(Database database) {
        ChangeStatus result = new ChangeStatus();
        try {
            String[] columnNames = getColumnNames().split("\\s+,\\s+");
            Column[] columns = new Column[columnNames.length];
            for (int i=0; i<columnNames.length; i++) {
                columns[i] = new Column(columnNames[i]);
            }
            UniqueConstraint example = new UniqueConstraint(getConstraintName(), getCatalogName(), getSchemaName(), getTableName(), columns);

            UniqueConstraint snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(example, database);
            result.assertComplete(snapshot != null, "Unique constraint does not exist");

            return result;

        } catch (Exception e) {
            return result.unknown(e);
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
//				String[] split_columns = getColumnNames().split("[ ]*,[ ]*");
//				for (String split_column:split_columns) {
//					if (column.getName().equals(split_column)) {
//    					column.getConstraints().setUnique(true);
//    				}
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
        return "Unique constraint added to "+getTableName()+"("+getColumnNames()+")";
    }

    @Override
    protected ExecutableChange[] createInverses() {
        DropUniqueConstraintAction inverse = new DropUniqueConstraintAction();
        inverse.setSchemaName(getSchemaName());
        inverse.setTableName(getTableName());
        inverse.setConstraintName(getConstraintName());

        return new ExecutableChange[]{
                inverse,
        };
    }
}
