package liquibase.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeStatus;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.change.ExecutableChange;
import liquibase.change.core.AddLookupTableChange;
import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.database.core.HsqlDatabase;
import liquibase.database.core.InformixDatabase;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.SybaseASADatabase;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.RawSqlStatement;
import liquibase.statement.core.ReorganizeTableStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.ForeignKey;
import liquibase.structure.core.Table;

import org.kohsuke.MetaInfServices;

/**
 * Extracts data from an existing column to create a lookup table.
 * A foreign key is created between the old column and the new lookup table.
 */
@DatabaseChange(name="addLookupTable",
        description = "Creates a lookup table containing values stored in a column and creates a foreign key to the new table.",
        priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "column")
@MetaInfServices(ExecutableChange.class)
public class AddLookupTableAction extends AbstractAction<AddLookupTableChange> {

    public AddLookupTableAction() {
        super(new AddLookupTableChange());
    }

    public AddLookupTableAction(AddLookupTableChange change) {
        super(change);
    }

    public String getExistingTableCatalogName() {
        return change.getExistingTableCatalogName();
    }

    public void setExistingTableCatalogName(String existingTableCatalogName) {
        change.setExistingTableCatalogName(existingTableCatalogName);
    }

    @DatabaseChangeProperty(mustEqualExisting ="column.relation.schema")
    public String getExistingTableSchemaName() {
        return change.getExistingTableSchemaName();
    }

    public void setExistingTableSchemaName(String existingTableSchemaName) {
        change.setExistingTableSchemaName(existingTableSchemaName);
    }

    public String getExistingTableName() {
        return change.getExistingTableName();
    }

    public void setExistingTableName(String existingTableName) {
        change.setExistingTableName(existingTableName);
    }

    public String getExistingColumnName() {
        return change.getExistingColumnName();
    }

    public void setExistingColumnName(String existingColumnName) {
        change.setExistingColumnName(existingColumnName);
    }


    public String getNewTableCatalogName() {
        return change.getNewTableCatalogName();
    }

    public void setNewTableCatalogName(String newTableCatalogName) {
        change.setNewTableCatalogName(newTableCatalogName);
    }

    public String getNewTableSchemaName() {
        return change.getNewTableSchemaName();
    }

    public void setNewTableSchemaName(String newTableSchemaName) {
        change.setNewTableSchemaName(newTableSchemaName);
    }

    public String getNewTableName() {
        return change.getNewTableName();
    }

    public void setNewTableName(String newTableName) {
        change.setNewTableName(newTableName);
    }

    public String getNewColumnName() {
        return change.getNewColumnName();
    }

    public void setNewColumnName(String newColumnName) {
        change.setNewColumnName(newColumnName);
    }

    public String getNewColumnDataType() {
        return change.getNewColumnDataType();
    }

    public void setNewColumnDataType(String newColumnDataType) {
        change.setNewColumnDataType(newColumnDataType);
    }

    public String getConstraintName() {
        return change.getConstraintName();
    }

    public String getFinalConstraintName() {
        return change.getFinalConstraintName();
    }

    public void setConstraintName(String constraintName) {
        change.setConstraintName(constraintName);
    }

    @Override
    public boolean supports(Database database) {
        if (database instanceof HsqlDatabase) {
            return false;
        }
        return super.supports(database);
    }

