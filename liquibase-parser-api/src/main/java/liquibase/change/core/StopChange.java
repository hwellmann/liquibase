package liquibase.change.core;

import liquibase.change.BaseChange;
import liquibase.change.Change;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.ResourceAccessor;
import liquibase.util.StringUtils;

import org.kohsuke.MetaInfServices;

@DatabaseChange(name="stop", description = "Stops Liquibase execution with a message. Mainly useful for debugging and stepping through a changelog", priority = ChangeMetaData.PRIORITY_DEFAULT, since = "1.9")
@MetaInfServices(Change.class)
public class StopChange extends BaseChange {

    private String message ="Stop command in changelog file";

    @DatabaseChangeProperty(description = "Message to output when execution stops", exampleValue = "What just happened???")
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = StringUtils.trimToNull(message);
    }

    public static class StopChangeException extends RuntimeException {
        public StopChangeException(String message) {
            super(message);
        }
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    @Override
    protected void customLoadLogic(ParsedNode parsedNode, ResourceAccessor resourceAccessor) throws ParsedNodeException {
        Object value = parsedNode.getValue();
        if (value != null && value instanceof String) {
            setMessage((String) value);
        }
    }
}
