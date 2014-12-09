package liquibase.action;

import liquibase.change.ChangeMetaData;
import liquibase.change.ColumnConfig;
import liquibase.change.DatabaseChange;
import liquibase.change.ExecutableChange;
import liquibase.change.core.DeleteDataChange;
import liquibase.database.Database;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DeleteStatement;

import org.kohsuke.MetaInfServices;

@DatabaseChange(name="delete", description = "Deletes data from an existing table", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "table")
@MetaInfServices(ExecutableChange.class)
public class DeleteDataAction extends AbstractModifyDataAction<DeleteDataChange> {


    public DeleteDataAction() {
        super(new DeleteDataChange());
    }

    public DeleteDataAction(DeleteDataChange change) {
        super(change);
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {

        DeleteStatement statement = new DeleteStatement(getCatalogName(), getSchemaName(), getTableName());

        statement.setWhere(getWhere());

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
    public String getConfirmationMessage() {
        return "Data deleted from " + getTableName();
    }
}
