package liquibase.changelog.filter;

import liquibase.changelog.ChangeSetImpl;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.ChangeSet;

import java.util.HashSet;
import java.util.Set;

public class NotInChangeLogChangeSetFilter implements ChangeSetFilter {

    private Set<ChangeSetImpl> changeSets;

    public NotInChangeLogChangeSetFilter(DatabaseChangeLog databaseChangeLog) {
        this.changeSets = new HashSet<ChangeSetImpl>(databaseChangeLog.getChangeSets());
    }

    @Override
    public ChangeSetFilterResult accepts(ChangeSet changeSet) {
        if (changeSets.contains(changeSet)) {
            return new ChangeSetFilterResult(false, "Change set is in change log", this.getClass());
        } else {
            return new ChangeSetFilterResult(true, "Change set is not in change log", this.getClass());
        }
    }
}
