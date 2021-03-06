package liquibase.changelog.filter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Date;

import liquibase.change.CheckSum;
import liquibase.changelog.ExecutableChangeSetImpl;
import liquibase.changelog.RanChangeSet;
import liquibase.exception.RollbackFailedException;

import org.junit.Test;

public class AfterTagChangeSetFilterTest {

    @Test
    public void accepts_noTag() throws Exception {
        try {
            new AfterTagChangeSetFilter("tag1", new ArrayList<RanChangeSet>());
            fail("Did not throw exception");
        } catch (RollbackFailedException e) {
            ; //what we wanted
        }
    }

    @Test
    public void accepts() throws Exception {
        ArrayList<RanChangeSet> ranChanges = new ArrayList<RanChangeSet>();
        ranChanges.add(new RanChangeSet("path/changelog", "1", "testAuthor", CheckSum.parse("12345"), new Date(), null, null, null, null));
        ranChanges.add(new RanChangeSet("path/changelog", "2", "testAuthor", CheckSum.parse("12345"), new Date(), "tag1", null, null, null));
        ranChanges.add(new RanChangeSet("path/changelog", "3", "testAuthor", CheckSum.parse("12345"), new Date(), null, null, null, null));
        AfterTagChangeSetFilter filter = new AfterTagChangeSetFilter("tag1", ranChanges);

        assertFalse(filter.accepts(new ExecutableChangeSetImpl("1", "testAuthor", false, false, "path/changelog", null, null, null)).isAccepted());
        assertFalse(filter.accepts(new ExecutableChangeSetImpl("2", "testAuthor", false, false, "path/changelog", null, null, null)).isAccepted());
        assertTrue(filter.accepts(new ExecutableChangeSetImpl("3", "testAuthor", false, false, "path/changelog", null, null, null)).isAccepted());

    }
}
