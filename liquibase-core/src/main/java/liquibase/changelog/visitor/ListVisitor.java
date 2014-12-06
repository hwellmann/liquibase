package liquibase.changelog.visitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import liquibase.changelog.ExecutableChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.filter.ChangeSetFilterResult;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;

public class ListVisitor implements ChangeSetVisitor {

    private List<ExecutableChangeSet> seenChangeSets = new ArrayList<ExecutableChangeSet>();

    public List<ExecutableChangeSet> getSeenChangeSets() {
        return seenChangeSets;
    }

    @Override
    public Direction getDirection() {
        return ChangeSetVisitor.Direction.FORWARD;
    }

    @Override
    public void visit(ExecutableChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database, Set<ChangeSetFilterResult> filterResults) throws LiquibaseException {
        seenChangeSets.add(changeSet);
    }
}