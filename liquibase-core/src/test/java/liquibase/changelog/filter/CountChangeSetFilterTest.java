package liquibase.changelog.filter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import liquibase.changelog.ExecutableChangeSetImpl;

import org.junit.Test;

public class CountChangeSetFilterTest  {

    @Test
    public void acceptsZeroCorrectly() {
        CountChangeSetFilter filter = new CountChangeSetFilter(0);
        assertFalse(filter.accepts(new ExecutableChangeSetImpl("a1","b1",false, false, "c1", null, null, null)).isAccepted());
        assertFalse(filter.accepts(new ExecutableChangeSetImpl("a2","b2",false, false, "c2", null, null, null)).isAccepted());
    }

    @Test
    public void acceptsOneCorrectly() {
        CountChangeSetFilter filter = new CountChangeSetFilter(1);
        assertTrue(filter.accepts(new ExecutableChangeSetImpl("a1","b1",false, false, "c1", null, null, null)).isAccepted());
        assertFalse(filter.accepts(new ExecutableChangeSetImpl("a2","b2",false, false, "c2", null, null, null)).isAccepted());
    }

    @Test
    public void acceptsTwoCorrectly() {
        CountChangeSetFilter filter = new CountChangeSetFilter(2);
        assertTrue(filter.accepts(new ExecutableChangeSetImpl("a1","b1",false, false, "c1", null, null, null)).isAccepted());
        assertTrue(filter.accepts(new ExecutableChangeSetImpl("a2","b2",false, false, "c2", null, null, null)).isAccepted());
        assertFalse(filter.accepts(new ExecutableChangeSetImpl("a3","b3",false, false, "c3", null, null, null)).isAccepted());
    }
}
