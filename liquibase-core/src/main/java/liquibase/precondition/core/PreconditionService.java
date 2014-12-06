package liquibase.precondition.core;

import java.util.ArrayList;
import java.util.List;

import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.ChangeSetImpl;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.ExecutableChangeSet;
import liquibase.changelog.RanChangeSet;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseList;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.exception.CustomPreconditionErrorException;
import liquibase.exception.CustomPreconditionFailedException;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.PreconditionFailedException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.logging.LogFactory;
import liquibase.precondition.CustomPrecondition;
import liquibase.precondition.CustomPreconditionWrapper;
import liquibase.precondition.ErrorPrecondition;
import liquibase.precondition.FailedPrecondition;
import liquibase.precondition.Precondition;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.core.RawSqlStatement;
import liquibase.statement.core.TableRowCountStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.ForeignKey;
import liquibase.structure.core.Index;
import liquibase.structure.core.PrimaryKey;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Sequence;
import liquibase.structure.core.Table;
import liquibase.structure.core.View;
import liquibase.util.ObjectUtil;
import liquibase.util.StreamUtil;
import liquibase.util.StringUtils;

public class PreconditionService {

    private PreconditionContainer container;

    public PreconditionService(PreconditionContainer container) {
        this.container = container;
    }

    public void check(Precondition condition, Database database, DatabaseChangeLog changeLog,
        ChangeSet changeSet) throws PreconditionFailedException, PreconditionErrorException {
        String ranOn = String.valueOf(changeLog);
        if (changeSet != null) {
            ranOn = String.valueOf(changeSet);
        }

        Executor executor = ExecutorService.getInstance().getExecutor(database);
        try {
            // Three cases for preConditions onUpdateSQL:
            // 1. TEST: preConditions should be run, as in regular update mode
            // 2. FAIL: the preConditions should fail if there are any
            // 3. IGNORE: act as if preConditions don't exist
            boolean testPrecondition = false;
            if (executor.updatesDatabase()) {
                testPrecondition = true;
            }
            else {
                if (container.getOnSqlOutput().equals(PreconditionContainer.OnSqlOutputOption.TEST)) {
                    testPrecondition = true;
                }
                else if (container.getOnSqlOutput().equals(
                    PreconditionContainer.OnSqlOutputOption.FAIL)) {
                    throw new PreconditionFailedException(
                        "Unexpected precondition in updateSQL mode with onUpdateSQL value: "
                            + container.getOnSqlOutput(), changeLog, container);
                }
                else if (container.getOnSqlOutput().equals(
                    PreconditionContainer.OnSqlOutputOption.IGNORE)) {
                    testPrecondition = false;
                }
            }

            if (testPrecondition) {
                doCheck(container, database, changeLog, changeSet);
            }
        }
        catch (PreconditionFailedException e) {
            StringBuffer message = new StringBuffer();
            message.append("     ").append(e.getFailedPreconditions().size())
                .append(" preconditions failed").append(StreamUtil.getLineSeparator());
            for (FailedPrecondition invalid : e.getFailedPreconditions()) {
                message.append("     ").append(invalid.toString());
                message.append(StreamUtil.getLineSeparator());
            }

            if (container.getOnFailMessage() != null) {
                message = new StringBuffer(container.getOnFailMessage());
            }
            if (container.getOnFail().equals(PreconditionContainer.FailOption.WARN)) {
                LogFactory.getLogger().info(
                    "Executing: " + ranOn
                        + " despite precondition failure due to onFail='WARN':\n " + message);
            }
            else {
                if (container.getOnFailMessage() == null) {
                    throw e;
                }
                else {
                    throw new PreconditionFailedException(container.getOnFailMessage(), changeLog,
                        container);
                }
            }
        }
        catch (PreconditionErrorException e) {
            StringBuffer message = new StringBuffer();
            message.append("     ").append(e.getErrorPreconditions().size())
                .append(" preconditions failed").append(StreamUtil.getLineSeparator());
            for (ErrorPrecondition invalid : e.getErrorPreconditions()) {
                message.append("     ").append(invalid.toString());
                message.append(StreamUtil.getLineSeparator());
            }

            if (container.getOnError().equals(PreconditionContainer.ErrorOption.CONTINUE)) {
                LogFactory.getLogger().info(
                    "Continuing past: " + toString() + " despite precondition error:\n " + message);
                throw e;
            }
            else if (container.getOnError().equals(PreconditionContainer.ErrorOption.WARN)) {
                LogFactory.getLogger().warning(
                    "Continuing past: " + toString() + " despite precondition error:\n " + message);
            }
            else {
                if (container.getOnErrorMessage() == null) {
                    throw e;
                }
                else {
                    throw new PreconditionErrorException(container.getOnErrorMessage(),
                        e.getErrorPreconditions());
                }
            }
        }
    }

