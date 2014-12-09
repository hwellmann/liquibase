package liquibase.action;

import java.util.ArrayList;
import java.util.List;

import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeStatus;
import liquibase.change.DatabaseChange;
import liquibase.change.ExecutableChange;
import liquibase.change.core.RenameTableChange;
import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.RenameTableStatement;
import liquibase.statement.core.ReorganizeTableStatement;
import liquibase.structure.core.Table;

import org.kohsuke.MetaInfServices;

/**
 * Renames an existing table.
 */
@DatabaseChange(name="renameTable", description = "Renames an existing table", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "table")
@MetaInfServices(ExecutableChange.class)
public class RenameTableAction extends AbstractAction<RenameTableChange> {

    public RenameTableAction() {
        super(new RenameTableChange());
    }

    public RenameTableAction(RenameTableChange change) {
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

    public String getOldTableName() {
        return change.getOldTableName();
    }

    public void setOldTableName(String oldTableName) {
        change.setOldTableName(oldTableName);
    }

    public String getNewTableName() {
        return change.getNewTableName();
    }

    public void setNewTableName(String newTableName) {
        change.setNewTableName(newTableName);
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        List<SqlStatement> statements = new ArrayList<SqlStatement>();
        statements.add(new RenameTableStatement(getCatalogName(), getSchemaName(), getOldTableName(), getNewTableName()));
        if (database instanceof DB2Database) {
            statements.add(new ReorganizeTableStatement(getCatalogName(), getSchemaName(), getNewTableName()));
        }

        return statements.toArray(new SqlStatement[statements.size()]);
    }

    @Override
    public ChangeStatus checkStatus(Database database) {
        try {
            ChangeStatus changeStatus = new ChangeStatus();
            Table newTable = SnapshotGeneratorFactory.getInstance().createSnapshot(new Table(getCatalogName(), getSchemaName(), getNewTableName()), database);
            Table oldTable = SnapshotGeneratorFactory.getInstance().createSnapshot(new Table(getCatalogName(), getSchemaName(), getOldTableName()), database);

            if (newTable == null && oldTable == null) {
                return changeStatus.unknown("Neither table exists");
            }
            if (newTable != null && oldTable != null) {
                return changeStatus.unknown("Both tables exist");
            }
            changeStatus.assertComplete(newTable != null, "New table does not exist");

            return changeStatus;
        } catch (Exception e) {
            return new ChangeStatus().unknown(e);
        }

    }

    @Override
    protected ExecutableChange[] createInverses() {
        RenameTableAction inverse = new RenameTableAction();
        inverse.setSchemaName(getSchemaName());
        inverse.setOldTableName(getNewTableName());
        inverse.setNewTableName(getOldTableName());

        return new ExecutableChange[]{
                inverse
        };
    }

    @Override
    public String getConfirmationMessage() {
        return "Table " + getOldTableName() + " renamed to " + getNewTableName();
    }
}
