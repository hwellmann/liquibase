package liquibase.changelog.visitor;

import liquibase.changelog.ChangeSetImpl;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.filter.ChangeSetFilterResult;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;

import java.util.Set;

public class ChangeLogSyncVisitor implements ChangeSetVisitor {

    private Database database;
    private ChangeLogSyncListener listener;

    public ChangeLogSyncVisitor(Database database) {
        this.database = database;
    }

    public ChangeLogSyncVisitor(Database database, ChangeLogSyncListener listener) {
        this.database = database;
        this.listener = listener;
    }

    @Override
    public Direction getDirection() {
        return ChangeSetVisitor.Direction.FORWARD;
    }

    @Override
    public void visit(ChangeSetImpl changeSet, DatabaseChangeLog databaseChangeLog, Database database, Set<ChangeSetFilterResult> filterResults) throws LiquibaseException {
        this.database.markChangeSetExecStatus(changeSet, ChangeSet.ExecType.EXECUTED);
        if(listener != null) {
            listener.markedRan(changeSet, databaseChangeLog, database);
        }
    }
}
