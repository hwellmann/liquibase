package liquibase.change.core;

import liquibase.change.AbstractChange;
import liquibase.change.ExecutableChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeStatus;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.TagDatabaseStatement;

import org.kohsuke.MetaInfServices;

@DatabaseChange(name="tagDatabase", description = "Applies a tag to the database for future rollback", priority = ChangeMetaData.PRIORITY_DEFAULT, since = "1.6")
@MetaInfServices(ExecutableChange.class)
public class TagDatabaseChange extends AbstractChange {

    private String tag;

    @DatabaseChangeProperty(description = "Tag to apply", exampleValue = "version_1.3")
    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[] {
                new TagDatabaseStatement(tag)
        };
    }

    @Override
    public ChangeStatus checkStatus(Database database) {
        try {
            return new ChangeStatus().assertComplete(ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database).tagExists(getTag()), "Database not tagged");
        } catch (DatabaseException e) {
            return new ChangeStatus().unknown(e);
        }
    }

    @Override
    public String getConfirmationMessage() {
        return "Tag '"+tag+"' applied to database";
    }

    @Override
    protected ExecutableChange[] createInverses() {
        return new ExecutableChange[0];
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}
