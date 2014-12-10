package liquibase.action;

import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeStatus;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.change.ExecutableChange;
import liquibase.change.core.TagDatabaseChange;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.TagDatabaseStatement;

import org.kohsuke.MetaInfServices;

@DatabaseChange(name="tagDatabase", description = "Applies a tag to the database for future rollback", priority = ChangeMetaData.PRIORITY_DEFAULT, since = "1.6")
@MetaInfServices(ExecutableChange.class)
public class TagDatabaseAction extends AbstractAction<TagDatabaseChange> {

    public TagDatabaseAction() {
        super(new TagDatabaseChange());
    }

    public TagDatabaseAction(TagDatabaseChange change) {
        super(change);
    }

    @DatabaseChangeProperty(description = "Tag to apply", exampleValue = "version_1.3")
    public String getTag() {
        return change.getTag();
    }

    public void setTag(String tag) {
        change.setTag(tag);
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[] {
                new TagDatabaseStatement(getTag())
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
        return "Tag '"+ getTag() +"' applied to database";
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
