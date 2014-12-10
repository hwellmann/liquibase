package liquibase.changelog;

import static org.easymock.EasyMock.checkOrder;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import liquibase.database.Database;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.exception.DatabaseException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;

import org.junit.Test;

/**
 * Tests for {@link liquibase.changelog.ChangeSetImpl#execute(DatabaseChangeLogImpl, Database)}
 */
public class ChangeSetExecuteTest {

	private void trainToAcceptAnyNumberOfCommitsOrRollbacks(Database database) {
		try {
			database.commit();
			expectLastCall().anyTimes();
			database.rollback();
			expectLastCall().anyTimes();
		} catch (DatabaseException e) {
			throw new RuntimeException(e);
		}
	}

    private Database createMockDatabaseThatSupportsDdlInTran() {
    	Database database = createMock(Database.class);
    	expect(database.supportsDDLInTransaction()).andStubReturn(true);
    	expect(database.getAutoCommitMode()).andStubReturn(false);
        database.setObjectQuotingStrategy(ObjectQuotingStrategy.LEGACY);
    	return database;
    }

    private Database createMockDatabaseThatDoesNotSupportDdlInTran() {
    	Database database = createMock(Database.class);
    	database.setObjectQuotingStrategy(ObjectQuotingStrategy.LEGACY);
        expect(database.supportsDDLInTransaction()).andStubReturn(false);
    	expect(database.getAutoCommitMode()).andStubReturn(true);
    	return database;
    }

    private Executor createMockExecutor() {
    	Executor executor = createNiceMock(Executor.class);
    	replay(executor);
    	return executor;
    }

    private ExecutableChangeSet createTestChangeSet(boolean runInTransaction) {
    	return new ExecutableChangeSetImpl("test-id", "test-author", false, false, "/test.xml", null, null, runInTransaction, null);
    }

    @Test
	public void testMockDatabaseThatSupportsDdlInTran() {
    	Database database = createMockDatabaseThatSupportsDdlInTran();
    	replay(database);
    	assertTrue(database.supportsDDLInTransaction());
    	assertFalse(database.getAutoCommitMode());
	}

    @Test
	public void testMockDatabaseThatDoesNotSupportDdlInTran() {
    	Database database = createMockDatabaseThatDoesNotSupportDdlInTran();
    	replay(database);
    	assertFalse(database.supportsDDLInTransaction());
    	assertTrue(database.getAutoCommitMode());
	}

    @Test
    public void testExecuteForDatabaseThatSupportsDdlInTranWhenRunInTransactionIsTrue() throws Exception {
    	Database database = createMockDatabaseThatSupportsDdlInTran();
    	ExecutorService.getInstance().setExecutor(database, createMockExecutor());
    	checkOrder(database, true);
    	database.setAutoCommit(false); // before ChangeSet is run
    	checkOrder(database, false);
    	trainToAcceptAnyNumberOfCommitsOrRollbacks(database);
    	// no need to set back after ChangeSet is run because false is the auto-commit mode for
    	// databases that support DDL in transactions
    	replay(database);

    	ExecutableChangeSet changeSet = createTestChangeSet(true);
    	changeSet.execute(new DatabaseChangeLogImpl(), database);

    	verify(database);
    }

    @Test
    public void testExecuteForDatabaseThatSupportsDdlInTranWhenRunInTransactionIsFalse() throws Exception {
    	Database database = createMockDatabaseThatSupportsDdlInTran();
    	ExecutorService.getInstance().setExecutor(database, createMockExecutor());
    	checkOrder(database, true);
    	database.setAutoCommit(true); // before ChangeSet is run
    	checkOrder(database, false);
    	trainToAcceptAnyNumberOfCommitsOrRollbacks(database);
    	checkOrder(database, true);
    	database.setAutoCommit(false); // after ChangeSet is run
    	replay(database);

    	ExecutableChangeSet changeSet = createTestChangeSet(false);
    	changeSet.execute(new DatabaseChangeLogImpl(), database);

    	verify(database);
    }

    @Test
    public void testExecuteForDatabaseThatDoesNotSupportDdlInTranWhenRunInTransactionIsTrue() throws Exception {
    	Database database = createMockDatabaseThatDoesNotSupportDdlInTran();
    	ExecutorService.getInstance().setExecutor(database, createMockExecutor());
    	trainToAcceptAnyNumberOfCommitsOrRollbacks(database);
    	// database.setAutoCommit(boolean) should not be called
    	replay(database);

    	ExecutableChangeSet changeSet = createTestChangeSet(true);
    	changeSet.execute(new DatabaseChangeLogImpl(), database);

    	verify(database);
    }

    @Test
    public void testExecuteForDatabaseThatDoesNotSupportDdlInTranWhenRunInTransactionIsFalse() throws Exception {
    	Database database = createMockDatabaseThatDoesNotSupportDdlInTran();
    	ExecutorService.getInstance().setExecutor(database, createMockExecutor());
    	trainToAcceptAnyNumberOfCommitsOrRollbacks(database);
    	// database.setAutoCommit(boolean) should not be called
    	replay(database);

    	ExecutableChangeSet changeSet = createTestChangeSet(false);
    	changeSet.execute(new DatabaseChangeLogImpl(), database);

    	verify(database);
    }

}
