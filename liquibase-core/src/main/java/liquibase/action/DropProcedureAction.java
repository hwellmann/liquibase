package liquibase.action;

import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.ExecutableChange;
import liquibase.change.core.DropProcedureChange;
import liquibase.database.Database;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropProcedureStatement;

import org.kohsuke.MetaInfServices;

@DatabaseChange(name="dropProcedure", description = "Drops an existing procedure", priority = ChangeMetaData.PRIORITY_DEFAULT+100, appliesTo = "storedProcedure")
@MetaInfServices(ExecutableChange.class)
public class DropProcedureAction extends AbstractAction<DropProcedureChange> {

    public DropProcedureAction() {
        super(new DropProcedureChange());
    }

    public DropProcedureAction(DropProcedureChange change) {
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

    public String getProcedureName() {
        return change.getProcedureName();
    }

    public void setProcedureName(String procedureName) {
        change.setProcedureName(procedureName);
    }

    @Override
    public String getConfirmationMessage() {
        return "Stored Procedure "+getProcedureName()+" dropped";
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[]{
                new DropProcedureStatement(getCatalogName(), getSchemaName(), getProcedureName())
        };
    }
}