    private void doCheck(Precondition condition, Database database, DatabaseChangeLog changeLog,
        ChangeSet changeSet) throws PreconditionFailedException, PreconditionErrorException {
        if (condition instanceof AndPrecondition) {
            checkAnd((AndPrecondition) condition, database, changeLog, changeSet);
        }
        else if (condition instanceof ChangeLogPropertyDefinedPrecondition) {
            checkChangeLogPropertyDefined((ChangeLogPropertyDefinedPrecondition) condition,
                database, changeLog, changeSet);
        }
        else if (condition instanceof ChangeSetExecutedPrecondition) {
            checkChangeSetExecuted((ChangeSetExecutedPrecondition) condition, database, changeLog,
                changeSet);
        }
        else if (condition instanceof ColumnExistsPrecondition) {
            checkColumnExists((ColumnExistsPrecondition) condition, database, changeLog, changeSet);
        }
        else if (condition instanceof CustomPreconditionWrapper) {
            checkCustomPrecondition((CustomPreconditionWrapper) condition, database, changeLog, changeSet);
        }
        else if (condition instanceof DBMSPrecondition) {
            checkDbms((DBMSPrecondition) condition, database, changeLog, changeSet);
        }
        else if (condition instanceof ForeignKeyExistsPrecondition) {
            checkForeignKeyExists((ForeignKeyExistsPrecondition) condition, database, changeLog,
                changeSet);
        }
        else if (condition instanceof IndexExistsPrecondition) {
            checkIndexExists((IndexExistsPrecondition) condition, database, changeLog, changeSet);
        }
        else if (condition instanceof NotPrecondition) {
            checkNot((NotPrecondition) condition, database, changeLog, changeSet);
        }
        else if (condition instanceof ObjectQuotingStrategyPrecondition) {
            checkObjectQuotingStrategy((ObjectQuotingStrategyPrecondition) condition, database,
                changeLog, changeSet);
        }
        else if (condition instanceof OrPrecondition) {
            checkOr((OrPrecondition) condition, database, changeLog, changeSet);
        }
        else if (condition instanceof PrimaryKeyExistsPrecondition) {
            checkPrimaryKeyExists((PrimaryKeyExistsPrecondition) condition, database, changeLog,
                changeSet);
        }
        else if (condition instanceof RowCountPrecondition) {
            checkRowCount((RowCountPrecondition) condition, database, changeLog, changeSet);
        }
        else if (condition instanceof RunningAsPrecondition) {
            checkRunningAs((RunningAsPrecondition) condition, database, changeLog, changeSet);
        }
        else if (condition instanceof SequenceExistsPrecondition) {
            checkSequenceExists((SequenceExistsPrecondition) condition, database, changeLog,
                changeSet);
        }
        else if (condition instanceof SqlPrecondition) {
            checkSql((SqlPrecondition) condition, database, changeLog, changeSet);
        }
        else if (condition instanceof TableExistsPrecondition) {
            checkTableExists((TableExistsPrecondition) condition, database, changeLog, changeSet);
        }
        else if (condition instanceof TableIsEmptyPrecondition) {
            checkRowCount((RowCountPrecondition) condition, database, changeLog, changeSet);
        }
        else if (condition instanceof ViewExistsPrecondition) {
            checkViewExists((ViewExistsPrecondition) condition, database, changeLog, changeSet);
        }
        else {
            throw new IllegalArgumentException(condition.getClass().getName());
        }

    }

