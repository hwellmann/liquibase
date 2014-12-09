package liquibase.action;

import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeStatus;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.change.ExecutableChange;
import liquibase.change.core.RenameViewChange;
import liquibase.database.Database;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.RenameViewStatement;
import liquibase.structure.core.View;

import org.kohsuke.MetaInfServices;

/**
 * Renames an existing view.
 */
@DatabaseChange(name="renameView", description = "Renames an existing view", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "view")
@MetaInfServices(ExecutableChange.class)
public class RenameViewAction extends AbstractAction<RenameViewChange> {

    public RenameViewAction() {
        super(new RenameViewChange());
    }

    public RenameViewAction(RenameViewChange change) {
        super(change);
    }

    public String getCatalogName() {
        return change.getCatalogName();
    }

    public void setCatalogName(String catalogName) {
        change.setCatalogName(catalogName);
    }

    @DatabaseChangeProperty(mustEqualExisting ="view.schema")
    public String getSchemaName() {
        return change.getSchemaName();
    }

    public void setSchemaName(String schemaName) {
        change.setSchemaName(schemaName);
    }

    @DatabaseChangeProperty(mustEqualExisting = "view", description = "Name of the view to rename")
    public String getOldViewName() {
        return change.getOldViewName();
    }

    public void setOldViewName(String oldViewName) {
        change.setOldViewName(oldViewName);
    }

    @DatabaseChangeProperty(description = "Name to rename the view to")
    public String getNewViewName() {
        return change.getNewViewName();
    }

    public void setNewViewName(String newViewName) {
        change.setNewViewName(newViewName);
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[]{new RenameViewStatement(getCatalogName(), getSchemaName(), getOldViewName(), getNewViewName())};
    }

    @Override
    protected ExecutableChange[] createInverses() {
        RenameViewAction inverse = new RenameViewAction();
        inverse.setOldViewName(getNewViewName());
        inverse.setNewViewName(getOldViewName());

        return new ExecutableChange[]{
                inverse
        };
    }

    @Override
    public ChangeStatus checkStatus(Database database) {
        try {
            ChangeStatus changeStatus = new ChangeStatus();
            View newView = SnapshotGeneratorFactory.getInstance().createSnapshot(new View(getCatalogName(), getSchemaName(), getNewViewName()), database);
            View oldView = SnapshotGeneratorFactory.getInstance().createSnapshot(new View(getCatalogName(), getSchemaName(), getOldViewName()), database);

            if (newView == null && oldView == null) {
                return changeStatus.unknown("Neither view exists");
            }
            if (newView != null && oldView != null) {
                return changeStatus.unknown("Both views exist");
            }
            changeStatus.assertComplete(newView != null, "New view does not exist");

            return changeStatus;
        } catch (Exception e) {
            return new ChangeStatus().unknown(e);
        }

    }

    @Override
    public String getConfirmationMessage() {
        return "View " + getOldViewName() + " renamed to " + getNewViewName();
    }
}
