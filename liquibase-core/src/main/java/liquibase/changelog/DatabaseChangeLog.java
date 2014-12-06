package liquibase.changelog;

import java.util.List;

import liquibase.database.ObjectQuotingStrategy;
import liquibase.precondition.Precondition;

public interface DatabaseChangeLog extends Comparable<DatabaseChangeLogImpl> {

    Precondition getPreconditions();

    void setPreconditions(Precondition precond);

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
