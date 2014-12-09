package liquibase.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.ExecutableChange;
import liquibase.change.core.DropAllForeignKeyConstraintsChange;
import liquibase.database.Database;
import liquibase.diff.compare.DatabaseObjectComparatorFactory;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.FindForeignKeyConstraintsStatement;
import liquibase.structure.core.Table;

import org.kohsuke.MetaInfServices;

@DatabaseChange(name="dropAllForeignKeyConstraints", description = "Drops all foreign key constraints for a table", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "table")
@MetaInfServices(ExecutableChange.class)
public class DropAllForeignKeyConstraintsAction extends AbstractAction<DropAllForeignKeyConstraintsChange> {

    public DropAllForeignKeyConstraintsAction() {
        super(new DropAllForeignKeyConstraintsChange());
    }

    public DropAllForeignKeyConstraintsAction(DropAllForeignKeyConstraintsChange change) {
        super(change);
    }

    public String getBaseTableCatalogName() {
        return change.getBaseTableCatalogName();
    }

    public void setBaseTableCatalogName(String baseTableCatalogName) {
        change.setBaseTableCatalogName(baseTableCatalogName);
    }

    public String getBaseTableSchemaName() {
        return change.getBaseTableSchemaName();
    }

    public void setBaseTableSchemaName(String baseTableSchemaName) {
        change.setBaseTableSchemaName(baseTableSchemaName);
    }

    public String getBaseTableName() {
        return change.getBaseTableName();
    }

    public void setBaseTableName(String baseTableName) {
        change.setBaseTableName(baseTableName);
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        List<SqlStatement> sqlStatements = new ArrayList<SqlStatement>();

        List<DropForeignKeyConstraintAction> childDropChanges = generateChildren(database);

        if (childDropChanges != null) {
            for (DropForeignKeyConstraintAction change : childDropChanges) {
                sqlStatements.addAll(Arrays.asList(change.generateStatements(database)));
            }
        }

        return sqlStatements.toArray(new SqlStatement[sqlStatements.size()]);
    }

    @Override
    public String getConfirmationMessage() {
        return "Foreign keys on base table " + getBaseTableName() + " dropped";
    }

    private List<DropForeignKeyConstraintAction> generateChildren(Database database) {
        // Make a new list
        List<DropForeignKeyConstraintAction> childDropChanges = new ArrayList<DropForeignKeyConstraintAction>();

        Executor executor = ExecutorService.getInstance().getExecutor(database);

        FindForeignKeyConstraintsStatement sql = new FindForeignKeyConstraintsStatement(getBaseTableCatalogName(), getBaseTableSchemaName(), getBaseTableName());

        try {
            List<Map<String, ?>> results = executor.queryForList(sql);
            Set<String> handledConstraints = new HashSet<String>();

            if (results != null && results.size() > 0) {
                for (Map result : results) {
                    String baseTableName =
                            (String) result.get(FindForeignKeyConstraintsStatement.RESULT_COLUMN_BASE_TABLE_NAME);
                    String constraintName =
                            (String) result.get(FindForeignKeyConstraintsStatement.RESULT_COLUMN_CONSTRAINT_NAME);
                    if (DatabaseObjectComparatorFactory.getInstance().isSameObject(new Table().setName(getBaseTableName()), new Table().setName(baseTableName), database)) {
                        if( !handledConstraints.contains(constraintName)) {
                            DropForeignKeyConstraintAction dropForeignKeyConstraintChange =
                                    new DropForeignKeyConstraintAction();

                            dropForeignKeyConstraintChange.setBaseTableSchemaName(getBaseTableSchemaName());
                            dropForeignKeyConstraintChange.setBaseTableName(baseTableName);
                            dropForeignKeyConstraintChange.setConstraintName(constraintName);

                            childDropChanges.add(dropForeignKeyConstraintChange);
                            handledConstraints.add(constraintName);
                        }
                    } else {
                        throw new IllegalStateException("Expected to return only foreign keys for base table name: " +
                                getBaseTableName() + " and got results for table: " + baseTableName);
                    }
                }
            }

            return childDropChanges;

        } catch (DatabaseException e) {
            throw new UnexpectedLiquibaseException("Failed to find foreign keys for table: " + getBaseTableName(), e);
        }
    }

    @Override
    public boolean generateStatementsVolatile(Database database) {
        return true;
    }
}
