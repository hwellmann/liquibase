package liquibase.precondition.core;

import liquibase.database.ObjectQuotingStrategy;
import liquibase.precondition.AbstractPrecondition;
import liquibase.precondition.Precondition;

import org.kohsuke.MetaInfServices;

@MetaInfServices(Precondition.class)
public class ObjectQuotingStrategyPrecondition extends AbstractPrecondition {
    private ObjectQuotingStrategy strategy;

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    @Override
    public String getName() {
        return "expectedQuotingStrategy";
    }

    public void setStrategy(String strategy) {
        this.strategy = ObjectQuotingStrategy.valueOf(strategy);
    }


    /**
     * @return the strategy
     */
    public ObjectQuotingStrategy getStrategy() {
        return strategy;
    }


}
