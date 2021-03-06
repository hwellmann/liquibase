package liquibase.change.core;

import liquibase.change.BaseChange;
import liquibase.change.Change;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;

import org.kohsuke.MetaInfServices;

/**
 * Drops an existing view.
 */
@DatabaseChange(name="dropView", description = "Drops an existing view", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "view")
@MetaInfServices(Change.class)
public class DropViewChange extends BaseChange {
    private String catalogName;
    private String schemaName;
    private String viewName;


    @DatabaseChangeProperty(mustEqualExisting ="view.catalog", since = "3.0")
    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    @DatabaseChangeProperty(mustEqualExisting ="view.schema")
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "view", description = "Name of the view to drop")
    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}
