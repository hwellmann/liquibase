package liquibase.action;

import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeStatus;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.change.ExecutableChange;
import liquibase.change.core.DropForeignKeyConstraintChange;
import liquibase.database.Database;
import liquibase.database.core.SQLiteDatabase;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropForeignKeyConstraintStatement;
import liquibase.structure.core.ForeignKey;

import org.kohsuke.MetaInfServices;

/**
 * Drops an existing foreign key constraint.
 */
@DatabaseChange(name="dropForeignKeyConstraint", description = "Drops an existing foreign key", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "foreignKey")
@MetaInfServices(ExecutableChange.class)
public class DropForeignKeyConstraintAction extends AbstractAction<DropForeignKeyConstraintChange> {

    public DropForeignKeyConstraintAction() {
        super(new DropForeignKeyConstraintChange());
    }

    public DropForeignKeyConstraintAction(DropForeignKeyConstraintChange change) {
        super(change);
    }

    public String getBaseTableCatalogName() {
        return change.getBaseTableCatalogName();
    }

    public void setBaseTableCatalogName(String baseTableCatalogName) {
        change.setBaseTableCatalogName(baseTableCatalogName);
    }

    public String getBaseTableSchemaName() {
        return change.getBaseTableSchemaName();
    }

    public void setBaseTableSchemaName(String baseTableSchemaName) {
        change.setBaseTableSchemaName(baseTableSchemaName);
    }

    @DatabaseChangeProperty(mustEqualExisting = "foreignKey.table", description = "Name of the table containing the column constrained by the foreign key")
    public String getBaseTableName() {
        return change.getBaseTableName();
    }

    public void setBaseTableName(String baseTableName) {
        change.setBaseTableName(baseTableName);
    }

    @DatabaseChangeProperty(mustEqualExisting = "foreignKey", description = "Name of the foreign key constraint to drop", exampleValue = "fk_address_person")
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
    		return generateStatementsForSQLiteDatabase();
    	}

        return new SqlStatement[]{
                new DropForeignKeyConstraintStatement(
                        getBaseTableCatalogName(),
                        getBaseTableSchemaName(),
                        getBaseTableName(),
                        getConstraintName()),
        };
    }

    @Override
    public ChangeStatus checkStatus(Database database) {
        try {
            return new ChangeStatus().assertComplete(!SnapshotGeneratorFactory.getInstance().has(new ForeignKey(getConstraintName(), getBaseTableCatalogName(), getBaseTableSchemaName(), getBaseTableCatalogName()), database), "Foreign key exists");
        } catch (Exception e) {
            return new ChangeStatus().unknown(e);
        }
    }

    private SqlStatement[] generateStatementsForSQLiteDatabase() {
    	// SQLite does not support foreign keys until now.
		// See for more information: http://www.sqlite.org/omitted.html
		// Therefore this is an empty operation...
		return new SqlStatement[]{};
    }

    @Override
    public String getConfirmationMessage() {
        return "Foreign key " + getConstraintName() + " dropped";
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}
