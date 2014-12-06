package liquibase.changelog.filter;

import java.util.HashSet;
import java.util.Set;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.ExecutableChangeSet;

public class NotInChangeLogChangeSetFilter implements ChangeSetFilter {

    private Set<ChangeSet> changeSets;

    public NotInChangeLogChangeSetFilter(DatabaseChangeLog databaseChangeLog) {
        this.changeSets = new HashSet<ChangeSet>(databaseChangeLog.getChangeSets());
    }

    @Override
    public ChangeSetFilterResult accepts(ExecutableChangeSet changeSet) {
        if (changeSets.contains(changeSet)) {
            return new ChangeSetFilterResult(false, "Change set is in change log", this.getClass());
        } else {
            return new ChangeSetFilterResult(true, "Change set is not in change log", this.getClass());
        }
    }
}
