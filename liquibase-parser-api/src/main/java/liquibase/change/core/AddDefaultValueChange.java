package liquibase.change.core;

import liquibase.change.BaseChange;
import liquibase.change.Change;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.SequenceNextValueFunction;

import org.kohsuke.MetaInfServices;

/**
 * Sets a new default value to an existing column.
 */
@DatabaseChange(name = "addDefaultValue",
        description = "Adds a default value to the database definition for the specified column.\n" +
                "One of defaultValue, defaultValueNumeric, defaultValueBoolean or defaultValueDate must be set",
        priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "column")
@MetaInfServices(Change.class)
public class AddDefaultValueChange extends BaseChange {

    private String catalogName;
    private String schemaName;
    private String tableName;
    private String columnName;
    private String columnDataType;
    private String defaultValue;
    private String defaultValueNumeric;
    private String defaultValueDate;
    private Boolean defaultValueBoolean;
    private DatabaseFunction defaultValueComputed;
    private SequenceNextValueFunction defaultValueSequenceNext;

    @DatabaseChangeProperty(mustEqualExisting = "column.relation.catalog", since = "3.0")
    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "column.relation.schema")
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "column.relation", description = "Name of the table to containing the column", exampleValue = "file")
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "column", description = "Name of the column to add a default value to", exampleValue = "fileName")
    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    @DatabaseChangeProperty(description = "Current data type of the column to add default value to", exampleValue = "varchar(50)")
    public String getColumnDataType() {
        return columnDataType;
    }

    public void setColumnDataType(String columnDataType) {
        this.columnDataType = columnDataType;
    }

    @DatabaseChangeProperty(description = "Default value. Either this property or one of the other defaultValue* properties are required.", exampleValue = "Something Else", requiredForDatabase = "none")
    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }


    @DatabaseChangeProperty(requiredForDatabase = "none", exampleValue = "439.2")
    public String getDefaultValueNumeric() {
        return defaultValueNumeric;
    }

    public void setDefaultValueNumeric(String defaultValueNumeric) {
        this.defaultValueNumeric = defaultValueNumeric;
    }

    @DatabaseChangeProperty(requiredForDatabase = "none", exampleValue = "2008-02-12T12:34:03")
    public String getDefaultValueDate() {
        return defaultValueDate;
    }

    public void setDefaultValueDate(String defaultValueDate) {
        this.defaultValueDate = defaultValueDate;
    }


    @DatabaseChangeProperty(requiredForDatabase = "none")
    public Boolean getDefaultValueBoolean() {
        return defaultValueBoolean;
    }

    public void setDefaultValueBoolean(Boolean defaultValueBoolean) {
        this.defaultValueBoolean = defaultValueBoolean;
    }

    @DatabaseChangeProperty(requiredForDatabase = "none")
    public DatabaseFunction getDefaultValueComputed() {
        return defaultValueComputed;
    }

    public void setDefaultValueComputed(DatabaseFunction defaultValueComputed) {
        this.defaultValueComputed = defaultValueComputed;
    }

    @DatabaseChangeProperty(requiredForDatabase = "none")
    public SequenceNextValueFunction getDefaultValueSequenceNext() {
        return defaultValueSequenceNext;
    }

    public void setDefaultValueSequenceNext(SequenceNextValueFunction defaultValueSequenceNext) {
        this.defaultValueSequenceNext = defaultValueSequenceNext;
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}
