package liquibase.changelog.visitor;

import liquibase.changelog.ChangeSetImpl;
import liquibase.changelog.DatabaseChangeLogImpl;
import liquibase.database.Database;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.classextension.EasyMock.*;
import org.junit.Test;

public class RollbackVisitorTest {
    @Test
    public void visit() throws Exception {
        Database database = createMock(Database.class);

        ChangeSetImpl changeSet = createMock(ChangeSetImpl.class);
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
