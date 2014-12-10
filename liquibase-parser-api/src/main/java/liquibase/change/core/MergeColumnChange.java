package liquibase.change.core;

import liquibase.change.BaseChange;
import liquibase.change.Change;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;

import org.kohsuke.MetaInfServices;

/**
 * Combines data from two existing columns into a new column and drops the original columns.
 */
@DatabaseChange(name="mergeColumns", description = "Concatenates the values in two columns, joins them by with string, and stores the resulting value in a new column.", priority = ChangeMetaData.PRIORITY_DEFAULT)
@MetaInfServices(Change.class)
public class MergeColumnChange extends BaseChange {

    private String catalogName;
    private String schemaName;
    private String tableName;
    private String column1Name;
    private String joinString;
    private String column2Name;
    private String finalColumnName;
    private String finalColumnType;

    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @DatabaseChangeProperty(description = "Name of the table containing the columns to join")
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @DatabaseChangeProperty(description = "Name of the column containing the first half of the data", exampleValue = "first_name")
    public String getColumn1Name() {
        return column1Name;
    }

    public void setColumn1Name(String column1Name) {
        this.column1Name = column1Name;
    }

    @DatabaseChangeProperty(description = "String to place include between the values from column1 and column2 (may be empty)", exampleValue = " ")
    public String getJoinString() {
        return joinString;
    }

    public void setJoinString(String joinString) {
        this.joinString = joinString;
    }

    @DatabaseChangeProperty(description = "Name of the column containing the second half of the data", exampleValue = "last_name")
    public String getColumn2Name() {
        return column2Name;
    }

    public void setColumn2Name(String column2Name) {
        this.column2Name = column2Name;
    }

    @DatabaseChangeProperty(description = "Name of the column to create", exampleValue = "full_name")
    public String getFinalColumnName() {
        return finalColumnName;
    }

    public void setFinalColumnName(String finalColumnName) {
        this.finalColumnName = finalColumnName;
    }

    @DatabaseChangeProperty(description = "Data type of the column to create", exampleValue = "varchar(255)")
    public String getFinalColumnType() {
        return finalColumnType;
    }

    public void setFinalColumnType(String finalColumnType) {
        this.finalColumnType = finalColumnType;
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}