    private void checkCustomPrecondition(CustomPreconditionWrapper condition, Database database,
        DatabaseChangeLog changeLog, ChangeSet changeSet) throws PreconditionFailedException, PreconditionErrorException {
        CustomPrecondition customPrecondition;
        String className = condition.getClassName();
        ClassLoader classLoader = condition.getClassLoader();
        try {
//            System.out.println(classLoader.toString());
            try {
                customPrecondition = (CustomPrecondition) Class.forName(className, true, classLoader).newInstance();
            } catch (ClassCastException e) { //fails in Ant in particular
                customPrecondition = (CustomPrecondition) Class.forName(className).newInstance();
            }
        } catch (Exception e) {
            throw new PreconditionFailedException("Could not open custom precondition class "+className, changeLog, condition);
        }

        for (String param : condition.getParams()) {
            try {
                ObjectUtil.setProperty(customPrecondition, param, condition.getParamValues().get(param));
            } catch (Exception e) {
                throw new PreconditionFailedException("Error setting parameter "+param+" on custom precondition "+className, changeLog, condition);
            }
        }

        try {
            customPrecondition.check(database);
        } catch (CustomPreconditionFailedException e) {
            throw new PreconditionFailedException(new FailedPrecondition("Custom Precondition Failed: "+e.getMessage(), changeLog, condition));
        } catch (CustomPreconditionErrorException e) {
            throw new PreconditionErrorException(new ErrorPrecondition(e, changeLog, condition));
        }
    }

    private void checkViewExists(ViewExistsPrecondition condition, Database database,
        DatabaseChangeLog changeLog, ChangeSet changeSet) throws PreconditionErrorException,
        PreconditionFailedException {
        String currentSchemaName;
        String currentCatalogName;
        try {
            currentCatalogName = condition.getCatalogName();
            currentSchemaName = condition.getSchemaName();
            if (!SnapshotGeneratorFactory.getInstance().has(
                new View().setName(condition.getViewName()).setSchema(
                    new Schema(currentCatalogName, currentSchemaName)), database)) {
                throw new PreconditionFailedException("View "
                    + database.escapeTableName(currentCatalogName, currentSchemaName,
                        condition.getViewName()) + " does not exist", changeLog, condition);
            }
        }
        catch (PreconditionFailedException e) {
            throw e;
        }
        catch (Exception e) {
            throw new PreconditionErrorException(e, changeLog, condition);
        }
    }

    private void checkTableExists(TableExistsPrecondition condition, Database database,
        DatabaseChangeLog changeLog, ChangeSet changeSet) throws PreconditionErrorException,
        PreconditionFailedException {
        try {
            String correctedTableName = database.correctObjectName(condition.getTableName(),
                Table.class);
            if (!SnapshotGeneratorFactory.getInstance().has(
                new Table().setName(correctedTableName).setSchema(
                    new Schema(condition.getCatalogName(), condition.getSchemaName())), database)) {
                throw new PreconditionFailedException("Table "
                    + database.escapeTableName(condition.getCatalogName(),
                        condition.getSchemaName(), condition.getTableName()) + " does not exist",
                    changeLog, condition);
            }
        }
        catch (PreconditionFailedException e) {
            throw e;
        }
        catch (Exception e) {
            throw new PreconditionErrorException(e, changeLog, condition);
        }
    }

    private void checkSql(SqlPrecondition condition, Database database,
        DatabaseChangeLog changeLog, ChangeSet changeSet) throws PreconditionErrorException,
        PreconditionFailedException {
        DatabaseConnection connection = database.getConnection();
        try {
            String result = ExecutorService
                .getInstance()
                .getExecutor(database)
                .queryForObject(new RawSqlStatement(condition.getSql().replaceFirst(";$", "")),
                    String.class);
            if (result == null) {
                throw new PreconditionFailedException("No rows returned from SQL Precondition",
                    changeLog, condition);
            }

            String expectedResult = condition.getExpectedResult();
            if (!expectedResult.equals(result)) {
                throw new PreconditionFailedException("SQL Precondition failed.  Expected '"
                    + expectedResult + "' got '" + result + "'", changeLog, condition);
            }

        }
        catch (DatabaseException e) {
            throw new PreconditionErrorException(e, changeLog, condition);
        }
    }

