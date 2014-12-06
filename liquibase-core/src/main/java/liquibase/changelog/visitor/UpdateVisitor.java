package liquibase.changelog.visitor;

import java.util.Set;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.ChangeSetImpl;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.ExecutableChangeSet;
import liquibase.changelog.ExecutableChangeSet.RunStatus;
import liquibase.changelog.filter.ChangeSetFilterResult;
import liquibase.database.Database;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.exception.LiquibaseException;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;

public class UpdateVisitor implements ChangeSetVisitor {

    private Database database;

    private Logger log = LogFactory.getLogger();

    private ChangeExecListener execListener;

    public UpdateVisitor(Database database) {
        this.database = database;
    }

    public UpdateVisitor(Database database, ChangeExecListener execListener) {
      this(database);
      this.execListener = execListener;
    }

    @Override
    public Direction getDirection() {
        return ChangeSetVisitor.Direction.FORWARD;
    }

    @Override
    public void visit(ExecutableChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database, Set<ChangeSetFilterResult> filterResults) throws LiquibaseException {
        ChangeSetImpl.RunStatus runStatus = this.database.getRunStatus(changeSet);
        log.debug("Running Changeset:" + changeSet);
        fireWillRun(changeSet, databaseChangeLog, database, runStatus);
        ChangeSetImpl.ExecType execType = changeSet.execute(databaseChangeLog, execListener, this.database);
        if (!runStatus.equals(RunStatus.NOT_RAN)) {
            execType = ExecutableChangeSet.ExecType.RERAN;
        }
        fireRan(changeSet, databaseChangeLog, database, execType);
        // reset object quoting strategy after running changeset
        this.database.setObjectQuotingStrategy(ObjectQuotingStrategy.LEGACY);
        this.database.markChangeSetExecStatus(changeSet, execType);

        this.database.commit();
    }

    private void fireWillRun(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database2, ExecutableChangeSet.RunStatus runStatus) {
      if (execListener != null) {
        execListener.willRun(changeSet, databaseChangeLog, database, runStatus);
      }
    }

    private void fireRan(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database2, ExecutableChangeSet.ExecType execType) {
      if (execListener != null) {
        execListener.ran(changeSet, databaseChangeLog, database, execType);
      }
    }
}
