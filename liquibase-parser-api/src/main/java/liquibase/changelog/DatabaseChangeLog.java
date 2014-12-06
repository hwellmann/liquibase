package liquibase.changelog;

import java.util.List;

import liquibase.database.ObjectQuotingStrategy;
import liquibase.precondition.core.PreconditionContainer;

public interface DatabaseChangeLog extends Comparable<DatabaseChangeLog> {

    PreconditionContainer getPreconditions();

    void setPreconditions(PreconditionContainer precond);

    ChangeLogParameters getChangeLogParameters();

    void setChangeLogParameters(ChangeLogParameters changeLogParameters);

    String getPhysicalFilePath();

    void setPhysicalFilePath(String physicalFilePath);

    String getLogicalFilePath();

    void setLogicalFilePath(String logicalFilePath);

    String getFilePath();

    ObjectQuotingStrategy getObjectQuotingStrategy();

    void setObjectQuotingStrategy(ObjectQuotingStrategy objectQuotingStrategy);

    ChangeSet getChangeSet(String path, String author, String id);

    List<ChangeSet> getChangeSets();

    void addChangeSet(ChangeSet changeSet);

    void setIgnoreClasspathPrefix(boolean ignoreClasspathPrefix);

    boolean ignoreClasspathPrefix();

}
