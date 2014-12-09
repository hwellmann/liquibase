package liquibase.action;

import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeStatus;
import liquibase.change.ColumnConfig;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.change.ExecutableChange;
import liquibase.change.core.DropUniqueConstraintChange;
import liquibase.database.Database;
import liquibase.database.core.SybaseASADatabase;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropUniqueConstraintStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.UniqueConstraint;

import org.kohsuke.MetaInfServices;

/**
 * Removes an existing unique constraint.
 */
@DatabaseChange(name = "dropUniqueConstraint", description = "Drops an existing unique constraint", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "uniqueConstraint")
@MetaInfServices(ExecutableChange.class)
public class DropUniqueConstraintAction extends AbstractAction<DropUniqueConstraintChange> {

    public DropUniqueConstraintAction() {
        super(new DropUniqueConstraintChange());
    }

    public DropUniqueConstraintAction(DropUniqueConstraintChange change) {
        super(change);
    }

    public String getCatalogName() {
        return change.getCatalogName();
    }

    public void setCatalogName(String catalogName) {
        change.setCatalogName(catalogName);
    }

    @DatabaseChangeProperty(mustEqualExisting = "uniqueConstraint.table.schema")
    public String getSchemaName() {
        return change.getSchemaName();
    }

    public void setSchemaName(String schemaName) {
        change.setSchemaName(schemaName);
    }

    @DatabaseChangeProperty(mustEqualExisting = "uniqueConstraint.table", description = "Name of the table to drop the unique constraint from")
    public String getTableName() {
        return change.getTableName();
    }

    public void setTableName(String tableName) {
        change.setTableName(tableName);
    }

    @DatabaseChangeProperty(mustEqualExisting = "uniqueConstraint", description = "Name of unique constraint to drop")
    public String getConstraintName() {
        return change.getConstraintName();
    }

    public void setConstraintName(String constraintName) {
        change.setConstraintName(constraintName);
    }

    @DatabaseChangeProperty(exampleValue = "name")
    public String getUniqueColumns() {
        return change.getUniqueColumns();
    }

    public void setUniqueColumns(String uniqueColumns) {
        change.setUniqueColumns(uniqueColumns);
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {

        // todo if (database instanceof SQLiteDatabase) {
        // // return special statements for SQLite databases
        // return generateStatementsForSQLiteDatabase(database);
        // }
        DropUniqueConstraintStatement statement = new DropUniqueConstraintStatement(
            getCatalogName(), getSchemaName(), getTableName(), getConstraintName());
        if (database instanceof SybaseASADatabase) {
            statement.setUniqueColumns(ColumnConfig.arrayFromNames(getUniqueColumns()));
        }
        return new SqlStatement[] { statement };
    }

    @Override
    public ChangeStatus checkStatus(Database database) {
        try {
            UniqueConstraint example = new UniqueConstraint(getConstraintName(), getCatalogName(),
                getSchemaName(), getTableName());
            if (getUniqueColumns() != null) {
                for (String column : getUniqueColumns().split("\\s*,\\s*")) {
                    example.addColumn(example.getColumns().size(), new Column(column));
                }
            }
            return new ChangeStatus().assertComplete(
                !SnapshotGeneratorFactory.getInstance().has(example, database),
                "Unique constraint exists");
        }
        catch (Exception e) {
            return new ChangeStatus().unknown(e);
        }
    }

    // private SqlStatement[] generateStatementsForSQLiteDatabase(Database database) {
    //
    // // SQLite does not support this ALTER TABLE operation until now.
    // // For more information see: http://www.sqlite.org/omitted.html.
    // // This is a small work around...
    //
    // // Note: The attribute "constraintName" is used to pass the column
    // // name instead of the constraint name.
    //
    // List<SqlStatement> statements = new ArrayList<SqlStatement>();
    //
    // // define alter table logic
    // AlterTableVisitor rename_alter_visitor = new AlterTableVisitor() {
    // public ColumnConfig[] getColumnsToAdd() {
    // return new ColumnConfig[0];
    // }
    // public boolean copyThisColumn(ColumnConfig column) {
    // return true;
    // }
    // public boolean createThisColumn(ColumnConfig column) {
    // if (column.getName().equals(getConstraintName())) {
    // column.getConstraints().setUnique(false);
    // }
    // return true;
    // }
    // public boolean createThisIndex(Index index) {
    // return true;
    // }
    // };
    //
    // try {
    // // alter table
    // statements.addAll(SQLiteDatabase.getAlterTableStatements(
    // rename_alter_visitor,
    // database,getCatalogName(), getSchemaName(),getTableName()));
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    //
    // return statements.toArray(new SqlStatement[statements.size()]);
    // }

    @Override
    public String getConfirmationMessage() {
        return "Unique constraint " + getConstraintName() + " dropped from " + getTableName();
    }
}
