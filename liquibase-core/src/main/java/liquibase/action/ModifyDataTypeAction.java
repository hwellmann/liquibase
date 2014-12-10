package liquibase.action;

import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.change.ExecutableChange;
import liquibase.change.core.ModifyDataTypeChange;
import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.ModifyDataTypeStatement;
import liquibase.statement.core.ReorganizeTableStatement;

import org.kohsuke.MetaInfServices;

@DatabaseChange(name="modifyDataType", description = "Modify data type", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "column")
@MetaInfServices(ExecutableChange.class)
public class ModifyDataTypeAction extends AbstractAction<ModifyDataTypeChange> {

    public ModifyDataTypeAction() {
        super(new ModifyDataTypeChange());
    }

    public ModifyDataTypeAction(ModifyDataTypeChange change) {
        super(change);
    }

    @Override
    public String getConfirmationMessage() {
        return getTableName() +"."+ getColumnName() +" datatype was changed to "+ getNewDataType();
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        ModifyDataTypeStatement modifyDataTypeStatement = new ModifyDataTypeStatement(getCatalogName(), getSchemaName(), getTableName(), getColumnName(), getNewDataType());
        if (database instanceof DB2Database) {
            return new SqlStatement[] {
                    modifyDataTypeStatement,
                    new ReorganizeTableStatement(getCatalogName(), getSchemaName(), getTableName())
            };
        } else {
            return new SqlStatement[] {
                    modifyDataTypeStatement
            };
        }
    }

    @DatabaseChangeProperty(mustEqualExisting ="column.relation.catalog", since = "3.0")
    public String getCatalogName() {
        return change.getCatalogName();
    }

    public void setCatalogName(String catalogName) {
        change.setCatalogName(catalogName);
    }

    @DatabaseChangeProperty(mustEqualExisting ="column.relation.schema")
    public String getSchemaName() {
        return change.getSchemaName();
    }

    public void setSchemaName(String schemaName) {
        change.setSchemaName(schemaName);
    }

    @DatabaseChangeProperty(mustEqualExisting = "column.relation")
    public String getTableName() {
        return change.getTableName();
    }

    public void setTableName(String tableName) {
        change.setTableName(tableName);
    }

    @DatabaseChangeProperty(mustEqualExisting = "column")
    public String getColumnName() {
        return change.getColumnName();
    }

    public void setColumnName(String columnName) {
        change.setColumnName(columnName);
    }

    @DatabaseChangeProperty()
    public String getNewDataType() {
        return change.getNewDataType();
    }

    public void setNewDataType(String newDataType) {
        change.setNewDataType(newDataType);
    }
}
