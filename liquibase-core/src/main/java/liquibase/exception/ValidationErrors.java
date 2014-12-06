package liquibase.exception;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import liquibase.changelog.ChangeSet;
import liquibase.util.StringUtils;

public class ValidationErrors {

    protected List<String> errorMessages = new ArrayList<String>();

    public boolean hasErrors() {
        return errorMessages.size() > 0;
    }

    public void checkRequiredField(String requiredFieldName, Object value) {
        if (value == null) {
            addError(requiredFieldName + " is required");
        } else if (value instanceof Collection && ((Collection) value).size() == 0) {
            addError(requiredFieldName + " is empty");
        } else if (value instanceof Object[] && ((Object[]) value).length == 0) {
            addError(requiredFieldName + " is empty");
        }
    }

    public ValidationErrors addError(String message) {
        errorMessages.add(message);
        return this;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }

    public ValidationErrors addAll(ValidationErrors validationErrors) {
        if (validationErrors == null) {
            return this;
        }
        this.errorMessages.addAll(validationErrors.getErrorMessages());
        return this;
    }

    public void addAll(ValidationErrors validationErrors, ChangeSet changeSet) {
        for (String message : validationErrors.getErrorMessages()) {
            this.errorMessages.add(message+", "+changeSet);
        }
    }

    @Override
    public String toString() {
        if (getErrorMessages().size() == 0) {
            return "No errors";
        }
        return StringUtils.join(getErrorMessages(), "; ");
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    public List<String> getRequiredErrorMessages() {
        List<String> requiredErrorMessages = new ArrayList<String>();
        for (String message : errorMessages) {
            if (message.contains("is required")) {
                requiredErrorMessages.add(message);
            }
        }
        return Collections.unmodifiableList(requiredErrorMessages);
    }

    public List<String> getUnsupportedErrorMessages() {
        List<String> unsupportedErrorMessages = new ArrayList<String>();
        for (String message : errorMessages) {
            if (message.contains(" is not allowed on ")) {
                unsupportedErrorMessages.add(message);
            }
        }
        return Collections.unmodifiableList(unsupportedErrorMessages);
    }
}
