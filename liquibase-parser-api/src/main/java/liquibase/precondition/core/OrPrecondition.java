package liquibase.precondition.core;

import liquibase.precondition.Precondition;
import liquibase.precondition.PreconditionLogic;

import org.kohsuke.MetaInfServices;

/**
 * Class for controling "or" logic in preconditions.
 */
@MetaInfServices(Precondition.class)
public class OrPrecondition extends PreconditionLogic {

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    @Override
    public String getName() {
        return "or";
    }
}
