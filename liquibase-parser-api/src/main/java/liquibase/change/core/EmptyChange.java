package liquibase.change.core;

import liquibase.change.BaseChange;
import liquibase.change.Change;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;

import org.kohsuke.MetaInfServices;

@DatabaseChange(name="empty", description = "empty", priority = ChangeMetaData.PRIORITY_DEFAULT)
@MetaInfServices(Change.class)
public class EmptyChange extends BaseChange {

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}
