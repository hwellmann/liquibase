package liquibase.action;

import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.ExecutableChange;
import liquibase.change.core.EmptyChange;
import liquibase.database.Database;
import liquibase.statement.SqlStatement;

import org.kohsuke.MetaInfServices;

@DatabaseChange(name="empty", description = "empty", priority = ChangeMetaData.PRIORITY_DEFAULT)
@MetaInfServices(ExecutableChange.class)
public class EmptyAction extends AbstractAction<EmptyChange> {

    public EmptyAction() {
        super(new EmptyChange());
    }

    public EmptyAction(EmptyChange change) {
        super(change);
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[0];
    }

    @Override
    public String getConfirmationMessage() {
        return "Empty change did nothing";
    }
}
