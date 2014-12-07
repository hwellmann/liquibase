package liquibase.change;

import liquibase.change.core.AddAutoIncrementChange;
import liquibase.change.core.CreateTableChange;
import liquibase.change.core.DropTableChange;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.util.LiquibaseService;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.TreeSet;

import static org.junit.Assert.*;

public class ChangeFactoryTest {

    @Before
    public void setup() {
        ExecutableChangeFactory.reset();
        SometimesExceptionThrowingChange.timesCalled = 0;
    }

    @After
    public void resetRegistry() {
        ExecutableChangeFactory.reset();
    }

    @Test
    public void constructor() {
        ExecutableChangeFactory instance = ExecutableChangeFactory.getInstance();
        assertTrue(instance.getRegistry().containsKey("createTable"));
        assertTrue(instance.getRegistry().containsKey("dropTable"));
    }

    @Test
    public void getInstance() {
        assertNotNull(ExecutableChangeFactory.getInstance());

        assertTrue(ExecutableChangeFactory.getInstance() == ExecutableChangeFactory.getInstance());
    }

    @Test
    public void reset() {
        ExecutableChangeFactory instance1 = ExecutableChangeFactory.getInstance();
        ExecutableChangeFactory.reset();
        assertFalse(instance1 == ExecutableChangeFactory.getInstance());
    }

    @Test
    public void clear() {
        ExecutableChangeFactory changeFactory = ExecutableChangeFactory.getInstance();
        assertTrue(changeFactory.getRegistry().size() > 5);
        changeFactory.clear();
        assertEquals(0, changeFactory.getRegistry().size());
    }

    @Test
    public void register() {
        ExecutableChangeFactory changeFactory = ExecutableChangeFactory.getInstance();
        changeFactory.clear();

        assertEquals(0, changeFactory.getRegistry().size());
        changeFactory.register(CreateTableChange.class);

        assertEquals(1, changeFactory.getRegistry().size());
        assertTrue(changeFactory.getRegistry().containsKey("createTable"));

        changeFactory.register(Priority10Change.class);
        changeFactory.register(Priority5Change.class);
        changeFactory.register(AnotherPriority5Change.class); //only one should be stored

        assertEquals(3, changeFactory.getRegistry().get("createTable").size());
        assertEquals(Priority10Change.class, changeFactory.getRegistry().get("createTable").iterator().next());
    }

    @Test(expected = UnexpectedLiquibaseException.class)
    public void register_badClassRightAway() {
        ExecutableChangeFactory changeFactory = ExecutableChangeFactory.getInstance();

        changeFactory.register(ExceptionThrowingChange.class);
    }

    @Test(expected = UnexpectedLiquibaseException.class)
    public void register_badClassLaterInComparator() {
        ExecutableChangeFactory changeFactory = ExecutableChangeFactory.getInstance();

        changeFactory.register(SometimesExceptionThrowingChange.class);
        changeFactory.register(Priority5Change.class);
        changeFactory.register(Priority10Change.class);
    }

    @Test
    public void unregister_instance() {
        ExecutableChangeFactory factory = ExecutableChangeFactory.getInstance();

        factory.clear();

        assertEquals(0, factory.getRegistry().size());

        AddAutoIncrementChange change = new AddAutoIncrementChange();

        factory.register(CreateTableChange.class);
        factory.register(change.getClass());
        factory.register(DropTableChange.class);

        assertEquals(3, factory.getRegistry().size());

        factory.unregister(ExecutableChangeFactory.getInstance().getChangeMetaData(change).getName());
        assertEquals(2, factory.getRegistry().size());
    }

    @Test
    public void unregister_doesNotExist() {
        ExecutableChangeFactory factory = ExecutableChangeFactory.getInstance();

        factory.clear();

        assertEquals(0, factory.getRegistry().size());

        factory.register(CreateTableChange.class);
        factory.register(AddAutoIncrementChange.class);
        factory.register(DropTableChange.class);

        assertEquals(3, factory.getRegistry().size());

        factory.unregister("doesNoExist");
        assertEquals(3, factory.getRegistry().size());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getRegistry() {
        ExecutableChangeFactory.getInstance().getRegistry().put("x", new TreeSet<Class<? extends ExecutableChange>>());
    }

    @Test
    public void create_exists() {
        Change change = ExecutableChangeFactory.getInstance().create("createTable");

        assertNotNull(change);
        assertTrue(change instanceof CreateTableChange);

        assertNotSame(change, ExecutableChangeFactory.getInstance().create("createTable"));
    }

    @Test
    public void create_notExists() {
        Change change = ExecutableChangeFactory.getInstance().create("badChangeName");

        assertNull(change);

    }

    @Test(expected = UnexpectedLiquibaseException.class)
    public void create_badClass() {
        ExecutableChangeFactory.getInstance().register(SometimesExceptionThrowingChange.class);
        Change change = ExecutableChangeFactory.getInstance().create("createTable");

        assertNotNull(change);
        assertTrue(change instanceof CreateTableChange);

    }

    @LiquibaseService(skip = true)
    public static class Priority5Change extends CreateTableChange {
        @Override
        public ChangeMetaData createChangeMetaData() {
            return new ChangeMetaData("createTable", null, 5, null, null, null);
        }
    }

    @LiquibaseService(skip = true)
    public static class Priority10Change extends CreateTableChange {
        @Override
        public ChangeMetaData createChangeMetaData() {
            return new ChangeMetaData("createTable", null, 10, null, null, null);
        }
    }

    @LiquibaseService(skip = true)
    public static class AnotherPriority5Change extends CreateTableChange {
        @Override
        public ChangeMetaData createChangeMetaData() {
            return new ChangeMetaData("createTable", null, 5, null, null, null);
        }
    }

    @LiquibaseService(skip = true)
    public static class ExceptionThrowingChange extends CreateTableChange {
        public ExceptionThrowingChange() {
            throw new RuntimeException("I throw exceptions");
        }

        @Override
        public ChangeMetaData createChangeMetaData() {
            return new ChangeMetaData("createTable", null, 15, null, null, null);
        }
    }

    @LiquibaseService(skip = true)
    public static class SometimesExceptionThrowingChange extends CreateTableChange {
        private static int timesCalled = 0;
        public SometimesExceptionThrowingChange() {
            if (timesCalled > 1) {
                throw new RuntimeException("I throw exceptions");
            }
            timesCalled++;
        }

        @Override
        public ChangeMetaData createChangeMetaData() {
            return new ChangeMetaData("createTable", null, 15, null, null, null);
        }
    }
}
