package liquibase.change.core

import liquibase.action.InsertDataAction
import liquibase.change.ChangeStatus
import liquibase.change.ColumnConfig
import liquibase.change.StandardChangeTest
import liquibase.sdk.database.MockDatabase
import liquibase.snapshot.MockSnapshotGeneratorFactory
import liquibase.snapshot.SnapshotGeneratorFactory

public class InsertDataChangeTest extends StandardChangeTest {

    def getConfirmationMessage() throws Exception {
        when:
        def change = new InsertDataAction();
        change.setTableName("TABLE_NAME");

        ColumnConfig col1 = new ColumnConfig();
        col1.setName("id");
        col1.setValueNumeric("123");

        ColumnConfig col2 = new ColumnConfig();
        col2.setName("name");
        col2.setValue("Andrew");

        ColumnConfig col3 = new ColumnConfig();
        col3.setName("age");
        col3.setValueNumeric("21");

        ColumnConfig col4 = new ColumnConfig();
        col4.setName("height");
        col4.setValueNumeric("1.78");

        change.addColumn(col1);
        change.addColumn(col2);
        change.addColumn(col3);
        change.addColumn(col4);

        then:
        "New row inserted into TABLE_NAME" == change.getConfirmationMessage()
    }

    @Override
    protected String getExpectedChangeName() {
        return "insert"
    }

    def "checkStatus"() {
        when:
        def database = new MockDatabase()
        def snapshotFactory = new MockSnapshotGeneratorFactory()
        SnapshotGeneratorFactory.instance = snapshotFactory

        def change = new InsertDataAction()

        then:
        assert change.checkStatus(database).status == ChangeStatus.Status.unknown
        assert change.checkStatus(database).message == "Cannot check insertData status"
    }
}
