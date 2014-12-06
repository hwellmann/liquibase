package liquibase.precondition;

import java.util.Set;

import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.ResourceAccessor;

public class MockPrecondition implements ExecutablePrecondition {
    @Override
    public String getName() {
        return "mock";
    }

    @Override
    public void load(ParsedNode parsedNode, ResourceAccessor resourceAccessor) throws ParsedNodeException {

    }

    @Override
    public String getSerializedObjectName() {
        return null;
    }

    @Override
    public Set<String> getSerializableFields() {
        return null;
    }

    @Override
    public Object getSerializableFieldValue(String field) {
        return null;
    }

    @Override
    public SerializationType getSerializableFieldType(String field) {
        return null;
    }

    @Override
    public String getSerializedObjectNamespace() {
        return null;
    }

    @Override
    public String getSerializableFieldNamespace(String field) {
        return getSerializedObjectNamespace();
    }


    @Override
    public ParsedNode serialize() {
        return null;
    }
}
