package liquibase.change.core;

import liquibase.change.Change;
import liquibase.change.ChangeMetaData;
import liquibase.change.ColumnConfig;
import liquibase.change.DatabaseChange;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.ResourceAccessor;

import org.kohsuke.MetaInfServices;

@DatabaseChange(name="delete", description = "Deletes data from an existing table", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "table")
@MetaInfServices(Change.class)
public class DeleteDataChange extends BaseModifyDataChange {


    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    @Override
    protected void customLoadLogic(ParsedNode parsedNode, ResourceAccessor resourceAccessor) throws ParsedNodeException {
        ParsedNode whereParams = parsedNode.getChild(null, "whereParams");
        if (whereParams != null) {
            for (ParsedNode param : whereParams.getChildren(null, "param")) {
                ColumnConfig columnConfig = new ColumnConfig();
                try {
                    columnConfig.load(param, resourceAccessor);
                } catch (ParsedNodeException e) {
                    e.printStackTrace();
                }
                addWhereParam(columnConfig);
            }
        }
    }

}
