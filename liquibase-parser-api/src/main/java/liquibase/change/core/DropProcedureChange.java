package liquibase.change.core;

import liquibase.change.BaseChange;
import liquibase.change.Change;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.serializer.LiquibaseSerializable;

import org.kohsuke.MetaInfServices;

@DatabaseChange(name="dropProcedure", description = "Drops an existing procedure", priority = ChangeMetaData.PRIORITY_DEFAULT+100, appliesTo = "storedProcedure")
@MetaInfServices(Change.class)
public class DropProcedureChange extends BaseChange {

    private String catalogName;
    private String schemaName;
    private String procedureName;

    @DatabaseChangeProperty(mustEqualExisting ="storedProcedure.catalog")
    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    @DatabaseChangeProperty(mustEqualExisting ="storedProcedure.schema")
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "storedProcedure", description = "Name of the stored procedure to drop", exampleValue = "new_customer")
    public String getProcedureName() {
        return procedureName;
    }

    public void setProcedureName(String procedureName) {
        this.procedureName = procedureName;
    }

    @Override
    public String getSerializedObjectNamespace() {
        return LiquibaseSerializable.STANDARD_CHANGELOG_NAMESPACE;
    }
}
