package liquibase.action;

import java.util.ArrayList;
import java.util.List;

import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeStatus;
import liquibase.change.ColumnConfig;
import liquibase.change.DatabaseChange;
import liquibase.change.ExecutableChange;
import liquibase.change.core.DropPrimaryKeyChange;
import liquibase.database.Database;
import liquibase.database.core.SQLiteDatabase;
import liquibase.database.core.SQLiteDatabase.AlterTableVisitor;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropPrimaryKeyStatement;
import liquibase.structure.core.Index;
import liquibase.structure.core.PrimaryKey;

import org.kohsuke.MetaInfServices;

/**
 * Removes an existing primary key.
 */
@DatabaseChange(name = "dropPrimaryKey", description = "Drops an existing primary key", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "primaryKey")
@MetaInfServices(ExecutableChange.class)
public class DropPrimaryKeyAction extends AbstractAction<DropPrimaryKeyChange> {

    public DropPrimaryKeyAction() {
        this(new DropPrimaryKeyChange());
    }

    public DropPrimaryKeyAction(DropPrimaryKeyChange change) {
        super(change);
    }

    @Override
    public boolean generateStatementsVolatile(Database database) {
        if (database instanceof SQLiteDatabase) {
            return true;
        }
        return false;
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

    public String getConstraintName() {
        return change.getConstraintName();
    }

    public void setConstraintName(String constraintName) {
        change.setConstraintName(constraintName);
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {

        if (database instanceof SQLiteDatabase) {
            // return special statements for SQLite databases
            return generateStatementsForSQLiteDatabase(database);
        }

        return new SqlStatement[] { new DropPrimaryKeyStatement(getCatalogName(), getSchemaName(),
            getTableName(), getConstraintName()), };
    }

    @Override
    public ChangeStatus checkStatus(Database database) {
        try {
            return new ChangeStatus().assertComplete(
                !SnapshotGeneratorFactory.getInstance().has(
                    new PrimaryKey(getConstraintName(), getCatalogName(), getSchemaName(),
                        getTableName()), database), "Primary key exists");
        }
        catch (Exception e) {
            return new ChangeStatus().unknown(e);
        }

    }

    private SqlStatement[] generateStatementsForSQLiteDatabase(Database database) {

        // SQLite does not support this ALTER TABLE operation until now.
        // For more information see: http://www.sqlite.org/omitted.html.
        // This is a small work around...

        // Note: The attribute "constraintName" is used to pass the column
        // name instead of the constraint name.

        List<SqlStatement> statements = new ArrayList<SqlStatement>();

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
                if (column.getName().equals(getConstraintName())) {
                    column.getConstraints().setPrimaryKey(false);
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
            statements.addAll(SQLiteDatabase.getAlterTableStatements(rename_alter_visitor,
                database, getCatalogName(), getSchemaName(), getTableName()));
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return statements.toArray(new SqlStatement[statements.size()]);
    }

    @Override
    public String getConfirmationMessage() {
        return "Primary key dropped from " + getTableName();
    }
}