    private void checkSequenceExists(SequenceExistsPrecondition condition, Database database,
        DatabaseChangeLog changeLog, ChangeSet changeSet) throws PreconditionFailedException,
        PreconditionErrorException {
        DatabaseSnapshot snapshot;
        String catalogName = condition.getCatalogName();
        String schemaName = condition.getSchemaName();
        Schema schema = new Schema(catalogName, schemaName);
        try {
            String sequenceName = condition.getSequenceName();
            if (!SnapshotGeneratorFactory.getInstance().has(
                new Sequence().setName(sequenceName).setSchema(schema), database)) {
                throw new PreconditionFailedException("Sequence "
                    + database.escapeSequenceName(catalogName, schemaName, sequenceName)
                    + " does not exist", changeLog, condition);
            }
        }
        catch (LiquibaseException e) {
            throw new PreconditionErrorException(e, changeLog, condition);
        }
    }

    private void checkRunningAs(RunningAsPrecondition condition, Database database,
        DatabaseChangeLog changeLog, ChangeSet changeSet) throws PreconditionFailedException {
        String loggedusername = database.getConnection().getConnectionUserName();
        if (loggedusername != null && loggedusername.indexOf('@') >= 0) {
            loggedusername = loggedusername.substring(0, loggedusername.indexOf('@'));
        }
        String username = condition.getUsername();
        if (!username.equalsIgnoreCase(loggedusername)) {
            throw new PreconditionFailedException("RunningAs Precondition failed: expected "
                + username + ", was " + loggedusername, changeLog, condition);
        }
    }

    private void checkRowCount(RowCountPrecondition condition, Database database,
        DatabaseChangeLog changeLog, ChangeSet changeSet) throws PreconditionErrorException,
        PreconditionFailedException {
        try {
            TableRowCountStatement statement = new TableRowCountStatement(
                condition.getCatalogName(), condition.getSchemaName(), condition.getTableName());

            int result = ExecutorService.getInstance().getExecutor(database).queryForInt(statement);
            if (result != condition.getExpectedRows()) {
                throw new PreconditionFailedException(condition.getFailureMessage(result),
                    changeLog, condition);
            }

        }
        catch (PreconditionFailedException e) {
            throw e;
        }
        catch (Exception e) {
            throw new PreconditionErrorException(e, changeLog, condition);
        }
    }

    private void checkPrimaryKeyExists(PrimaryKeyExistsPrecondition condition, Database database,
        DatabaseChangeLog changeLog, ChangeSet changeSet) throws PreconditionErrorException,
        PreconditionFailedException {
        try {
            PrimaryKey example = new PrimaryKey();
            Table table = new Table();
            table.setSchema(new Schema(condition.getCatalogName(), condition.getSchemaName()));
            String tableName = condition.getTableName();
            if (StringUtils.trimToNull(tableName) != null) {
                table.setName(tableName);
            }
            example.setTable(table);
            example.setName(condition.getPrimaryKeyName());

            if (!SnapshotGeneratorFactory.getInstance().has(example, database)) {
                if (tableName != null) {
                    throw new PreconditionFailedException("Primary Key does not exist on "
                        + database.escapeObjectName(tableName, Table.class), changeLog, condition);
                }
                else {
                    throw new PreconditionFailedException(
                        "Primary Key "
                            + database.escapeObjectName(condition.getPrimaryKeyName(),
                                PrimaryKey.class) + " does not exist", changeLog, condition);
                }
            }
        }
        catch (PreconditionFailedException e) {
            throw e;
        }
        catch (Exception e) {
            throw new PreconditionErrorException(e, changeLog, condition);
        }
    }

    private void checkObjectQuotingStrategy(ObjectQuotingStrategyPrecondition condition,
        Database database, DatabaseChangeLog changeLog, ChangeSet changeSet)
        throws PreconditionErrorException, PreconditionFailedException {
        try {
            ObjectQuotingStrategy strategy = condition.getStrategy();
            if (changeLog.getObjectQuotingStrategy() != strategy) {
                throw new PreconditionFailedException(
                    "Quoting strategy Precondition failed: expected " + strategy + ", got "
                        + changeSet.getObjectQuotingStrategy(), changeLog, condition);
            }
        }
        catch (PreconditionFailedException e) {
            throw e;
        }
        catch (Exception e) {
            throw new PreconditionErrorException(e, changeLog, condition);
        }
    }

