package liquibase.action;

import java.util.List;

import liquibase.change.ColumnConfig;
import liquibase.change.core.BaseModifyDataChange;

/**
 * Encapsulates common fields for update and delete changes.
 */
public abstract class AbstractModifyDataAction<T extends BaseModifyDataChange> extends AbstractAction<T> {

    public AbstractModifyDataAction(T change) {
        super(change);
    }

    public String getCatalogName() {
        return change.getCatalogName();
    }

    public void setCatalogName(String catalogName) {
        change.setCatalogName(catalogName);
    }

    public String getSchemaName() {
        return change.getSchemaName();
    }

    public void setSchemaName(String schemaName) {
        change.setSchemaName(schemaName);
    }

    public String getTableName() {
        return change.getTableName();
    }

    public void setTableName(String tableName) {
        change.setTableName(tableName);
    }

    public String getWhere() {
        return change.getWhere();
    }

    public void setWhere(String where) {
        change.setWhere(where);
    }

    public void addWhereParam(ColumnConfig param) {
        change.addWhereParam(param);
    }

    public void removeWhereParam(ColumnConfig param) {
        change.addWhereParam(param);
    }

    public List<ColumnConfig> getWhereParams() {
        return change.getWhereParams();
    }
}
