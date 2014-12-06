package liquibase.changelog.visitor;

import liquibase.change.ColumnConfig;
import liquibase.change.core.CreateTableChange;
import liquibase.changelog.ChangeSetImpl;
import liquibase.changelog.RanChangeSet;
import liquibase.changelog.DatabaseChangeLogImpl;
import liquibase.sdk.database.MockDatabase;
import liquibase.exception.SetupException;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import liquibase.database.Database;
import liquibase.exception.ValidationErrors;

public class ValidatingVisitorTest {

    private ChangeSetImpl changeSet1;
    private ChangeSetImpl changeSet2;

    @Before
    public void setup() {
        changeSet1 = new ChangeSetImpl("1", "testAuthor", false, false, "path/changelog", null, null, null);
        changeSet2 = new ChangeSetImpl("2", "testAuthor", false, false, "path/changelog", null, null, null);
    }


    @Test
    public void visit_successful() throws Exception {
        CreateTableChange change1 = new CreateTableChange();
        change1.setTableName("table1");
        ColumnConfig column1 = new ColumnConfig();
        change1.addColumn(column1);
        column1.setName("col1");
        column1.setType("int");

        CreateTableChange change2 = new CreateTableChange();
        change2.setTableName("table2");
        ColumnConfig column2 = new ColumnConfig();
        change2.addColumn(column2);
        column2.setName("col2");
        column2.setType("int");

        changeSet1.addChange(change1);
        changeSet2.addChange(change2);

        ValidatingVisitor handler = new ValidatingVisitor(new ArrayList<RanChangeSet>());
        handler.visit(changeSet1, new DatabaseChangeLogImpl(), new MockDatabase(), null);
        handler.visit(changeSet2, new DatabaseChangeLogImpl(), new MockDatabase(), null);

        assertTrue(handler.validationPassed());

    }

    @Test
    public void visit_setupException() throws Exception {
        changeSet1.addChange(new CreateTableChange() {
            @Override
            public void finishInitialization() throws SetupException {
                throw new SetupException("Test message");
            }
        });

        ValidatingVisitor handler = new ValidatingVisitor(new ArrayList<RanChangeSet>());
        handler.visit(changeSet1, new DatabaseChangeLogImpl(), null, null);

        assertEquals(1, handler.getSetupExceptions().size());
        assertEquals("Test message", handler.getSetupExceptions().get(0).getMessage());

        assertFalse(handler.validationPassed());
    }

    @Test
    public void visit_duplicate() throws Exception {

        ValidatingVisitor handler = new ValidatingVisitor(new ArrayList<RanChangeSet>());
        handler.visit(changeSet1, new DatabaseChangeLogImpl(), null, null);
        handler.visit(changeSet1, new DatabaseChangeLogImpl(), null, null);

        assertEquals(1, handler.getDuplicateChangeSets().size());

        assertFalse(handler.validationPassed());
    }

    @Test
    public void visit_validateError() throws Exception {

        changeSet1.addChange(new CreateTableChange() {
            @Override
            public ValidationErrors validate(Database database) {
                ValidationErrors changeValidationErrors = new ValidationErrors();
                changeValidationErrors.addError("Test message");
                return changeValidationErrors;
            }
        });

        List<RanChangeSet> ran = new ArrayList<RanChangeSet>();
        ValidatingVisitor handler = new ValidatingVisitor(ran);
        handler.visit(changeSet1, new DatabaseChangeLogImpl(), null, null);

        assertEquals(1, handler.getValidationErrors().getErrorMessages().size());
        assertTrue(handler.getValidationErrors().getErrorMessages().get(0).startsWith("Test message"));

        assertFalse(handler.validationPassed());
    }

    @Test
    public void visit_torunOnly() throws Exception {

        changeSet1.addChange(new CreateTableChange() {
            @Override
            public ValidationErrors validate(Database database) {
                ValidationErrors changeValidationErrors = new ValidationErrors();
                changeValidationErrors.addError("Test message");
                return changeValidationErrors;
            }
        });

        List<RanChangeSet> ran = new ArrayList<RanChangeSet>();
        ran.add(new RanChangeSet(changeSet1));
        ValidatingVisitor handler = new ValidatingVisitor(ran);
        handler.visit(changeSet1, new DatabaseChangeLogImpl(), null, null);

        assertEquals(0, handler.getSetupExceptions().size());

        assertTrue(handler.validationPassed());
    }
}
