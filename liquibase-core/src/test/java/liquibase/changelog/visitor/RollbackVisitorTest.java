package liquibase.changelog.visitor;

import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;
import liquibase.changelog.DatabaseChangeLogImpl;
import liquibase.changelog.ExecutableChangeSet;
import liquibase.database.Database;

import org.junit.Test;

public class RollbackVisitorTest {
    @Test
    public void visit() throws Exception {
        Database database = createMock(Database.class);

        ExecutableChangeSet changeSet = createMock(ExecutableChangeSet.class);
        changeSet.rollback(database);
        expectLastCall();


        database.removeRanStatus(changeSet);
        expectLastCall();

        database.commit();
        expectLastCall();


        replay(changeSet);
        replay(database);

        RollbackVisitor visitor = new RollbackVisitor(database);
        visitor.visit(changeSet, new DatabaseChangeLogImpl(), database, null);

        verify(database);
        verify(changeSet);
    }
}
