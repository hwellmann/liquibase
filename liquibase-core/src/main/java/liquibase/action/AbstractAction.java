package liquibase.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import liquibase.change.Change;
import liquibase.change.ChangeFactory;
import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeParameterMetaData;
import liquibase.change.ChangeParameterService;
import liquibase.change.ChangeStatus;
import liquibase.change.CheckSum;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.change.ExecutableChange;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.ExecutableChangeSet;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.exception.RollbackImpossibleException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.ResourceAccessor;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.structure.DatabaseObject;

/**
 * Standard superclass to simplify {@link ExecutableChange } implementations. You can implement Change directly, this class is purely for convenience but recommended.
 * <p></p>
 * By default, this base class relies on annotations such as {@link DatabaseChange} and {@link DatabaseChangeProperty}
 * and delegating logic to the {@link liquibase.sqlgenerator.SqlGenerator} objects created to do the actual change work.
 * Place the @DatabaseChangeProperty annotations on the read "get" methods to control property metadata.
 */
public abstract class AbstractAction<T extends Change> implements Action<T>, ExecutableChange {

    private ExecutableChangeSet changeSet;
    
    protected T change;

    public AbstractAction(T change) {
        this.change = change;
    }
    
    
    public T getChange() {
        return change;
    }
    
    
    public void setChange(T change) {
        this.change = change;
    }


