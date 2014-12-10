package liquibase.change.core;

import liquibase.change.BaseChange;
import liquibase.change.Change;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;

import org.kohsuke.MetaInfServices;

@DatabaseChange(name="tagDatabase", description = "Applies a tag to the database for future rollback", priority = ChangeMetaData.PRIORITY_DEFAULT, since = "1.6")
@MetaInfServices(Change.class)
public class TagDatabaseChange extends BaseChange {

    private String tag;

    @DatabaseChangeProperty(description = "Tag to apply", exampleValue = "version_1.3")
    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}
