package liquibase.changelog.filter;

import liquibase.changelog.ExecutableChangeSet;

public interface ChangeSetFilter {

    public ChangeSetFilterResult accepts(ExecutableChangeSet changeSet);
}
