package liquibase.parser.core.xml;

import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.DatabaseChangeLogImpl;
import liquibase.exception.ChangeLogParseException;
import liquibase.parser.ChangeLogParser;
import liquibase.parser.core.ParsedNode;
import liquibase.resource.ResourceAccessor;

public abstract class AbstractChangeLogParser implements ChangeLogParser {

    @Override
    public DatabaseChangeLogImpl parse(String physicalChangeLogLocation, ChangeLogParameters changeLogParameters, ResourceAccessor resourceAccessor) throws ChangeLogParseException {
        ParsedNode parsedNode = parseToNode(physicalChangeLogLocation, changeLogParameters, resourceAccessor);
        if (parsedNode == null) {
            return null;
        }

        DatabaseChangeLogImpl changeLog = new DatabaseChangeLogImpl(physicalChangeLogLocation);
        changeLog.setChangeLogParameters(changeLogParameters);
        try {
            changeLog.load(parsedNode, resourceAccessor);
        } catch (Exception e) {
            throw new ChangeLogParseException(e);
        }

        return changeLog;
    }

    protected abstract ParsedNode parseToNode(String physicalChangeLogLocation, ChangeLogParameters changeLogParameters, ResourceAccessor resourceAccessor) throws ChangeLogParseException;
}
