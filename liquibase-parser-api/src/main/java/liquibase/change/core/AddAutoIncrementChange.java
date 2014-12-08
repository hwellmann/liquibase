package liquibase.change.core;

import java.math.BigInteger;

import liquibase.change.BaseChange;
import liquibase.change.Change;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeNote;
import liquibase.change.DatabaseChangeProperty;

import org.kohsuke.MetaInfServices;

/**
 * Makes an existing column into an auto-increment column.
 * This change is only valid for databases with auto-increment/identity columns.
 * The current version does not support MS-SQL.
 */
@DatabaseChange(name="addAutoIncrement", description = "Converts an existing column to be an auto-increment (a.k.a 'identity') column",
        priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "column",
        databaseNotes = {@DatabaseChangeNote(database = "sqlite", notes = "If the column type is not INTEGER it is converted to INTEGER")}
)
@MetaInfServices(Change.class)
public class AddAutoIncrementChange extends BaseChange {

    private String catalogName;
    private String schemaName;
    private String tableName;
    private String columnName;
    private String columnDataType;
    private BigInteger startWith;
    private BigInteger incrementBy;

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

    @DatabaseChangeProperty(mustEqualExisting ="column.relation")
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @DatabaseChangeProperty(mustEqualExisting ="column")
    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    @DatabaseChangeProperty(description = "Current data type of the column to make auto-increment", exampleValue = "int")
    public String getColumnDataType() {
        return columnDataType;
    }

    public void setColumnDataType(String columnDataType) {
        this.columnDataType = columnDataType;
    }

    @DatabaseChangeProperty(exampleValue = "100")
    public BigInteger getStartWith() {
    	return startWith;
    }

    public void setStartWith(BigInteger startWith) {
    	this.startWith = startWith;
    }

    @DatabaseChangeProperty(exampleValue = "1")
    public BigInteger getIncrementBy() {
    	return incrementBy;
    }

    public void setIncrementBy(BigInteger incrementBy) {
    	this.incrementBy = incrementBy;
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}