    @Override
    protected ExecutableChange[] createInverses() {
        DropForeignKeyConstraintAction dropFK = new DropForeignKeyConstraintAction();
        dropFK.setBaseTableSchemaName(getExistingTableSchemaName());
        dropFK.setBaseTableName(getExistingTableName());
        dropFK.setConstraintName(getFinalConstraintName());

        DropTableAction dropTable = new DropTableAction();
        dropTable.setSchemaName(getNewTableSchemaName());
        dropTable.setTableName(getNewTableName());

        return new ExecutableChange[]{
                dropFK,
                dropTable,
        };
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        List<SqlStatement> statements = new ArrayList<SqlStatement>();

        String newTableCatalogName = getNewTableCatalogName();
        String newTableSchemaName = getNewTableSchemaName();

        String existingTableCatalogName = getExistingTableCatalogName();
        String existingTableSchemaName = getExistingTableSchemaName();

        SqlStatement[] createTablesSQL = {new RawSqlStatement("CREATE TABLE " + database.escapeTableName(newTableCatalogName, newTableSchemaName, getNewTableName()) + " AS SELECT DISTINCT " + database.escapeObjectName(getExistingColumnName(), Column.class) + " AS " + database.escapeObjectName(getNewColumnName(), Column.class) + " FROM " + database.escapeTableName(existingTableCatalogName, existingTableSchemaName, getExistingTableName()) + " WHERE " + database.escapeObjectName(getExistingColumnName(), Column.class) + " IS NOT NULL")};
        if (database instanceof MSSQLDatabase) {
            createTablesSQL = new SqlStatement[]{new RawSqlStatement("SELECT DISTINCT " + database.escapeObjectName(getExistingColumnName(), Column.class) + " AS " + database.escapeObjectName(getNewColumnName(), Column.class) + " INTO " + database.escapeTableName(newTableCatalogName, newTableSchemaName, getNewTableName()) + " FROM " + database.escapeTableName(existingTableCatalogName, existingTableSchemaName, getExistingTableName()) + " WHERE " + database.escapeObjectName(getExistingColumnName(), Column.class) + " IS NOT NULL"),};
        } else if (database instanceof SybaseASADatabase) {
            createTablesSQL = new SqlStatement[]{new RawSqlStatement("SELECT DISTINCT " + database.escapeObjectName(getExistingColumnName(), Column.class) + " AS " + database.escapeObjectName(getNewColumnName(), Column.class) + " INTO " + database.escapeTableName(newTableCatalogName, newTableSchemaName, getNewTableName()) + " FROM " + database.escapeTableName(existingTableCatalogName, existingTableSchemaName, getExistingTableName()) + " WHERE " + database.escapeObjectName(getExistingColumnName(), Column.class) + " IS NOT NULL"),};
        } else if (database instanceof DB2Database) {
            createTablesSQL = new SqlStatement[]{
                    new RawSqlStatement("CREATE TABLE " + database.escapeTableName(newTableCatalogName, newTableSchemaName, getNewTableName()) + " AS (SELECT " + database.escapeObjectName(getExistingColumnName(), Column.class) + " AS " + database.escapeObjectName(getNewColumnName(), Column.class) + " FROM " + database.escapeTableName(existingTableCatalogName, existingTableSchemaName, getExistingTableName()) + ") WITH NO DATA"),
                    new RawSqlStatement("INSERT INTO " + database.escapeTableName(newTableCatalogName, newTableSchemaName, getNewTableName()) + " SELECT DISTINCT " + database.escapeObjectName(getExistingColumnName(), Column.class) + " FROM " + database.escapeTableName(existingTableCatalogName, existingTableSchemaName, getExistingTableName()) + " WHERE " + database.escapeObjectName(getExistingColumnName(), Column.class) + " IS NOT NULL"),
            };
        } else if (database instanceof InformixDatabase) {
            createTablesSQL = new SqlStatement[] {
                    new RawSqlStatement("CREATE TABLE " + database.escapeTableName(newTableCatalogName, newTableSchemaName, getNewTableName()) + " ( "  + database.escapeObjectName(getNewColumnName(), Column.class) + " " + getNewColumnDataType() + " )"),
                    new RawSqlStatement("INSERT INTO " + database.escapeTableName(newTableCatalogName, newTableSchemaName, getNewTableName()) + " ( "  + database.escapeObjectName(getNewColumnName(), Column.class) + " ) SELECT DISTINCT "  + database.escapeObjectName(getExistingColumnName(), Column.class) + " FROM " + database.escapeTableName(existingTableCatalogName, existingTableSchemaName, getExistingTableName()) + " WHERE " + database.escapeObjectName(getExistingColumnName(), Column.class) + " IS NOT NULL"),
            };
        }

        statements.addAll(Arrays.asList(createTablesSQL));

        if (!(database instanceof OracleDatabase)) {
            AddNotNullConstraintAction addNotNullAction = new AddNotNullConstraintAction();
            addNotNullAction.setSchemaName(newTableSchemaName);
            addNotNullAction.setTableName(getNewTableName());
            addNotNullAction.setColumnName(getNewColumnName());
            addNotNullAction.setColumnDataType(getNewColumnDataType());
            statements.addAll(Arrays.asList(addNotNullAction.generateStatements(database)));
        }

        if (database instanceof DB2Database) {
            statements.add(new ReorganizeTableStatement(newTableCatalogName, newTableSchemaName, getNewTableName()));
        }

        AddPrimaryKeyAction addPKAction = new AddPrimaryKeyAction();
        addPKAction.setSchemaName(newTableSchemaName);
        addPKAction.setTableName(getNewTableName());
        addPKAction.setColumnNames(getNewColumnName());
        statements.addAll(Arrays.asList(addPKAction.generateStatements(database)));

        if (database instanceof DB2Database) {
            statements.add(new ReorganizeTableStatement(newTableCatalogName,newTableSchemaName, getNewTableName()));
        }

        AddForeignKeyConstraintAction addFKAction = new AddForeignKeyConstraintAction();
        addFKAction.setBaseTableSchemaName(existingTableSchemaName);
        addFKAction.setBaseTableName(getExistingTableName());
        addFKAction.setBaseColumnNames(getExistingColumnName());
        addFKAction.setReferencedTableSchemaName(newTableSchemaName);
        addFKAction.setReferencedTableName(getNewTableName());
        addFKAction.setReferencedColumnNames(getNewColumnName());

        addFKAction.setConstraintName(getFinalConstraintName());
        statements.addAll(Arrays.asList(addFKAction.generateStatements(database)));

        return statements.toArray(new SqlStatement[statements.size()]);
    }

    @Override
    public ChangeStatus checkStatus(Database database) {
        ChangeStatus result = new ChangeStatus();
        try {
            Table newTableExample = new Table(getNewTableCatalogName(), getNewTableSchemaName(), getNewTableName());
            Column newColumnExample = new Column(Table.class, getNewTableCatalogName(), getNewTableSchemaName(), getNewTableName(), getNewColumnName());

            ForeignKey foreignKeyExample = new ForeignKey(getConstraintName(), getExistingTableCatalogName(), getExistingTableSchemaName(), getExistingTableName());
            foreignKeyExample.setPrimaryKeyTable(newTableExample);
            foreignKeyExample.setForeignKeyColumns(Column.listFromNames(getExistingColumnName()));
            foreignKeyExample.setPrimaryKeyColumns(Column.listFromNames(getNewColumnName()));

            result.assertComplete(SnapshotGeneratorFactory.getInstance().has(newTableExample, database), "New table does not exist");
            result.assertComplete(SnapshotGeneratorFactory.getInstance().has(newColumnExample, database), "New column does not exist");
            result.assertComplete(SnapshotGeneratorFactory.getInstance().has(foreignKeyExample, database), "Foreign key does not exist");

            return result;

        } catch (Exception e) {
            return result.unknown(e);
        }
    }

    @Override
    public String getConfirmationMessage() {
        return "Lookup table added for "+getExistingTableName()+"."+getExistingColumnName();
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}