    private void checkIndexExists(IndexExistsPrecondition condition, Database database,
        DatabaseChangeLog changeLog, ChangeSet changeSet) throws PreconditionErrorException,
        PreconditionFailedException {
        try {
            Schema schema = new Schema(condition.getCatalogName(), condition.getSchemaName());
            Index example = new Index();
            String tableName = StringUtils.trimToNull(condition.getTableName());
            if (tableName != null) {
                example.setTable((Table) new Table().setName(
                    database.correctObjectName(condition.getTableName(), Table.class)).setSchema(
                    schema));
            }
            example.setName(database.correctObjectName(condition.getIndexName(), Index.class));
            if (StringUtils.trimToNull(condition.getColumnNames()) != null) {
                for (String column : condition.getColumnNames().split("\\s*,\\s*")) {
                    example.addColumn(new Column(database.correctObjectName(column, Column.class)));
                }
            }
            if (!SnapshotGeneratorFactory.getInstance().has(example, database)) {
                String name = "";

                if (condition.getIndexName() != null) {
                    name += database.escapeObjectName(condition.getIndexName(), Index.class);
                }

                if (tableName != null) {
                    name += " on "
                        + database.escapeObjectName(condition.getTableName(), Table.class);

                    if (StringUtils.trimToNull(condition.getColumnNames()) != null) {
                        name += " columns " + condition.getColumnNames();
                    }
                }
                throw new PreconditionFailedException("Index " + name + " does not exist",
                    changeLog, condition);
            }
        }
        catch (Exception e) {
            if (e instanceof PreconditionFailedException) {
                throw (((PreconditionFailedException) e));
            }
            throw new PreconditionErrorException(e, changeLog, condition);
        }
    }

    private void checkForeignKeyExists(ForeignKeyExistsPrecondition condition, Database database,
        DatabaseChangeLog changeLog, ChangeSet changeSet) throws PreconditionFailedException,
        PreconditionErrorException {
        try {
            ForeignKey example = new ForeignKey();
            example.setName(condition.getForeignKeyName());
            example.setForeignKeyTable(new Table());
            if (StringUtils.trimToNull(condition.getForeignKeyTableName()) != null) {
                example.getForeignKeyTable().setName(condition.getForeignKeyTableName());
            }
            String catalogName = condition.getCatalogName();
            String schemaName = condition.getSchemaName();
            example.getForeignKeyTable().setSchema(new Schema(catalogName, schemaName));

            if (!SnapshotGeneratorFactory.getInstance().has(example, database)) {
                throw new PreconditionFailedException("Foreign Key "
                    + database.escapeIndexName(catalogName, schemaName,
                        condition.getForeignKeyName()) + " does not exist", changeLog, condition);
            }
        }
        catch (PreconditionFailedException e) {
            throw e;
        }
        catch (Exception e) {
            throw new PreconditionErrorException(e, changeLog, condition);
        }
    }

    private void checkDbms(DBMSPrecondition condition, Database database,
        DatabaseChangeLog changeLog, ChangeSet changeSet) throws PreconditionErrorException,
        PreconditionFailedException {
        try {
            String dbType = database.getShortName();
            String type = condition.getType();
            if (!DatabaseList.definitionMatches(type, database, false)) {
                throw new PreconditionFailedException("DBMS Precondition failed: expected " + type
                    + ", got " + dbType, changeLog, condition);
            }
        }
        catch (PreconditionFailedException e) {
            throw e;
        }
        catch (Exception e) {
            throw new PreconditionErrorException(e, changeLog, condition);
        }
    }

    private void checkColumnExists(ColumnExistsPrecondition condition, Database database,
        DatabaseChangeLog changeLog, ChangeSet changeSet) throws PreconditionErrorException,
        PreconditionFailedException {
        Column example = new Column();
        if (StringUtils.trimToNull(condition.getTableName()) != null) {
            example.setRelation(new Table().setName(
                database.correctObjectName(condition.getTableName(), Table.class)).setSchema(
                new Schema(condition.getCatalogName(), condition.getSchemaName())));
        }
        example.setName(database.correctObjectName(condition.getColumnName(), Column.class));

        try {
            if (!SnapshotGeneratorFactory.getInstance().has(example, database)) {
                throw new PreconditionFailedException("Column '"
                    + database.escapeColumnName(condition.getCatalogName(),
                        condition.getSchemaName(), condition.getTableName(),
                        condition.getColumnName()) + "' does not exist", changeLog, condition);
            }
        }
        catch (LiquibaseException e) {
            throw new PreconditionErrorException(e, changeLog, condition);
        }
    }

