package liquibase.action;

import java.util.ArrayList;
import java.util.List;

import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeStatus;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.change.ExecutableChange;
import liquibase.change.core.LoadUpdateDataChange;
import liquibase.database.Database;
import liquibase.datatype.DataTypeFactory;
import liquibase.exception.LiquibaseException;
import liquibase.exception.RollbackImpossibleException;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DeleteStatement;
import liquibase.statement.core.InsertOrUpdateStatement;
import liquibase.statement.core.InsertStatement;

import org.kohsuke.MetaInfServices;

@DatabaseChange(name="loadUpdateData",
        description = "Loads or updates data from a CSV file into an existing table. Differs from loadData by issuing a SQL batch that checks for the existence of a record. If found, the record is UPDATEd, else the record is INSERTed. Also, generates DELETE statements for a rollback.\n" +
                "\n" +
                "A value of NULL in a cell will be converted to a database NULL rather than the string 'NULL'",
        priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "table", since = "2.0")
@MetaInfServices(ExecutableChange.class)
public class LoadUpdateDataAction extends LoadDataAction {

    public LoadUpdateDataAction() {
        super(new LoadUpdateDataChange());
    }

    public LoadUpdateDataAction(LoadUpdateDataChange change) {
        super(change);
    }

    @Override
    public LoadUpdateDataChange getChange() {
        return (LoadUpdateDataChange) change;
    }

    @Override
    public String getTableName() {
        return change.getTableName();
    }

    @Override
    public void setTableName(String tableName) {
        change.setTableName(tableName);
    }

    public void setPrimaryKey(String primaryKey) throws LiquibaseException {
        getChange().setPrimaryKey(primaryKey);
    }

    @DatabaseChangeProperty(description = "Comma delimited list of the columns for the primary key", requiredForDatabase = "all")
    public String getPrimaryKey() {
        return getChange().getPrimaryKey();
    }

    @DatabaseChangeProperty(description = "If true, records with no matching database record should be ignored", since = "3.3" )
    public Boolean getOnlyUpdate() {
        return getChange().getOnlyUpdate();
	}

	public void setOnlyUpdate(Boolean onlyUpdate) {
		getChange().setOnlyUpdate(onlyUpdate);
	}

    @Override
    protected InsertStatement createStatement(String catalogName, String schemaName, String tableName) {
        return new InsertOrUpdateStatement(catalogName, schemaName, tableName, getPrimaryKey(), getOnlyUpdate());
    }

    @Override
    public SqlStatement[] generateRollbackStatements(Database database) throws RollbackImpossibleException {
        List<SqlStatement> statements = new ArrayList<SqlStatement>();
        SqlStatement[] forward = this.generateStatements(database);

        for(SqlStatement thisForward: forward){
            InsertOrUpdateStatement thisInsert = (InsertOrUpdateStatement)thisForward;
            DeleteStatement delete = new DeleteStatement(change.getCatalogName(), change.getSchemaName(),getTableName());
            delete.setWhere(getWhere(thisInsert,database));
            statements.add(delete);
        }

        return statements.toArray(new SqlStatement[statements.size()]);
    }

    private String getWhere(InsertOrUpdateStatement insertOrUpdateStatement, Database database) {
        StringBuilder where = new StringBuilder();

        String[] pkColumns = insertOrUpdateStatement.getPrimaryKey().split(",");

        for(String thisPkColumn:pkColumns)
        {
            Object newValue = insertOrUpdateStatement.getColumnValues().get(thisPkColumn);
            where.append(database.escapeColumnName(insertOrUpdateStatement.getCatalogName(),
                        insertOrUpdateStatement.getSchemaName(),
                        insertOrUpdateStatement.getTableName(),
                        thisPkColumn)).append(newValue == null || newValue.toString().equalsIgnoreCase("NULL") ? " is " : " = ");

            if (newValue == null || newValue.toString().equalsIgnoreCase("NULL")) {
                where.append("NULL");
            } else {
                where.append(DataTypeFactory.getInstance().fromObject(newValue, database).objectToSql(newValue, database));
            }

            where.append(" AND ");
        }

        where.delete(where.lastIndexOf(" AND "),where.lastIndexOf(" AND ") + " AND ".length());
        return where.toString();
    }

    @Override
    public ChangeStatus checkStatus(Database database) {
        return new ChangeStatus().unknown("Cannot check loadUpdateData status");
    }

}
