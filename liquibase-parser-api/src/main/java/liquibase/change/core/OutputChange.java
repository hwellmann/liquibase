package liquibase.change.core;

import liquibase.change.BaseChange;
import liquibase.change.Change;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.serializer.LiquibaseSerializable;
import liquibase.util.StringUtils;

import org.kohsuke.MetaInfServices;

@DatabaseChange(name="output", description = "Logs a message and continues execution.", priority = ChangeMetaData.PRIORITY_DEFAULT, since = "3.3")
@MetaInfServices(Change.class)
public class OutputChange extends BaseChange {

    private String message;
    private String target = "";

    @DatabaseChangeProperty(description = "Message to output", exampleValue = "Make sure you feed the cat", serializationType = LiquibaseSerializable.SerializationType.DIRECT_VALUE)
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = StringUtils.trimToNull(message);
    }

    @DatabaseChangeProperty(description = "Target for message. Possible values: STDOUT, STDERR, FATAL, WARN, INFO, DEBUG. Default value: STDERR", exampleValue = "STDERR")
    public String getTarget() {
        if (target == null) {
            return "STDERR";
        }
        return target;
    }

    public void setTarget(String target) {
        this.target = StringUtils.trimToNull(target);
    }


    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    @Override
    public Object getSerializableFieldValue(String field) {
        Object value = super.getSerializableFieldValue(field);
        if (field.equals("target") && value.equals("")) {
            return null;
        }
        return value;
    }
}
