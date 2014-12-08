package liquibase.change.core;

import java.util.ArrayList;
import java.util.Collection;
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
 * Creates an index on an existing column.
 */
@DatabaseChange(name = "createIndex", description = "Creates an index on an existing column or set of columns.", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "index")
@MetaInfServices(Change.class)
public class CreateIndexChange extends BaseChange implements ChangeWithColumns<AddColumnConfig> {

    private String catalogName;
    private String schemaName;
    private String tableName;
    private String indexName;
    private Boolean unique;
    private String tablespace;
    private List<AddColumnConfig> columns;

    // Contain associations of index
    // for example: foreignKey, primaryKey or uniqueConstraint
    private String associatedWith;
    private Boolean clustered;

    public CreateIndexChange() {
        columns = new ArrayList<AddColumnConfig>();
    }

    @DatabaseChangeProperty(mustEqualExisting = "index", description = "Name of the index to create")
    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "index.schema")
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "index.table", description = "Name of the table to add the index to", exampleValue = "person")
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @Override
    @DatabaseChangeProperty(mustEqualExisting = "index.column", description = "Column(s) to add to the index", requiredForDatabase = "all")
    public List<AddColumnConfig> getColumns() {
        if (columns == null) {
            return new ArrayList<AddColumnConfig>();
        }
        return columns;
    }

    @Override
    public void setColumns(List<AddColumnConfig> columns) {
        this.columns = columns;
    }

    @Override
    public void addColumn(AddColumnConfig column) {
        columns.add(column);
    }

    @DatabaseChangeProperty(description = "Tablepace to create the index in.")
    public String getTablespace() {
        return tablespace;
    }

    public void setTablespace(String tablespace) {
        this.tablespace = tablespace;
    }

    /**
     * @param isUnique
     *            the isUnique to set
     */
    public void setUnique(Boolean isUnique) {
        this.unique = isUnique;
    }

    @DatabaseChangeProperty(description = "Unique values index", since = "1.8")
    public Boolean isUnique() {
        return this.unique;
    }

    /**
     * @return Index associations. Valid values:<br>
     *         <li>primaryKey</li> <li>foreignKey</li> <li>uniqueConstraint</li> <li>none</li>
     * */
    @DatabaseChangeProperty(isChangeProperty = false)
    public String getAssociatedWith() {
        return associatedWith;
    }

    public void setAssociatedWith(String associatedWith) {
        this.associatedWith = associatedWith;
    }

    @DatabaseChangeProperty(since = "3.0")
    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    public Boolean getClustered() {
        return clustered;
    }

    public void setClustered(Boolean clustered) {
        this.clustered = clustered;
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    @Override
    public Object getSerializableFieldValue(String field) {
        Object value = super.getSerializableFieldValue(field);
        if (value != null && field.equals("columns")) {
            for (ColumnConfig config : (Collection<ColumnConfig>) value) {
                config.setType(null);
                config.setAutoIncrement(null);
                config.setConstraints(null);
                config.setDefaultValue(null);
                config.setValue(null);
                config.setStartWith(null);
                config.setIncrementBy(null);
                config.setEncoding(null);
                config.setRemarks(null);
            }

        }
        return value;
    }
}
