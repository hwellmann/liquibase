package liquibase.change.core;

import java.util.ArrayList;
import java.util.List;

import liquibase.change.BaseChange;
import liquibase.change.Change;
import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeWithColumns;
import liquibase.change.ColumnConfig;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;

import org.kohsuke.MetaInfServices;

/**
 * Creates a new table.
 */
@DatabaseChange(name = "createTable", description = "Create Table", priority = ChangeMetaData.PRIORITY_DEFAULT)
@MetaInfServices(Change.class)
public class CreateTableChange extends BaseChange implements ChangeWithColumns<ColumnConfig> {

    private List<ColumnConfig> columns;
    private String catalogName;
    private String schemaName;
    private String tableName;
    private String tablespace;
    private String remarks;

    public CreateTableChange() {
        super();
        columns = new ArrayList<ColumnConfig>();
    }

    @Override
    @DatabaseChangeProperty(requiredForDatabase = "all")
    public List<ColumnConfig> getColumns() {
        if (columns == null) {
            return new ArrayList<ColumnConfig>();
        }
        return columns;
    }

    @Override
    public void setColumns(List<ColumnConfig> columns) {
        this.columns = columns;
    }

    @DatabaseChangeProperty(since = "3.0")
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

    @DatabaseChangeProperty()
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getTablespace() {
        return tablespace;
    }

    public void setTablespace(String tablespace) {
        this.tablespace = tablespace;
    }

    @Override
    public void addColumn(ColumnConfig column) {
        columns.add(column);
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}
