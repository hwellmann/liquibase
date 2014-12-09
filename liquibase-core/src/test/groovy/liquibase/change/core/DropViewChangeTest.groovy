package liquibase.change.core

import liquibase.action.DropViewAction
import liquibase.change.ChangeStatus
import liquibase.change.StandardChangeTest
import liquibase.sdk.database.MockDatabase
import liquibase.snapshot.MockSnapshotGeneratorFactory
import liquibase.snapshot.SnapshotGeneratorFactory
import liquibase.structure.core.View

public class DropViewChangeTest  extends StandardChangeTest {

    def getConfirmationMessage() throws Exception {
        when:
        DropViewAction change = new DropViewAction();
        change.setViewName("VIEW_NAME");

        then:
        "View VIEW_NAME dropped" == change.getConfirmationMessage()
    }

    def "checkStatus"() {
        when:
        def database = new MockDatabase()
        def snapshotFactory = new MockSnapshotGeneratorFactory()
        SnapshotGeneratorFactory.instance = snapshotFactory

        def view = new View(null, null, "test_view")

        def change = new DropViewAction()
        change.viewName = view.name

        then: "view is not there yet"
        assert change.checkStatus(database).status == ChangeStatus.Status.complete

        when: "view exists"
        snapshotFactory.addObjects(view)
        then:
        assert change.checkStatus(database).status == ChangeStatus.Status.notApplied
    }
}
