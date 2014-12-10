package liquibase.sql.visitor;

import java.util.Set;

import liquibase.ContextExpression;
import liquibase.Labels;
import liquibase.change.CheckSum;
import liquibase.serializer.LiquibaseSerializable;

public interface SqlVisitor extends LiquibaseSerializable {

    String modifySql(String sql);

    String getName();

    Set<String> getApplicableDbms();

    void setApplicableDbms(Set<String> modifySqlDbmsList);

    void setApplyToRollback(boolean applyOnRollback);

    boolean isApplyToRollback();

    ContextExpression getContexts();

    void setContexts(ContextExpression contexts);

    Labels getLabels();
    void setLabels(Labels labels);

    CheckSum generateCheckSum();

}
