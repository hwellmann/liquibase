package liquibase.precondition;

import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.ResourceAccessor;
import liquibase.serializer.LiquibaseSerializable;

public interface Precondition extends LiquibaseSerializable {

    String getName();

    @Override
    void load(ParsedNode parsedNode, ResourceAccessor resourceAccessor) throws ParsedNodeException;

}
