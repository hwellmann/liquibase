package liquibase.action;

import java.util.ArrayList;
import java.util.List;

import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeStatus;
import liquibase.change.DatabaseChange;
import liquibase.change.ExecutableChange;
import liquibase.change.core.CreateViewChange;
import liquibase.database.Database;
import liquibase.database.core.SQLiteDatabase;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.CreateViewStatement;
import liquibase.statement.core.DropViewStatement;
import liquibase.structure.core.View;

import org.kohsuke.MetaInfServices;

/**
 * Creates a new view.
 */
@DatabaseChange(name = "createView", description = "Create a new database view", priority = ChangeMetaData.PRIORITY_DEFAULT)
@MetaInfServices(ExecutableChange.class)
public class CreateViewAction extends AbstractAction<CreateViewChange> {

    public CreateViewAction() {
        super(new CreateViewChange());
    }

    public CreateViewAction(CreateViewChange change) {
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

    public String getSelectQuery() {
        return change.getSelectQuery();
    }

    public void setSelectQuery(String selectQuery) {
        change.setSelectQuery(selectQuery);
    }

    public Boolean getReplaceIfExists() {
        return change.getReplaceIfExists();
    }

    public void setReplaceIfExists(Boolean replaceIfExists) {
        change.setReplaceIfExists(replaceIfExists);
    }

    public Boolean getFullDefinition() {
        return change.getFullDefinition();
    }

    public void setFullDefinition(Boolean fullDefinition) {
        change.setFullDefinition(fullDefinition);
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        List<SqlStatement> statements = new ArrayList<SqlStatement>();

        boolean replaceIfExists = false;
        if (getReplaceIfExists() != null && getReplaceIfExists()) {
            replaceIfExists = true;
        }

        boolean fullDefinition = false;
        if (getFullDefinition() != null) {
            fullDefinition = getFullDefinition();
        }

        if (!supportsReplaceIfExistsOption(database) && replaceIfExists) {
            statements.add(new DropViewStatement(getCatalogName(), getSchemaName(), getViewName()));
            statements.add(new CreateViewStatement(getCatalogName(), getSchemaName(),
                getViewName(), getSelectQuery(), false).setFullDefinition(fullDefinition));
        }
        else {
            statements
                .add(new CreateViewStatement(getCatalogName(), getSchemaName(), getViewName(),
                    getSelectQuery(), replaceIfExists).setFullDefinition(fullDefinition));
        }

        return statements.toArray(new SqlStatement[statements.size()]);
    }

    @Override
    public String getConfirmationMessage() {
        return "View " + getViewName() + " created";
    }

    @Override
    protected ExecutableChange[] createInverses() {
        DropViewAction inverse = new DropViewAction();
        inverse.setViewName(getViewName());
        inverse.setSchemaName(getSchemaName());

        return new ExecutableChange[] { inverse };
    }

    @Override
    public ChangeStatus checkStatus(Database database) {
        ChangeStatus result = new ChangeStatus();
        try {
            View example = new View(getCatalogName(), getSchemaName(), getViewName());

            View snapshot = SnapshotGeneratorFactory.getInstance()
                .createSnapshot(example, database);
            result.assertComplete(snapshot != null, "View does not exist");

            return result;

        }
        catch (Exception e) {
            return result.unknown(e);
        }
    }

    private boolean supportsReplaceIfExistsOption(Database database) {
        return !(database instanceof SQLiteDatabase);
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}
