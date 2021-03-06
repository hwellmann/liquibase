package liquibase.changelog;

import java.util.List;
import java.util.Set;

import liquibase.ContextExpression;
import liquibase.Labels;
import liquibase.change.Change;
import liquibase.change.CheckSum;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.parser.core.ParsedNode;
import liquibase.precondition.core.PreconditionContainer;
import liquibase.serializer.LiquibaseSerializable;
import liquibase.sql.visitor.SqlVisitor;

public interface ChangeSet extends LiquibaseSerializable {

    public enum ExecType {
        EXECUTED("EXECUTED", false, true),
        FAILED("FAILED", false, false),
        SKIPPED("SKIPPED", false, false),
        RERAN("RERAN", true, true),
        MARK_RAN("MARK_RAN", false, true);

        ExecType(String value, boolean ranBefore, boolean ran) {
            this.value = value;
            this.ranBefore = ranBefore;
            this.ran = ran;
        }

        public final String value;
        public final boolean ranBefore;
        public final boolean ran;
    }

    public enum ValidationFailOption {
        HALT("HALT"),
        MARK_RAN("MARK_RAN");

        String key;

        ValidationFailOption(String key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return key;
        }
    }

    boolean shouldAlwaysRun();

    boolean shouldRunOnChange();

    String getFilePath();

    CheckSum generateCheckSum();

    @Override
    ParsedNode serialize();

    /**
     * Returns an unmodifiable list of changes.  To add one, use the addRefactoing method.
     */
    List<Change> getChanges();

    void addChange(Change change);

    String getId();

    String getAuthor();

    ContextExpression getContexts();

    Labels getLabels();

    void setLabels(Labels labels);

    Set<String> getDbmsSet();

    DatabaseChangeLog getChangeLog();

    String getComments();

    void setComments(String comments);

    boolean isAlwaysRun();

    boolean isRunOnChange();

    boolean isRunInTransaction();

    Change[] getRollBackChanges();

    void addRollBackSQL(String sql);

    void addRollbackChange(Change change);

    String getDescription();

    Boolean getFailOnError();

    void setFailOnError(Boolean failOnError);

    ValidationFailOption getOnValidationFail();

    void setOnValidationFail(ValidationFailOption onValidationFail);

    void setValidationFailed(boolean validationFailed);

    void addValidCheckSum(String text);

    Set<CheckSum> getValidCheckSums();

    boolean isCheckSumValid(CheckSum storedCheckSum);

    PreconditionContainer getPreconditions();

    void setPreconditions(PreconditionContainer preconditionContainer);

    ChangeLogParameters getChangeLogParameters();

    /**
     * Called by the changelog parsing process to pass the {@link ChangeLogParametersImpl}.
     */
    void setChangeLogParameters(ChangeLogParameters changeLogParameters);

    /**
     * Called to update file path from database entry when rolling back and ignoreClasspathPrefix is true.
     */
    void setFilePath(String filePath);

    ObjectQuotingStrategy getObjectQuotingStrategy();

    @Override
    String getSerializedObjectName();

    @Override
    Set<String> getSerializableFields();

    @Override
    Object getSerializableFieldValue(String field);

    @Override
    SerializationType getSerializableFieldType(String field);

    @Override
    String getSerializedObjectNamespace();

    @Override
    String getSerializableFieldNamespace(String field);

    public String toString(boolean includeMD5Sum);

    void addSqlVisitor(SqlVisitor sqlVisitor);

    List<SqlVisitor> getSqlVisitors();


}
