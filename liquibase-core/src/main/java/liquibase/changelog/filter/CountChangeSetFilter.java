package liquibase.changelog.filter;

import liquibase.changelog.ExecutableChangeSet;

public class CountChangeSetFilter implements ChangeSetFilter {

    private int changeSetsToAllow;
    private int changeSetsSeen = 0;

    public CountChangeSetFilter(int changeSetsToAllow) {
        this.changeSetsToAllow = changeSetsToAllow;
    }

    @Override
    public ChangeSetFilterResult accepts(ExecutableChangeSet changeSet) {
        changeSetsSeen++;

        if (changeSetsSeen <= changeSetsToAllow) {
            return new ChangeSetFilterResult(true, "One of "+changeSetsToAllow+" change sets to run", this.getClass());
        } else {
            return new ChangeSetFilterResult(false, "Only running "+changeSetsToAllow+" change sets", this.getClass());
        }
    }
}
