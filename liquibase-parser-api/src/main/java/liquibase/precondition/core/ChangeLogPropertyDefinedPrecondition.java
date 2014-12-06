package liquibase.precondition.core;

import liquibase.precondition.AbstractPrecondition;
import liquibase.precondition.Precondition;

import org.kohsuke.MetaInfServices;

@MetaInfServices(Precondition.class)
public class ChangeLogPropertyDefinedPrecondition extends AbstractPrecondition {

    private String property;
    private String value;

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    @Override
    public String getName() {
        return "changeLogPropertyDefined";
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
