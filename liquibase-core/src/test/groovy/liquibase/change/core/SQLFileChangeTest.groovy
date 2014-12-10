package liquibase.change.core

import static org.junit.Assert.assertEquals
import liquibase.action.RawSQLAction
import liquibase.action.SQLFileAction
import liquibase.change.Change
import liquibase.change.ChangeStatus
import liquibase.change.StandardChangeTest
import liquibase.changelog.ChangeLogParameters
import liquibase.changelog.ChangeLogParametersImpl
import liquibase.changelog.ChangeSet
import liquibase.changelog.ChangeSetImpl
import liquibase.exception.UnexpectedLiquibaseException
import liquibase.sdk.database.MockDatabase
import liquibase.sdk.resource.MockResourceAccessor
import liquibase.statement.SqlStatement

public class SQLFileChangeTest extends StandardChangeTest {

    def "generateStatements throws Exception if file does not exist"() throws Exception {
        when:
        def change = new SQLFileAction();
        change.setPath("doesnotexist.sql");
        change.finishInitialization();

        change.generateStatements(new MockDatabase())

        then:
         thrown(UnexpectedLiquibaseException.class)
    }

    def "lines from file parse into one or more statements correctly"() throws Exception {
        when:
        SQLFileAction change2 = new SQLFileAction();
        change2.setSql(fileContents);
        MockDatabase database = new MockDatabase();
        SqlStatement[] statements = change2.generateStatements(database);

        then:
        statements.length == expectedStatements.size();
        for (int i = 0; i < expectedStatements.size(); i++) {
            assert expectedStatements[i] == statements[i].sql
        }

        where:
        fileContents | expectedStatements
        "SELECT * FROM customer;"                                                | ["SELECT * FROM customer"]
        "SELECT * FROM customer;\nSELECT * from table;\nSELECT * from table2;\n" | ["SELECT * FROM customer", "SELECT * from table", "SELECT * from table2"]
        "SELECT * FROM customer\ngo"                                             | ["SELECT * FROM customer"]
        "goSELECT * FROM customer\ngo" | ["goSELECT * FROM customer"]
        "SELECT * FROM customer\ngo\nSELECT * FROM table\ngo" | ["SELECT * FROM customer", "SELECT * FROM table"]
        "SELECT * FROM go\ngo\nSELECT * from gogo\ngo\n" | ["SELECT * FROM go", "SELECT * from gogo"]
        "insert into table ( col ) values (' value with; semicolon ');" | ["insert into table ( col ) values (' value with; semicolon ')"]
        "--\n-- This is a comment\nUPDATE tablename SET column = 1;\nGO" | ["--\n-- This is a comment\nUPDATE tablename SET column = 1", "GO"]
    }


    def getConfirmationMessage() throws Exception {
        when:
        def change = new SQLFileAction();
        change.setPath("com/example/changelog.xml");

        then:
        "SQL in file com/example/changelog.xml executed" == change.getConfirmationMessage()
    }

    def replacementOfProperties() throws Exception {
        when:
        SQLFileAction change = new SQLFileAction();
        ChangeLogParameters changeLogParameters = new ChangeLogParametersImpl();
        changeLogParameters.set("table.prefix", "prfx");
        changeLogParameters.set("some.other.prop", "nofx");
        ChangeSet changeSet = new ChangeSetImpl("x", "y", true, true, null, null, null, null);
        changeSet.setChangeLogParameters(changeLogParameters);
        change.setChangeSet(changeSet);

        String fakeSql = "create \${table.prefix}_customer (\${some.other.prop} INTEGER NOT NULL, PRIMARY KEY (\${some.other.prop}));";

        change.setSql(fakeSql);

        then:
        assertEquals("create prfx_customer (nofx INTEGER NOT NULL, PRIMARY KEY (nofx));", change.getSql());
    }

    def "checkStatus"() {
        when:
        def database = new MockDatabase()
        def change = new RawSQLAction()

        then:
        assert change.checkStatus(database).status == ChangeStatus.Status.unknown
        assert change.checkStatus(database).message == "Cannot check raw sql status"
    }

    @Override
    protected boolean canUseStandardGenerateCheckSumTest() {
        return false;
    }

    def isValidForLoad(Change change) {
        return ((SQLFileAction) change).path != null;
    }

    def "openSqlStream throws exception if file does not exist"() {
        when:
        def change = new SQLFileChange()
        change.path = "non-existing.sql"
        change.resourceAccessor = new MockResourceAccessor()
        change.openSqlStream()

        then:
        def e = thrown(IOException)
        e.message == "File does not exist: 'non-existing.sql'"

    }

}
