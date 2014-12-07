package liquibase.action;

import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeStatus;
import liquibase.change.DatabaseChange;
import liquibase.change.ExecutableChange;
import liquibase.change.core.AddPrimaryKeyChange;
import liquibase.change.core.DropPrimaryKeyChange;
import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AddPrimaryKeyStatement;
import liquibase.statement.core.ReorganizeTableStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.PrimaryKey;

import org.kohsuke.MetaInfServices;

/**
 * Creates a primary key out of an existing column or set of columns.
 */
@DatabaseChange(name="addPrimaryKey", description = "Adds creates a primary key out of an existing column or set of columns.", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "column")
@MetaInfServices(ExecutableChange.class)
public class AddPrimaryKeyAction extends AbstractAction<AddPrimaryKeyChange> {

    public AddPrimaryKeyAction() {
        this(new AddPrimaryKeyChange());
    }

    public AddPrimaryKeyAction(AddPrimaryKeyChange change) {
        super(change);
    }

    public String getTableName() {
        return change.getTableName();
    }

    public void setTableName(String tableName) {
        change.setTableName(tableName);
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

    public String getColumnNames() {
        return change.getColumnNames();
    }

    public void setColumnNames(String columnNames) {
        change.setColumnNames(columnNames);
    }

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

    public Boolean getClustered() {
        return change.getClustered();
    }

    public void setClustered(Boolean clustered) {
        change.setClustered(clustered);
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {


        AddPrimaryKeyStatement statement = new AddPrimaryKeyStatement(getCatalogName(), getSchemaName(), getTableName(), getColumnNames(), getConstraintName());
        statement.setTablespace(getTablespace());
        statement.setClustered(getClustered());

        if (database instanceof DB2Database) {
            return new SqlStatement[]{
                    statement,
                    new ReorganizeTableStatement(getCatalogName(), getSchemaName(), getTableName())
            };
//todo        } else if (database instanceof SQLiteDatabase) {
//            // return special statements for SQLite databases
//            return generateStatementsForSQLiteDatabase(database);
        }

        return new SqlStatement[]{
                statement
        };
    }

    @Override
    public ChangeStatus checkStatus(Database database) {
        ChangeStatus result = new ChangeStatus();
        try {
            PrimaryKey example = new PrimaryKey(getConstraintName(), getCatalogName(), getSchemaName(), getTableName(), Column.arrayFromNames(getColumnNames()));

            PrimaryKey snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(example, database);
            result.assertComplete(snapshot != null, "Primary key does not exist");

            return result;

        } catch (Exception e) {
            return result.unknown(e);
        }
    }

    //    private SqlStatement[] generateStatementsForSQLiteDatabase(Database database) {
//        // SQLite does not support this ALTER TABLE operation until now.
//        // or more information: http://www.sqlite.org/omitted.html
//        // This is a small work around...
//
//        List<SqlStatement> statements = new ArrayList<SqlStatement>();
//
//        // define alter table logic
//        AlterTableVisitor rename_alter_visitor = new AlterTableVisitor() {
//            public ColumnConfig[] getColumnsToAdd() {
//                return new ColumnConfig[0];
//            }
//
//            public boolean copyThisColumn(ColumnConfig column) {
//                return true;
//            }
//
//            public boolean createThisColumn(ColumnConfig column) {
//                String[] split_columns = getColumnNames().split("[ ]*,[ ]*");
//                for (String split_column : split_columns) {
//                    if (column.getName().equals(split_column)) {
//                        column.getConstraints().setPrimaryKey(true);
//                    }
//                }
//                return true;
//            }
//
//            public boolean createThisIndex(Index index) {
//                return true;
//            }
//        };
//
//        try {
//            // alter table
//            statements.addAll(SQLiteDatabase.getAlterTableStatements(
//                    rename_alter_visitor,
//                    database, getCatalogName(),  getSchemaName(), getTableName()));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return statements.toArray(new SqlStatement[statements.size()]);
//    }

    @Override
    protected ExecutableChange[] createInverses() {
        DropPrimaryKeyChange inverse = new DropPrimaryKeyChange();
        inverse.setSchemaName(getSchemaName());
        inverse.setTableName(getTableName());
        inverse.setConstraintName(getConstraintName());

        return new ExecutableChange[]{
                inverse,
        };
    }

    @Override
    public String getConfirmationMessage() {
        return "Primary key added to " + getTableName() + " (" + getColumnNames() + ")";
    }
}
