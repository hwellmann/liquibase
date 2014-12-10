package liquibase.changelog.visitor;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;
import liquibase.changelog.ExecutableChangeSet;
import liquibase.changelog.ExecutableChangeSetImpl;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.DatabaseChangeLogImpl;
import liquibase.database.Database;
import liquibase.database.ObjectQuotingStrategy;

import org.junit.Test;

public class UpdateVisitorTest {

    @Test
    public void visit_unrun() throws Exception {
        Database database = createMock(Database.class);
        database.setObjectQuotingStrategy(ObjectQuotingStrategy.LEGACY);

        ChangeExecListener listener = createMock(ChangeExecListener.class);

        ExecutableChangeSetImpl changeSet = createMock(ExecutableChangeSetImpl.class);
        DatabaseChangeLog databaseChangeLog = new DatabaseChangeLogImpl("test.xml");
        expect(changeSet.execute(databaseChangeLog, listener, database)).andReturn(ExecutableChangeSet.ExecType.EXECUTED);

        expect(database.getRunStatus(changeSet)).andReturn(ExecutableChangeSet.RunStatus.NOT_RAN);

        listener.willRun(changeSet, databaseChangeLog, database, ExecutableChangeSet.RunStatus.NOT_RAN);
        expectLastCall();
        listener.ran(changeSet, databaseChangeLog, database, ExecutableChangeSet.ExecType.EXECUTED);
        expectLastCall();

        database.markChangeSetExecStatus(changeSet, ExecutableChangeSet.ExecType.EXECUTED);
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
