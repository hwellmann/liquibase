package liquibase.precondition.core;

import liquibase.precondition.AbstractPrecondition;
import liquibase.precondition.Precondition;

import org.kohsuke.MetaInfServices;

@MetaInfServices(Precondition.class)
public class ChangeSetExecutedPrecondition extends AbstractPrecondition {

    private String changeLogFile;
    private String id;
    private String author;

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    public String getChangeLogFile() {
        return changeLogFile;
    }

    public void setChangeLogFile(String changeLogFile) {
        this.changeLogFile = changeLogFile;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    @Override
    public String getName() {
        return "changeSetExecuted";
    }
}
