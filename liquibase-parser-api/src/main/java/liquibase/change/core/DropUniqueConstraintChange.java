package liquibase.change.core;

import liquibase.change.BaseChange;
import liquibase.change.Change;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;

import org.kohsuke.MetaInfServices;

/**
 * Removes an existing unique constraint.
 */
@DatabaseChange(name = "dropUniqueConstraint", description = "Drops an existing unique constraint", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "uniqueConstraint")
@MetaInfServices(Change.class)
public class DropUniqueConstraintChange extends BaseChange {

    private String catalogName;
    private String schemaName;
    private String tableName;
    private String constraintName;
    /**
     * Sybase ASA does drop unique constraint not by name, but using list of the columns in unique
     * clause.
     */
    private String uniqueColumns;

    @DatabaseChangeProperty(mustEqualExisting = "uniqueConstraint.table.catalog", since = "3.0")
    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "uniqueConstraint.table.schema")
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "uniqueConstraint.table", description = "Name of the table to drop the unique constraint from")
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "uniqueConstraint", description = "Name of unique constraint to drop")
    public String getConstraintName() {
        return constraintName;
    }

    public void setConstraintName(String constraintName) {
        this.constraintName = constraintName;
    }

    @DatabaseChangeProperty(exampleValue = "name")
    public String getUniqueColumns() {
        return uniqueColumns;
    }

    public void setUniqueColumns(String uniqueColumns) {
        this.uniqueColumns = uniqueColumns;
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}
