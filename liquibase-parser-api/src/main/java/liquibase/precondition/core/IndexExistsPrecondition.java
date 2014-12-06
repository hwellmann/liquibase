package liquibase.precondition.core;

import liquibase.precondition.AbstractPrecondition;
import liquibase.precondition.Precondition;
import liquibase.util.StringUtils;

import org.kohsuke.MetaInfServices;

@MetaInfServices(Precondition.class)
public class IndexExistsPrecondition extends AbstractPrecondition {
    private String catalogName;
    private String schemaName;
    private String tableName;
    private String columnNames;
    private String indexName;

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

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

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public String getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(String columnNames) {
        this.columnNames = columnNames;
    }

//    public ValidationErrors validate(Database database) {
//        ValidationErrors validationErrors = new ValidationErrors();
//        if (getIndexName() == null && getTableName() == null && getColumnNames() == null) {
//            validationErrors.addError("indexName OR tableName and columnNames is required");
//        }
//        return validationErrors;
//    }

    @Override
    public String getName() {
        return "indexExists";
    }

    @Override
    public String toString() {
        String string = "Index Exists Precondition: ";

        if (getIndexName() != null) {
            string += getIndexName();
        }

        if (tableName != null) {
            string += " on "+getTableName();

            if (StringUtils.trimToNull(getColumnNames()) != null) {
                string += " columns "+getColumnNames();
            }
        }

        return string;
    }
}