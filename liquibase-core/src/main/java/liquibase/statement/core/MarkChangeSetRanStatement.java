package liquibase.statement.core;

import liquibase.changelog.ExecutableChangeSetImpl;
import liquibase.changelog.ChangeSet;
import liquibase.statement.AbstractSqlStatement;

public class MarkChangeSetRanStatement extends AbstractSqlStatement {

    private ChangeSet changeSet;

    private ExecutableChangeSetImpl.ExecType execType;

    public MarkChangeSetRanStatement(ChangeSet changeSet, ExecutableChangeSetImpl.ExecType execType) {
        this.changeSet = changeSet;
        this.execType = execType;
    }

    public ChangeSet getChangeSet() {
        return changeSet;
    }

    public ExecutableChangeSetImpl.ExecType getExecType() {
        return execType;
    }
}
