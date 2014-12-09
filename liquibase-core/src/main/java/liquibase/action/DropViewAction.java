package liquibase.action;

import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeStatus;
import liquibase.change.DatabaseChange;
import liquibase.change.ExecutableChange;
import liquibase.change.core.DropViewChange;
import liquibase.database.Database;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropViewStatement;
import liquibase.structure.core.View;

import org.kohsuke.MetaInfServices;

/**
 * Drops an existing view.
 */
@DatabaseChange(name="dropView", description = "Drops an existing view", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "view")
@MetaInfServices(ExecutableChange.class)
public class DropViewAction extends AbstractAction<DropViewChange> {

    public DropViewAction() {
        super(new DropViewChange());
    }

    public DropViewAction(DropViewChange change) {
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

    public String getViewName() {
        return change.getViewName();
    }

    public void setViewName(String viewName) {
        change.setViewName(viewName);
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[]{
                new DropViewStatement(getCatalogName(), getSchemaName(), getViewName()),
        };
    }

    @Override
    public ChangeStatus checkStatus(Database database) {
        try {
            return new ChangeStatus().assertComplete(!SnapshotGeneratorFactory.getInstance().has(new View(getCatalogName(), getSchemaName(), getViewName()), database), "View exists");
        } catch (Exception e) {
            return new ChangeStatus().unknown(e);
        }
    }


    @Override
    public String getConfirmationMessage() {
        return "View "+getViewName()+" dropped";
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}