    /**
     * Implementation delegates logic to the {@link liquibase.sqlgenerator.SqlGenerator#generateStatementsIsVolatile(Database) } method on the {@link SqlStatement} objects returned by {@link #generateStatements }.
     * If zero or null SqlStatements are returned by generateStatements then this method returns false.
     */
    @Override
    public boolean generateStatementsVolatile(Database database) {
        SqlStatement[] statements = generateStatements(database);
        if (statements == null) {
            return false;
        }
        for (SqlStatement statement : statements) {
            if (SqlGeneratorFactory.getInstance().generateStatementsVolatile(statement, database)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Implementation delegates logic to the {@link liquibase.sqlgenerator.SqlGenerator#generateRollbackStatementsIsVolatile(Database) } method on the {@link SqlStatement} objects returned by {@link #generateStatements }
     * If no or null SqlStatements are returned by generateRollbackStatements then this method returns false.
     */
    @Override
    public boolean generateRollbackStatementsVolatile(Database database) {
        if (generateStatementsVolatile(database)) {
            return true;
        }
        SqlStatement[] statements = generateStatements(database);
        if (statements == null) {
            return false;
        }
        for (SqlStatement statement : statements) {
            if (SqlGeneratorFactory.getInstance().generateRollbackStatementsVolatile(statement, database)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Implementation delegates logic to the {@link liquibase.sqlgenerator.SqlGenerator#supports(liquibase.statement.SqlStatement, liquibase.database.Database)} method on the {@link SqlStatement} objects returned by {@link #generateStatements }.
     * If no or null SqlStatements are returned by generateStatements then this method returns true.
     * If {@link #generateStatementsVolatile(liquibase.database.Database)} returns true, we cannot call generateStatements and so assume true.
     */
    @Override
    public boolean supports(Database database) {
        if (generateStatementsVolatile(database)) {
            return true;
        }
        SqlStatement[] statements = generateStatements(database);
        if (statements == null) {
            return true;
        }
        for (SqlStatement statement : statements) {
            if (!SqlGeneratorFactory.getInstance().supports(statement, database)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Implementation delegates logic to the {@link liquibase.sqlgenerator.SqlGenerator#warn(liquibase.statement.SqlStatement, liquibase.database.Database, liquibase.sqlgenerator.SqlGeneratorChain)} method on the {@link SqlStatement} objects returned by {@link #generateStatements }.
     * If a generated statement is not supported for the given database, no warning will be added since that is a validation error.
     * If no or null SqlStatements are returned by generateStatements then this method returns no warnings.
     */
    @Override
    public Warnings warn(Database database) {
        Warnings warnings = new Warnings();
        if (generateStatementsVolatile(database)) {
            return warnings;
        }

        SqlStatement[] statements = generateStatements(database);
        if (statements == null) {
            return warnings;
        }
        for (SqlStatement statement : statements) {
            if (SqlGeneratorFactory.getInstance().supports(statement, database)) {
                warnings.addAll(SqlGeneratorFactory.getInstance().warn(statement, database));
            } else if (statement.skipOnUnsupported()) {
                ChangeMetaData changeMetaData = ChangeFactory.getInstance().getChangeMetaData(change);
                warnings.addWarning(statement.getClass().getName() + " is not supported on " + database.getShortName() + ", but " + changeMetaData.getName() + " will still execute");
            }
        }

        return warnings;
    }

    /**
     * Implementation checks the ChangeParameterMetaData for declared required fields
     * and also delegates logic to the {@link liquibase.sqlgenerator.SqlGenerator#validate(liquibase.statement.SqlStatement, liquibase.database.Database, liquibase.sqlgenerator.SqlGeneratorChain)}  method on the {@link SqlStatement} objects returned by {@link #generateStatements }.
     * If no or null SqlStatements are returned by generateStatements then this method returns no errors.
     * If there are no parameters than this method returns no errors
     */
    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors changeValidationErrors = new ValidationErrors();

        ChangeMetaData changeMetaData = ChangeFactory.getInstance().getChangeMetaData(change);
        for (ChangeParameterMetaData param : changeMetaData.getParameters().values()) {
            ChangeParameterService analyzer = new ChangeParameterService(param);
            if (analyzer.isRequiredFor(database) && param.getCurrentValue(change) == null) {
                changeValidationErrors.addError(param.getParameterName() + " is required for " + changeMetaData.getName() + " on " + database.getShortName());
            }
        }
        if (changeValidationErrors.hasErrors()) {
            return changeValidationErrors;
        }

        String unsupportedWarning = changeMetaData.getName() + " is not supported on " + database.getShortName();
        if (!this.supports(database)) {
            changeValidationErrors.addError(unsupportedWarning);
        } else if (!generateStatementsVolatile(database)) {
            boolean sawUnsupportedError = false;
            SqlStatement[] statements;
            statements = generateStatements(database);
            if (statements != null) {
                for (SqlStatement statement : statements) {
                    boolean supported = SqlGeneratorFactory.getInstance().supports(statement, database);
                    if (!supported && !sawUnsupportedError) {
                        if (!statement.skipOnUnsupported()) {
                            changeValidationErrors.addError(unsupportedWarning);
                            sawUnsupportedError = true;
                        }
                    } else {
                        changeValidationErrors.addAll(SqlGeneratorFactory.getInstance().validate(statement, database));
                    }
                }
            }
        }

        return changeValidationErrors;
    }

    @Override
    public ChangeStatus checkStatus(Database database) {
        return new ChangeStatus().unknown("Not implemented");
    }

    /**
     * Implementation relies on value returned from {@link #createInverses()}.
     */
    @Override
    public SqlStatement[] generateRollbackStatements(Database database) throws RollbackImpossibleException {
        return generateRollbackStatementsFromInverse(database);
    }

    /**
     * Implementation returns true if {@link #createInverses()} returns a non-null value.
     */
    @Override
    public boolean supportsRollback(Database database) {
        return createInverses() != null;
    }

    /*
     * Generates rollback statements from the inverse changes returned by createInverses().
     * Throws RollbackImpossibleException if the changes created by createInverses() is not supported for the passed database.
     *
     */
    private SqlStatement[] generateRollbackStatementsFromInverse(Database database) throws RollbackImpossibleException {
        ExecutableChange[] inverses = createInverses();
        if (inverses == null) {
            throw new RollbackImpossibleException("No inverse to " + getClass().getName() + " created");
        }

        List<SqlStatement> statements = new ArrayList<SqlStatement>();

        try {
            for (ExecutableChange inverse : inverses) {
                if (!inverse.supports(database)) {
                    throw new RollbackImpossibleException(ChangeFactory.getInstance().getChangeMetaData(inverse).getName() + " is not supported on " + database.getShortName());
                }
                statements.addAll(Arrays.asList(inverse.generateStatements(database)));
            }
        } catch (LiquibaseException e) {
            throw new RollbackImpossibleException(e);
        }

        return statements.toArray(new SqlStatement[statements.size()]);
    }

    /**
     * Create inverse changes that can roll back this change. This method is intended
     * to be overriden by Change implementations that have a logical inverse operation. Default implementation returns null.
     * <p/>
     * If {@link #generateRollbackStatements(liquibase.database.Database)} is overridden, this method may not be called.
     *
     * @return Return null if there is no corresponding inverse and therefore automatic rollback is not possible. Return an empty array to have a no-op rollback.
     * @also #generateRollbackStatements #supportsRollback
     */
    protected ExecutableChange[] createInverses() {
        return null;
    }

    /**
     * Implementation delegates logic to the {@link liquibase.sqlgenerator.SqlGeneratorFactory#getAffectedDatabaseObjects(liquibase.statement.SqlStatement, liquibase.database.Database)}  method on the {@link SqlStatement} objects returned by {@link #generateStatements }
     * Returns empty set if change is not supported for the passed database
     */
    @Override
    public Set<DatabaseObject> getAffectedDatabaseObjects(Database database) {
        if (this.generateStatementsVolatile(database)) {
            return new HashSet<DatabaseObject>();
        }
        Set<DatabaseObject> affectedObjects = new HashSet<DatabaseObject>();
        SqlStatement[] statements = generateStatements(database);

        if (statements != null) {
            for (SqlStatement statement : statements) {
                affectedObjects.addAll(SqlGeneratorFactory.getInstance().getAffectedDatabaseObjects(statement, database));
            }
        }

        return affectedObjects;
    }


    @Override
    public void finishInitialization() throws SetupException {
        change.finishInitialization();
    }


    @Override
    public ChangeMetaData createChangeMetaData() {
        return change.createChangeMetaData();
    }


    @Override
    public ChangeSet getChangeSet() {
        return change.getChangeSet();
    }


    @Override
    public void setChangeSet(ChangeSet changeSet) {
        change.setChangeSet(changeSet);
    }


    @Override
    public void setResourceAccessor(ResourceAccessor resourceAccessor) {
        change.setResourceAccessor(resourceAccessor);
    }


    @Override
    public CheckSum generateCheckSum() {
        return change.generateCheckSum();
    }


    @Override
    public String getSerializedObjectName() {
        return change.getSerializedObjectName();
    }


    @Override
    public Set<String> getSerializableFields() {
        return change.getSerializableFields();
    }


    @Override
    public Object getSerializableFieldValue(String field) {
        return change.getSerializableFieldValue(field);
    }


    @Override
    public SerializationType getSerializableFieldType(String field) {
        return change.getSerializableFieldType(field);
    }


    @Override
    public String getSerializableFieldNamespace(String field) {
        return change.getSerializableFieldNamespace(field);
    }


    @Override
    public String getSerializedObjectNamespace() {
        return change.getSerializedObjectNamespace();
    }


    @Override
    public void load(ParsedNode parsedNode, ResourceAccessor resourceAccessor)
        throws ParsedNodeException {
        change.load(parsedNode, resourceAccessor);
    }


    @Override
    public ParsedNode serialize() throws ParsedNodeException {
        return change.serialize();
    }
    
    

}
