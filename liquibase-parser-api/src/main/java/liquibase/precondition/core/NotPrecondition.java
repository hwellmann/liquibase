package liquibase.precondition.core;

import liquibase.precondition.Precondition;
import liquibase.precondition.PreconditionLogic;

import org.kohsuke.MetaInfServices;

/**
 * Class for controling "not" logic in preconditions.
 */
@MetaInfServices(Precondition.class)
public class NotPrecondition extends PreconditionLogic {

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    @Override
    public String getName() {
        return "not";
    }
}
