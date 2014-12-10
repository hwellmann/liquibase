package liquibase.changelog;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import liquibase.Contexts;
import liquibase.RuntimeEnvironment;
import liquibase.changelog.filter.ChangeSetFilterResult;
import liquibase.changelog.filter.ContextChangeSetFilter;
import liquibase.changelog.filter.DbmsChangeSetFilter;
import liquibase.changelog.visitor.ChangeSetVisitor;
import liquibase.database.Database;
import liquibase.database.core.MySQLDatabase;
import liquibase.exception.LiquibaseException;

import org.junit.Before;
import org.junit.Test;

public class ChangeLogIteratorTest {
    private DatabaseChangeLogImpl changeLog;

    @Before
    public void setUp() {
        changeLog = new DatabaseChangeLogImpl();
        changeLog.addChangeSet(new ExecutableChangeSetImpl("1", "nvoxland", false, false, "/path/to/changelog", "test1", "mysql", null));
        changeLog.addChangeSet(new ExecutableChangeSetImpl("2", "nvoxland", false, false, "/path/to/changelog",  "test1", "oracle", null));
        changeLog.addChangeSet(new ExecutableChangeSetImpl("3", "nvoxland", false, false, "/path/to/changelog",  "test2", "mysql", null));
        changeLog.addChangeSet(new ExecutableChangeSetImpl("4", "nvoxland", false, false, "/path/to/changelog",  null, null, null));
        changeLog.addChangeSet(new ExecutableChangeSetImpl("5", "nvoxland", false, false, "/path/to/changelog",  null, "mysql", null));
        changeLog.addChangeSet(new ExecutableChangeSetImpl("6", "nvoxland", false, false, "/path/to/changelog",  "test2", null, null));
    }

    @Test
    public void runChangeSet_emptyFiltersIterator() throws Exception {
        TestChangeSetVisitor testChangeLogVisitor = new TestChangeSetVisitor();

        ChangeLogIterator iterator = new ChangeLogIterator(changeLog);
        iterator.run(testChangeLogVisitor, new RuntimeEnvironment(null, null, null));
        assertEquals(6, testChangeLogVisitor.visitedChangeSets.size());
    }

    @Test
    public void runChangeSet_singleFilterIterator() throws Exception {
        TestChangeSetVisitor testChangeLogVisitor = new TestChangeSetVisitor();

        ChangeLogIterator iterator = new ChangeLogIterator(changeLog, new ContextChangeSetFilter(new Contexts("test1")));
        iterator.run(testChangeLogVisitor, new RuntimeEnvironment(null, null, null));
        assertEquals(4, testChangeLogVisitor.visitedChangeSets.size());
    }

    @Test
    public void runChangeSet_doubleFilterIterator() throws Exception {
        TestChangeSetVisitor testChangeLogVisitor = new TestChangeSetVisitor();

        ChangeLogIterator iterator = new ChangeLogIterator(changeLog, new ContextChangeSetFilter(new Contexts("test1")), new DbmsChangeSetFilter(new MySQLDatabase()));
        iterator.run(testChangeLogVisitor, new RuntimeEnvironment(null, null, null));
        assertEquals(3, testChangeLogVisitor.visitedChangeSets.size());
        assertEquals("1", testChangeLogVisitor.visitedChangeSets.get(0).getId());
        assertEquals("4", testChangeLogVisitor.visitedChangeSets.get(1).getId());
        assertEquals("5", testChangeLogVisitor.visitedChangeSets.get(2).getId());

    }

    @Test
    public void runChangeSet_reverseVisitor() throws Exception {
        TestChangeSetVisitor testChangeLogVisitor = new ReverseChangeSetVisitor();

        ChangeLogIterator iterator = new ChangeLogIterator(changeLog, new ContextChangeSetFilter(new Contexts("test1")), new DbmsChangeSetFilter(new MySQLDatabase()));
        iterator.run(testChangeLogVisitor, new RuntimeEnvironment(null, null, null));
        assertEquals(3, testChangeLogVisitor.visitedChangeSets.size());
        assertEquals("5", testChangeLogVisitor.visitedChangeSets.get(0).getId());
        assertEquals("4", testChangeLogVisitor.visitedChangeSets.get(1).getId());
        assertEquals("1", testChangeLogVisitor.visitedChangeSets.get(2).getId());
    }

    private static class TestChangeSetVisitor implements ChangeSetVisitor {

        public List<ExecutableChangeSet> visitedChangeSets = new ArrayList<ExecutableChangeSet>();


        @Override
        public Direction getDirection() {
            return ChangeSetVisitor.Direction.FORWARD;
        }

        @Override
        public void visit(ExecutableChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database, Set<ChangeSetFilterResult> filterResults) throws LiquibaseException {
            visitedChangeSets.add(changeSet);
        }
    }

    private static class ReverseChangeSetVisitor extends TestChangeSetVisitor {

        @Override
        public Direction getDirection() {
            return ChangeSetVisitor.Direction.REVERSE;
        }
    }
}
