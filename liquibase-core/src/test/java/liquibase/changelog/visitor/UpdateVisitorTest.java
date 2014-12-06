package liquibase.changelog.visitor;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;
import liquibase.changelog.ChangeSetImpl;
import liquibase.changelog.DatabaseChangeLogImpl;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.database.ObjectQuotingStrategy;

import org.junit.Test;

public class UpdateVisitorTest {

    @Test
    public void visit_unrun() throws Exception {
        Database database = createMock(Database.class);
        database.setObjectQuotingStrategy(ObjectQuotingStrategy.LEGACY);

        ChangeExecListener listener = createMock(ChangeExecListener.class);

        ChangeSetImpl changeSet = createMock(ChangeSetImpl.class);
        DatabaseChangeLog databaseChangeLog = new DatabaseChangeLogImpl("test.xml");
        expect(changeSet.execute(databaseChangeLog, listener, database)).andReturn(ChangeSet.ExecType.EXECUTED);

        expect(database.getRunStatus(changeSet)).andReturn(ChangeSet.RunStatus.NOT_RAN);

        listener.willRun(changeSet, databaseChangeLog, database, ChangeSet.RunStatus.NOT_RAN);
        expectLastCall();
        listener.ran(changeSet, databaseChangeLog, database, ChangeSet.ExecType.EXECUTED);
        expectLastCall();

        database.markChangeSetExecStatus(changeSet, ChangeSet.ExecType.EXECUTED);
        expectLastCall();

        database.commit();
        expectLastCall();


        replay(changeSet);
        replay(database);
        replay(listener);

        UpdateVisitor visitor = new UpdateVisitor(database, listener);
        visitor.visit(changeSet, databaseChangeLog, database, null);

        verify(database);
        verify(changeSet);
        verify(listener);
    }

}
