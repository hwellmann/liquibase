package liquibase.precondition.core;

import liquibase.precondition.Precondition;
import liquibase.precondition.PreconditionLogic;

import org.kohsuke.MetaInfServices;

/**
 * Container class for all preconditions on a change log.
 */
@MetaInfServices(Precondition.class)
public class AndPrecondition extends PreconditionLogic {

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    @Override
    public String getName() {
        return "and";
    }
}
