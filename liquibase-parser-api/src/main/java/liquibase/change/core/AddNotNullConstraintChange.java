package liquibase.change.core;

import liquibase.change.BaseChange;
import liquibase.change.Change;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;

import org.kohsuke.MetaInfServices;

/**
 * Adds a not-null constraint to an existing column.
 */
@DatabaseChange(name="addNotNullConstraint",
        description = "Adds a not-null constraint to an existing table. If a defaultNullValue attribute is passed, all null values for the column will be updated to the passed value before the constraint is applied.",
        priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "column")
@MetaInfServices(Change.class)
public class AddNotNullConstraintChange extends BaseChange {
    private String catalogName;
    private String schemaName;
    private String tableName;
    private String columnName;
    private String defaultNullValue;
    private String columnDataType;

    @DatabaseChangeProperty(mustEqualExisting ="column.relation.catalog", since = "3.0")
    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    @DatabaseChangeProperty(mustEqualExisting ="column.relation.schema")
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "column.relation", description = "Adds a not-null constraint to an existing table. If a defaultNullValue attribute is passed, all null values for the column will be updated to the passed value before the constraint is applied.")
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "column.relation.column", description = "Name of the column to add the constraint to")
    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    @DatabaseChangeProperty(description = "Value to set all currently null values to. If not set, change will fail if null values exist")
    public String getDefaultNullValue() {
        return defaultNullValue;
    }

    public void setDefaultNullValue(String defaultNullValue) {
        this.defaultNullValue = defaultNullValue;
    }

    @DatabaseChangeProperty(description = "Current data type of the column")
    public String getColumnDataType() {
        return columnDataType;
    }

    public void setColumnDataType(String columnDataType) {
        this.columnDataType = columnDataType;
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}