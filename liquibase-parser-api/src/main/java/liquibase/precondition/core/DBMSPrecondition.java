package liquibase.precondition.core;

import liquibase.precondition.AbstractPrecondition;
import liquibase.precondition.Precondition;

import org.kohsuke.MetaInfServices;

/**
 * Precondition for specifying the type of database (oracle, mysql, etc.).
 */

@MetaInfServices(Precondition.class)
public class DBMSPrecondition extends AbstractPrecondition {
    private String type;


    public DBMSPrecondition() {
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    public String getType() {
        return type;
    }

    public void setType(String atype) {
        this.type = atype.toLowerCase();
    }

    @Override
    public String getName() {
        return "dbms";
    }

}
