package liquibase.changelog.visitor;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import liquibase.action.CreateTableAction;
import liquibase.change.ColumnConfig;
import liquibase.changelog.ChangeLogValidator;
import liquibase.changelog.DatabaseChangeLogImpl;
import liquibase.changelog.ExecutableChangeSet;
import liquibase.changelog.ExecutableChangeSetImpl;
import liquibase.changelog.RanChangeSet;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.PreconditionFailedException;
import liquibase.precondition.core.DBMSPrecondition;
import liquibase.precondition.core.PreconditionContainer;
import liquibase.precondition.core.PreconditionService;

import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author chris
 */
public class ValidatingVisitorPreConditionsTest {

    private DatabaseChangeLogImpl changeLog;
    private ExecutableChangeSet changeSet1;

    /**
     * Create a DatabaseChangelog, one changeset, and a create sequence change
     */
    @Before
    public void setUp() {
        changeLog = new DatabaseChangeLogImpl();

        changeSet1 = new ExecutableChangeSetImpl("1", "testAuthor", false, false, "path/changelog", null, null, null);
        changeLog.addChangeSet(changeSet1);

        CreateTableAction change1 = new CreateTableAction();
        change1.setTableName("valid_test");
        change1.addColumn(new ColumnConfig().setName("id").setType("int"));
        changeSet1.addChange(change1);

    }

    /**
     * Test against oracle, but I don't know for sure if the precondition is really
     * validated because oracle supports creating sequences.
     */
    @Test
    public void testPreconditionForOracleOnOracleWithChangeLog() {
        // create the pre condition
        PreconditionContainer preCondition = new PreconditionContainer();
        preCondition.setOnFail(PreconditionContainer.FailOption.MARK_RAN.toString());

        DBMSPrecondition dbmsPrecondition = new DBMSPrecondition();
        dbmsPrecondition.setType("oracle");
        preCondition.addNestedPrecondition(dbmsPrecondition);

        changeSet1.setPreconditions(preCondition);

        OracleDatabase oracleDb = new OracleDatabase() {
            @Override
            public List<RanChangeSet> getRanChangeSetList() throws DatabaseException {
                return new ArrayList<RanChangeSet>();
            }

            @Override
            public void rollback() throws DatabaseException {
                //super.rollback();
            }
        };

        String[] empty = { };
        boolean exceptionThrown = false;

        try {
            new ChangeLogValidator(changeLog).validate(oracleDb, empty);
        } catch (LiquibaseException ex) {
            exceptionThrown = true;
        }
        assertFalse(exceptionThrown);
    }

    /**
     * Test only the precondition tag with a precondition requiring oracle but
     * giving a MSSQL database.
     */
    @Test
    public void testPreConditionsForOracleOnMSSQLWithPreconditionTag() {
        // create the pre condition
        PreconditionContainer preCondition = new PreconditionContainer();
        preCondition.setOnFail(PreconditionContainer.FailOption.MARK_RAN.toString());

        DBMSPrecondition dbmsPrecondition = new DBMSPrecondition();
        dbmsPrecondition.setType("oracle");
        preCondition.addNestedPrecondition(dbmsPrecondition);

        changeSet1.setPreconditions(preCondition);


        MSSQLDatabase mssqlDb = new MSSQLDatabase() {
            @Override
            public List<RanChangeSet> getRanChangeSetList() throws DatabaseException {
                return new ArrayList<RanChangeSet>();
            }

            @Override
            public void rollback() throws DatabaseException {
                //super.rollback();
            }
        };

        boolean failedExceptionThrown = false;
        boolean errorExceptionThrown = false;
        try {
            PreconditionService service = new PreconditionService(preCondition);
            service.check(preCondition, mssqlDb, changeLog, changeSet1);
        } catch (PreconditionFailedException ex) {
            failedExceptionThrown = true;
        } catch (PreconditionErrorException ex) {
            errorExceptionThrown = true;
        }
        assertTrue(failedExceptionThrown);
        assertFalse(errorExceptionThrown);

    }

    /**
     * Test the same precondition from a changelog with mssql database, this
     * should not fail on the validation but just mark is as handled.
     */

    @Test
    public void testPreConditionsForOracleOnMSSQLWithChangeLog() {
        // create the pre condition
        PreconditionContainer preCondition = new PreconditionContainer();
        preCondition.setOnFail(PreconditionContainer.FailOption.MARK_RAN.toString());

        DBMSPrecondition dbmsPrecondition = new DBMSPrecondition();
        dbmsPrecondition.setType("oracle");
        preCondition.addNestedPrecondition(dbmsPrecondition);

        changeSet1.setPreconditions(preCondition);

        MSSQLDatabase mssqlDb = new MSSQLDatabase() {
            @Override
            public List<RanChangeSet> getRanChangeSetList() throws DatabaseException {
                return new ArrayList<RanChangeSet>();
            }

            @Override
            public void rollback() throws DatabaseException {
                //super.rollback();
            }
        };

        String[] empty = { };
        boolean exceptionThrown = false;

        try {
            // call the validate which gives the error
            new ChangeLogValidator(changeLog).validate(mssqlDb, empty);
        } catch (LiquibaseException ex) {
            System.out.println(ex.getMessage());
            exceptionThrown = true;
        }
        assertFalse(exceptionThrown);
    }



}
