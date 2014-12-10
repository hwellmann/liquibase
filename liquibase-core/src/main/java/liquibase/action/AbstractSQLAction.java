package liquibase.action;

import java.util.ArrayList;
import java.util.List;

import liquibase.change.BaseSQLChange;
import liquibase.change.ChangeStatus;
import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.exception.DatabaseException;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.RawSqlStatement;
import liquibase.util.StringUtils;

public abstract class AbstractSQLAction<T extends BaseSQLChange> extends AbstractAction<T> {

    public AbstractSQLAction(T change) {
        super(change);
    }

    @Override
    public boolean supports(Database database) {
        return true;
    }

    @Override
    public Warnings warn(Database database) {
        return new Warnings();
    }

    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors validationErrors = new ValidationErrors();
        if (StringUtils.trimToNull(change.getSql()) == null) {
            validationErrors.addError("'sql' is required");
        }
        return validationErrors;
    }

    @Override
    public String getConfirmationMessage() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Generates one or more SqlStatements depending on how the SQL should be parsed. If split
     * statements is set to true then the SQL is split and each command is made into a separate
     * SqlStatement.
     * <p>
     * </p>
     * If stripping comments is true then any comments are removed before the splitting is executed.
     * The set SQL is passed through the {@link java.sql.Connection#nativeSQL} method if a
     * connection is available.
     */
    @Override
    public SqlStatement[] generateStatements(Database database) {
        List<SqlStatement> returnStatements = new ArrayList<SqlStatement>();
        String sql = StringUtils.trimToNull(change.getSql());
        if (sql == null) {
            return new SqlStatement[0];
        }
        String processedSQL = normalizeLineEndings(sql);
        for (String statement : StringUtils.processMutliLineSQL(processedSQL, change.isStripComments(),
            change.isSplitStatements(), change.getEndDelimiter())) {
            if (database instanceof MSSQLDatabase) {
                statement = statement.replaceAll("\\n", "\r\n");
            }
            String escapedStatement = statement;
            try {
                if (database.getConnection() != null) {
                    escapedStatement = database.getConnection().nativeSQL(statement);
                }
            }
            catch (DatabaseException e) {
                escapedStatement = statement;
            }
            returnStatements.add(new RawSqlStatement(escapedStatement, change.getEndDelimiter()));
        }
        return returnStatements.toArray(new SqlStatement[returnStatements.size()]);
    }

    @Override
    public boolean generateStatementsVolatile(Database database) {
        return false;
    }

    @Override
    public boolean generateRollbackStatementsVolatile(Database database) {
        return false;
    }

    @Override
    public ChangeStatus checkStatus(Database database) {
        return new ChangeStatus().unknown("Cannot check raw sql status");
    }

    protected String normalizeLineEndings(String string) {
        return string.replace("\r", "");
    }

    public Boolean isSplitStatements() {
        return change.isSplitStatements();
    }

    public void setSplitStatements(Boolean splitStatements) {
        change.setSplitStatements(splitStatements);
    }

    public Boolean isStripComments() {
        return change.isStripComments();
    }

    public void setStripComments(Boolean stripComments) {
        change.setStripComments(stripComments);
    }
}
