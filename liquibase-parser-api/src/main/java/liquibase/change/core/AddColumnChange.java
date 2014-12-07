package liquibase.change.core;

import java.util.ArrayList;
import java.util.List;

import liquibase.change.AddColumnConfig;
import liquibase.change.BaseChange;
import liquibase.change.Change;
import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeWithColumns;
import liquibase.change.ColumnConfig;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;

import org.kohsuke.MetaInfServices;

/**
 * Adds a column to an existing table.
 */
@DatabaseChange(name="addColumn", description = "Adds a new column to an existing table", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "table")
@MetaInfServices(Change.class)
public class AddColumnChange extends BaseChange implements ChangeWithColumns<AddColumnConfig> {

    private String catalogName;
    private String schemaName;
    private String tableName;
    private List<AddColumnConfig> columns;

    public AddColumnChange() {
        columns = new ArrayList<AddColumnConfig>();
    }

    @DatabaseChangeProperty(mustEqualExisting ="relation.catalog", since = "3.0")
    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    @DatabaseChangeProperty(mustEqualExisting ="relation.schema")
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @DatabaseChangeProperty(mustEqualExisting ="table", description = "Name of the table to add the column to")
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @Override
    @DatabaseChangeProperty(description = "Column constraint and foreign key information. Setting the \"defaultValue\" attribute will specify a default value for the column. Setting the \"value\" attribute will set all rows existing to the specified value without modifying the column default.", requiredForDatabase = "all")
    public List<AddColumnConfig> getColumns() {
        return columns;
    }

    @Override
    public void setColumns(List<AddColumnConfig> columns) {
        this.columns = columns;
    }

    @Override
    public void addColumn(AddColumnConfig column) {
        this.columns.add(column);
    }

    public void removeColumn(ColumnConfig column) {
        this.columns.remove(column);
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

}
