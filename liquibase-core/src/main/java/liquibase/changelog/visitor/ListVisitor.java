package liquibase.changelog.visitor;

import liquibase.changelog.ChangeSetImpl;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.filter.ChangeSetFilterResult;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;

import java.util.*;

public class ListVisitor implements ChangeSetVisitor {

    private List<ChangeSetImpl> seenChangeSets = new ArrayList<ChangeSetImpl>();

    public List<ChangeSetImpl> getSeenChangeSets() {
        return seenChangeSets;
    }

    @Override
    public Direction getDirection() {
        return ChangeSetVisitor.Direction.FORWARD;
    }

    @Override
    public void visit(ChangeSetImpl changeSet, DatabaseChangeLog databaseChangeLog, Database database, Set<ChangeSetFilterResult> filterResults) throws LiquibaseException {
        seenChangeSets.add(changeSet);
    }
}