    private void checkChangeSetExecuted(ChangeSetExecutedPrecondition condition, Database database,
        DatabaseChangeLog changeLog, ChangeSet changeSet) throws PreconditionFailedException,
        PreconditionErrorException {
        ObjectQuotingStrategy objectQuotingStrategy = null;
        if (changeSet == null) {
            objectQuotingStrategy = ObjectQuotingStrategy.LEGACY;
        }
        else {
            objectQuotingStrategy = changeSet.getObjectQuotingStrategy();
        }
        ExecutableChangeSet interestedChangeSet = new ChangeSetImpl(condition.getId(),
            condition.getAuthor(), false, false, condition.getChangeLogFile(), null, null, false,
            objectQuotingStrategy, changeLog);
        RanChangeSet ranChangeSet;
        try {
            ranChangeSet = database.getRanChangeSet(interestedChangeSet);
        }
        catch (Exception e) {
            throw new PreconditionErrorException(e, changeLog, condition);
        }
        if (ranChangeSet == null || ranChangeSet.getExecType() == null
            || !ranChangeSet.getExecType().ran) {
            throw new PreconditionFailedException("Change Set '"
                + interestedChangeSet.toString(false) + "' has not been run", changeLog, condition);
        }
    }

    private void checkChangeLogPropertyDefined(ChangeLogPropertyDefinedPrecondition condition,
        Database database, DatabaseChangeLog changeLog, ChangeSet changeSet)
        throws PreconditionFailedException {
        String property = condition.getProperty();
        ChangeLogParameters changeLogParameters = changeLog.getChangeLogParameters();
        if (changeLogParameters == null) {
            throw new PreconditionFailedException("No Changelog properties were set", changeLog,
                condition);
        }
        Object propertyValue = changeLogParameters.getValue(property);
        if (propertyValue == null) {
            throw new PreconditionFailedException("Changelog property '" + property
                + "' was not set", changeLog, condition);
        }
        String value = condition.getValue();
        if (value != null && !propertyValue.toString().equals(value)) {
            throw new PreconditionFailedException("Expected changelog property '" + property
                + "' to have a value of '" + value + "'.  Got '" + propertyValue + "'", changeLog,
                condition);
        }
    }

    private void checkAnd(AndPrecondition condition, Database database,
        DatabaseChangeLog changeLog, ChangeSet changeSet) throws PreconditionFailedException,
        PreconditionErrorException {
        boolean allPassed = true;
        List<FailedPrecondition> failures = new ArrayList<FailedPrecondition>();
        for (Precondition precondition : condition.getNestedPreconditions()) {
            try {
                doCheck(precondition, database, changeLog, changeSet);
            }
            catch (PreconditionFailedException e) {
                failures.addAll(e.getFailedPreconditions());
                allPassed = false;
                break;
            }
        }
        if (!allPassed) {
            throw new PreconditionFailedException(failures);
        }
    }

    private void checkNot(NotPrecondition condition, Database database,
        DatabaseChangeLog changeLog, ChangeSet changeSet) throws PreconditionFailedException,
        PreconditionErrorException {
        for (Precondition precondition : condition.getNestedPreconditions()) {
            boolean threwException = false;
            try {
                doCheck(precondition, database, changeLog, changeSet);
            }
            catch (PreconditionFailedException e) {
                ; // that's what we want with a Not precondition
                threwException = true;
            }

            if (!threwException) {
                throw new PreconditionFailedException("Not precondition failed", changeLog,
                    condition);
            }
        }
    }

    private void checkOr(OrPrecondition condition, Database database, DatabaseChangeLog changeLog,
        ChangeSet changeSet) throws PreconditionErrorException, PreconditionFailedException {
        boolean onePassed = false;
        List<FailedPrecondition> failures = new ArrayList<FailedPrecondition>();
        for (Precondition precondition : condition.getNestedPreconditions()) {
            try {
                doCheck(precondition, database, changeLog, changeSet);
                onePassed = true;
                break;
            }
            catch (PreconditionFailedException e) {
                failures.addAll(e.getFailedPreconditions());
            }
        }
        if (!onePassed) {
            throw new PreconditionFailedException(failures);
        }
    }

}
