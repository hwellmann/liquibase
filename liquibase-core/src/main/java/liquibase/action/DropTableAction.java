package liquibase.action;

import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeStatus;
import liquibase.change.DatabaseChange;
import liquibase.change.ExecutableChange;
import liquibase.change.core.DropTableChange;
import liquibase.database.Database;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropTableStatement;
import liquibase.structure.core.Table;

import org.kohsuke.MetaInfServices;

/**
 * Drops an existing table.
 */
@DatabaseChange(name="dropTable", description = "Drops an existing table", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "table")
@MetaInfServices(ExecutableChange.class)
public class DropTableAction extends AbstractAction<DropTableChange> {


    public DropTableAction() {
        this(new DropTableChange());
    }

    public DropTableAction(DropTableChange change) {
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

    public Boolean isCascadeConstraints() {
        return change.isCascadeConstraints();
    }

    public void setCascadeConstraints(Boolean cascadeConstraints) {
        change.setCascadeConstraints(cascadeConstraints);
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        boolean constraints = false;
        if (isCascadeConstraints() != null) {
            constraints = isCascadeConstraints();
        }

        return new SqlStatement[]{
                new DropTableStatement(getCatalogName(), getSchemaName(), getTableName(), constraints)
        };
    }

    @Override
    public ChangeStatus checkStatus(Database database) {
        try {
            return new ChangeStatus().assertComplete(!SnapshotGeneratorFactory.getInstance().has(new Table(getCatalogName(), getSchemaName(), getTableName()), database), "Table exists");
        } catch (Exception e) {
            return new ChangeStatus().unknown(e);
        }
    }


    @Override
    public String getConfirmationMessage() {
        return "Table " + getTableName() + " dropped";
    }
}
