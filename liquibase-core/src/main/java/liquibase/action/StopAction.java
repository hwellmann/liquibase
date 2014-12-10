package liquibase.action;

import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.ExecutableChange;
import liquibase.change.core.StopChange;
import liquibase.change.core.StopChange.StopChangeException;
import liquibase.database.Database;
import liquibase.sql.Sql;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.RuntimeStatement;

import org.kohsuke.MetaInfServices;

@DatabaseChange(name="stop", description = "Stops Liquibase execution with a message. Mainly useful for debugging and stepping through a changelog", priority = ChangeMetaData.PRIORITY_DEFAULT, since = "1.9")
@MetaInfServices(ExecutableChange.class)
public class StopAction extends AbstractAction<StopChange> {

    public StopAction() {
        super(new StopChange());
    }

    public StopAction(StopChange change) {
        super(change);
    }

    @Override
    public boolean generateStatementsVolatile(Database database) {
        return true;
    }

    public String getMessage() {
        return change.getMessage();
    }

    public void setMessage(String message) {
        change.setMessage(message);
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[] { new RuntimeStatement() {
            @Override
            public Sql[] generate(Database database) {
                throw new StopChangeException(getMessage());
            }
        }};

    }

    @Override
    public String getConfirmationMessage() {
        return "Changelog Execution Stopped";
    }
}
