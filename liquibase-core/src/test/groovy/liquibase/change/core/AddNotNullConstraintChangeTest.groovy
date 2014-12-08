package liquibase.change.core;

import static org.junit.Assert.*
import liquibase.action.AddNotNullConstraintAction
import liquibase.change.StandardChangeTest

public class AddNotNullConstraintChangeTest extends StandardChangeTest {

    def getConfirmationMessage() throws Exception {
        when:
        def change = new AddNotNullConstraintAction();
        change.setTableName("TABLE_NAME");
        change.setColumnName("COL_HERE");

        then:
        change.getConfirmationMessage() == "Null constraint has been added to TABLE_NAME.COL_HERE"
    }
}