package liquibase.statement.core;

import liquibase.changelog.ChangeSetImpl;
import liquibase.changelog.ChangeSet;
import liquibase.statement.AbstractSqlStatement;

public class MarkChangeSetRanStatement extends AbstractSqlStatement {

    private ChangeSet changeSet;

    private ChangeSetImpl.ExecType execType;

    public MarkChangeSetRanStatement(ChangeSet changeSet, ChangeSetImpl.ExecType execType) {
        this.changeSet = changeSet;
        this.execType = execType;
    }

    public ChangeSet getChangeSet() {
        return changeSet;
    }

    public ChangeSetImpl.ExecType getExecType() {
        return execType;
    }
}
