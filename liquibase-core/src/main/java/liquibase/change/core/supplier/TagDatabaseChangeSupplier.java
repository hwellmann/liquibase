package liquibase.change.core.supplier;

import static liquibase.Assert.assertTrue;
import liquibase.change.AbstractChange;
import liquibase.change.IChange;
import liquibase.change.core.TagDatabaseChange;
import liquibase.changelog.ExecutableChangeSet;
import liquibase.changelog.ChangeSetImpl;
import liquibase.changelog.DatabaseChangeLogImpl;
import liquibase.database.Database;
import liquibase.diff.DiffResult;
import liquibase.executor.ExecutorService;
import liquibase.sdk.supplier.change.AbstractChangeSupplier;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.CreateDatabaseChangeLogTableStatement;
import liquibase.statement.core.MarkChangeSetRanStatement;
import liquibase.statement.core.RawSqlStatement;

public class TagDatabaseChangeSupplier extends AbstractChangeSupplier<TagDatabaseChange> {

    public TagDatabaseChangeSupplier() {
        super(TagDatabaseChange.class);
    }

    @Override
    public IChange[] prepareDatabase(TagDatabaseChange change) throws Exception {
        return new IChange[]{new AbstractChange() {
            @Override
            public String getConfirmationMessage() {
                return "Custom change";
            }

            @Override
            public SqlStatement[] generateStatements(Database database) {
                return new SqlStatement[]{
                        new CreateDatabaseChangeLogTableStatement(),
                        new MarkChangeSetRanStatement(new ChangeSetImpl("1", "test", false, false, "com/example/test.xml", null, null, new DatabaseChangeLogImpl("com/example/test.xml")), ExecutableChangeSet.ExecType.EXECUTED),
                        new MarkChangeSetRanStatement(new ChangeSetImpl("2", "test", false, false, "com/example/test.xml", null, null, new DatabaseChangeLogImpl("com/example/test.xml")), ExecutableChangeSet.ExecType.EXECUTED),
                        new MarkChangeSetRanStatement(new ChangeSetImpl("3", "test", false, false, "com/example/test.xml", null, null, new DatabaseChangeLogImpl("com/example/test.xml")), ExecutableChangeSet.ExecType.EXECUTED),
                        new MarkChangeSetRanStatement(new ChangeSetImpl("4", "test", false, false, "com/example/test.xml", null, null, new DatabaseChangeLogImpl("com/example/test.xml")), ExecutableChangeSet.ExecType.EXECUTED)
                };

            }
        }
        };
    }

    @Override
    public void checkDiffResult(DiffResult diffResult, TagDatabaseChange change) throws Exception {
        Database database = diffResult.getComparisonSnapshot().getDatabase();
        int rows = ExecutorService.getInstance().getExecutor(database).queryForInt(new RawSqlStatement("select count(*) from " + database.getDatabaseChangeLogTableName() + " where tag='" + change.getTag() + "'"));
        assertTrue(rows > 0);

    }
}
