package liquibase.precondition;

import liquibase.exception.SetupException;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.ResourceAccessor;

import java.util.ArrayList;
import java.util.List;

/**
 * Marker interface for precondition logic tags (and,or, not)
 */
public abstract class PreconditionLogic extends AbstractPrecondition {
    private List<ExecutablePrecondition> nestedPreconditions = new ArrayList<ExecutablePrecondition>();

    public List<ExecutablePrecondition> getNestedPreconditions() {
        return this.nestedPreconditions;
    }

    public void addNestedPrecondition(ExecutablePrecondition precondition) {
        if (precondition != null) {
            nestedPreconditions.add(precondition);
        }
    }

    @Override
    public void load(ParsedNode parsedNode, ResourceAccessor resourceAccessor) throws ParsedNodeException {
        super.load(parsedNode, resourceAccessor);

        for (ParsedNode child : parsedNode.getChildren()) {
            addNestedPrecondition(toPrecondition(child, resourceAccessor));
        }
    }

    protected ExecutablePrecondition toPrecondition(ParsedNode node, ResourceAccessor resourceAccessor) throws ParsedNodeException {
        ExecutablePrecondition precondition = PreconditionFactory.getInstance().create(node.getName());
        if (precondition == null) {
            return null;
        }

        precondition.load(node, resourceAccessor);
        return precondition;
    }
}
