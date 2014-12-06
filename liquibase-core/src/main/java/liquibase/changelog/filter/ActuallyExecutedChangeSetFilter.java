package liquibase.changelog.filter;

import java.util.List;

import liquibase.changelog.ExecutableChangeSet;
import liquibase.changelog.RanChangeSet;

public class ActuallyExecutedChangeSetFilter extends RanChangeSetFilter {

    public ActuallyExecutedChangeSetFilter(List<RanChangeSet> ranChangeSets, boolean ignoreClasspathPrefix) {
        super(ranChangeSets, ignoreClasspathPrefix);
    }

    @Override
    public ChangeSetFilterResult accepts(ExecutableChangeSet changeSet) {
        RanChangeSet ranChangeSet = getRanChangeSet(changeSet);
        if (ranChangeSet != null && (ranChangeSet.getExecType() == null || ranChangeSet.getExecType().equals(ExecutableChangeSet.ExecType.EXECUTED) || ranChangeSet.getExecType().equals(ExecutableChangeSet.ExecType.RERAN))) {
            return new ChangeSetFilterResult(true, "Change set was executed previously", this.getClass());
        } else {
            return new ChangeSetFilterResult(false, "Change set was not previously executed", this.getClass());
        }
    }
}
