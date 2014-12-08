package liquibase.change.core;

import liquibase.change.BaseChange;
import liquibase.change.Change;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;

import org.kohsuke.MetaInfServices;

/**
 * Extracts data from an existing column to create a lookup table.
 * A foreign key is created between the old column and the new lookup table.
 */
@DatabaseChange(name="addLookupTable",
        description = "Creates a lookup table containing values stored in a column and creates a foreign key to the new table.",
        priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "column")
@MetaInfServices(Change.class)
public class AddLookupTableChange extends BaseChange {

    private String existingTableCatalogName;
    private String existingTableSchemaName;
    private String existingTableName;
    private String existingColumnName;

    private String newTableCatalogName;
    private String newTableSchemaName;
    private String newTableName;
    private String newColumnName;
    private String newColumnDataType;
    private String constraintName;

    public String getExistingTableCatalogName() {
        return existingTableCatalogName;
    }

    public void setExistingTableCatalogName(String existingTableCatalogName) {
        this.existingTableCatalogName = existingTableCatalogName;
    }

    @DatabaseChangeProperty(mustEqualExisting ="column.relation.schema")
    public String getExistingTableSchemaName() {
        return existingTableSchemaName;
    }

    public void setExistingTableSchemaName(String existingTableSchemaName) {
        this.existingTableSchemaName = existingTableSchemaName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "column.relation", description = "Name of the table containing the data to extract", exampleValue = "address")
    public String getExistingTableName() {
        return existingTableName;
    }

    public void setExistingTableName(String existingTableName) {
        this.existingTableName = existingTableName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "column", description = "Name of the column containing the data to extract", exampleValue = "state")
    public String getExistingColumnName() {
        return existingColumnName;
    }

    public void setExistingColumnName(String existingColumnName) {
        this.existingColumnName = existingColumnName;
    }


    @DatabaseChangeProperty(since = "3.0")
    public String getNewTableCatalogName() {
        return newTableCatalogName;
    }

    public void setNewTableCatalogName(String newTableCatalogName) {
        this.newTableCatalogName = newTableCatalogName;
    }

    public String getNewTableSchemaName() {
        return newTableSchemaName;
    }

    public void setNewTableSchemaName(String newTableSchemaName) {
        this.newTableSchemaName = newTableSchemaName;
    }

    @DatabaseChangeProperty(description = "Name of lookup table to create", exampleValue = "state")
    public String getNewTableName() {
        return newTableName;
    }

    public void setNewTableName(String newTableName) {
        this.newTableName = newTableName;
    }

    @DatabaseChangeProperty(description = "Name of the column in the new table to create", exampleValue = "abbreviation")
    public String getNewColumnName() {
        return newColumnName;
    }

    public void setNewColumnName(String newColumnName) {
        this.newColumnName = newColumnName;
    }

    @DatabaseChangeProperty(description = "Data type of the new table column", exampleValue = "char(2)")
    public String getNewColumnDataType() {
        return newColumnDataType;
    }

    public void setNewColumnDataType(String newColumnDataType) {
        this.newColumnDataType = newColumnDataType;
    }

    @DatabaseChangeProperty(description = "Name of the foreign-key constraint to create between the existing table and the lookup table", exampleValue = "fk_address_state")
    public String getConstraintName() {
        return constraintName;
    }

    public String getFinalConstraintName() {
        if (constraintName == null) {
            return ("FK_" + getExistingTableName() + "_" + getNewTableName()).toUpperCase();
        } else {
            return constraintName;
        }
    }

    public void setConstraintName(String constraintName) {
        this.constraintName = constraintName;
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}
