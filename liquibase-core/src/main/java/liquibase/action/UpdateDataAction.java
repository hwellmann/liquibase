package liquibase.action;

import java.util.List;

import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeStatus;
import liquibase.change.ChangeWithColumns;
import liquibase.change.ColumnConfig;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.change.ExecutableChange;
import liquibase.change.core.UpdateDataChange;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.statement.SqlStatement;
import liquibase.statement.UpdateExecutablePreparedStatement;
import liquibase.statement.core.UpdateStatement;

import org.kohsuke.MetaInfServices;

@DatabaseChange(name = "update", description = "Updates data in an existing table", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "table")
@MetaInfServices(ExecutableChange.class)
public class UpdateDataAction extends AbstractModifyDataAction<UpdateDataChange> implements ChangeWithColumns<ColumnConfig> {

    public UpdateDataAction() {
        super(new UpdateDataChange());
    }

    public UpdateDataAction(UpdateDataChange change) {
        super(change);
    }

    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors validate = super.validate(database);
        validate.checkRequiredField("columns", getColumns());
        return validate;
    }

    @Override
    @DatabaseChangeProperty(description = "Data to update", requiredForDatabase = "all")
    public List<ColumnConfig> getColumns() {
        return change.getColumns();
    }

    @Override
    public void setColumns(List<ColumnConfig> columns) {
        change.setColumns(columns);
    }

    @Override
    public void addColumn(ColumnConfig column) {
        change.addColumn(column);
    }

    public void removeColumn(ColumnConfig column) {
        change.removeColumn(column);
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {

    	boolean needsPreparedStatement = false;
        for (ColumnConfig column : getColumns()) {
            if (column.getValueBlobFile() != null) {
                needsPreparedStatement = true;
            }
            if (column.getValueClobFile() != null
                || (column.getType() != null && column.getType().equalsIgnoreCase("CLOB"))) {
                needsPreparedStatement = true;
            }
        }

        if (needsPreparedStatement) {
            UpdateExecutablePreparedStatement statement = new UpdateExecutablePreparedStatement(database, getCatalogName(), getSchemaName(), getTableName(), getColumns(), getChangeSet(), change.getResourceAccessor());

            statement.setWhereClause(getWhere());

            for (ColumnConfig whereParam : getWhereParams()) {
                if (whereParam.getName() != null) {
                    statement.addWhereColumnName(whereParam.getName());
                }
                statement.addWhereParameter(whereParam.getValueObject());
            }

            return new SqlStatement[] {
                    statement
            };
        }

        UpdateStatement statement = new UpdateStatement(getCatalogName(), getSchemaName(), getTableName());

        for (ColumnConfig column : getColumns()) {
            statement.addNewColumnValue(column.getName(), column.getValueObject());
        }

        statement.setWhereClause(getWhere());

        for (ColumnConfig whereParam : getWhereParams()) {
            if (whereParam.getName() != null) {
                statement.addWhereColumnName(whereParam.getName());
            }
            statement.addWhereParameter(whereParam.getValueObject());
        }

        return new SqlStatement[]{
                statement
        };
    }

    @Override
    public ChangeStatus checkStatus(Database database) {
        return new ChangeStatus().unknown("Cannot check updateData status");
    }

    @Override
    public String getConfirmationMessage() {
        return "Data updated in " + getTableName();
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}
