package liquibase.changelog;

import java.util.List;

import liquibase.changelog.visitor.ChangeExecListener;
import liquibase.database.Database;
import liquibase.exception.MigrationFailedException;
import liquibase.exception.RollbackFailedException;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.ResourceAccessor;
import liquibase.serializer.LiquibaseSerializable;
import liquibase.sql.visitor.SqlVisitor;

public interface ExecutableChangeSet extends LiquibaseSerializable, ChangeSet {

    public enum RunStatus {
        NOT_RAN, ALREADY_RAN, RUN_AGAIN, MARK_RAN, INVALID_MD5SUM
    }

    @Override
    void load(ParsedNode node, ResourceAccessor resourceAccessor) throws ParsedNodeException;

    ExecType execute(DatabaseChangeLog databaseChangeLog, Database database)
        throws MigrationFailedException;

    /**
     * This method will actually execute each of the changes in the list against the
     * specified database.
     *
     * @return should change set be marked as ran
     */
    ExecType execute(DatabaseChangeLog databaseChangeLog, ChangeExecListener listener,
        Database database) throws MigrationFailedException;

    void rollback(Database database) throws RollbackFailedException;

    String toString(boolean includeMD5Sum);

    void addSqlVisitor(SqlVisitor sqlVisitor);

    List<SqlVisitor> getSqlVisitors();

    boolean supportsRollback(Database database);

}
