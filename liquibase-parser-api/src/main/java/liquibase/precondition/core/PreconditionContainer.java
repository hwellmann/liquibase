package liquibase.precondition.core;

import java.util.ArrayList;
import java.util.List;

import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.ResourceAccessor;
import liquibase.serializer.LiquibaseSerializable;
import liquibase.util.StringUtils;

public class PreconditionContainer extends AndPrecondition {

    public enum FailOption {
        HALT("HALT"),
        CONTINUE("CONTINUE"),
        MARK_RAN("MARK_RAN"),
        WARN("WARN");

        String key;

        FailOption(String key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return key;
        }
    }

    public enum ErrorOption {
        HALT("HALT"),
        CONTINUE("CONTINUE"),
        MARK_RAN("MARK_RAN"),
        WARN("WARN");

        String key;

        ErrorOption(String key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return key;
        }
    }


    public enum OnSqlOutputOption {
        IGNORE("IGNORE"),
        TEST("TEST"),
        FAIL("FAIL");

        String key;

        OnSqlOutputOption(String key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return key;
        }
    }

    private FailOption onFail = FailOption.HALT;
    private ErrorOption onError = ErrorOption.HALT;
    private OnSqlOutputOption onSqlOutput = OnSqlOutputOption.IGNORE;
    private String onFailMessage;
    private String onErrorMessage;


    public FailOption getOnFail() {
        return onFail;
    }

    public void setOnFail(String onFail) {
        if (onFail == null) {
            this.onFail = FailOption.HALT;
        } else {
            for (FailOption option : FailOption.values()) {
                if (option.key.equalsIgnoreCase(onFail)) {
                    this.onFail = option;
                    return;
                }
            }
            List<String> possibleOptions = new ArrayList<String>();
            for (FailOption option : FailOption.values()) {
                possibleOptions.add(option.key);
            }
            throw new RuntimeException("Unknown onFail attribute value '"+onFail+"'.  Possible values: " + StringUtils.join(possibleOptions, ", "));
        }
    }

    public ErrorOption getOnError() {
        return onError;
    }

    public void setOnError(String onError) {
        if (onError == null) {
            this.onError = ErrorOption.HALT;
        } else {
            for (ErrorOption option : ErrorOption.values()) {
                if (option.key.equalsIgnoreCase(onError)) {
                    this.onError = option;
                    return;
                }
            }
            List<String> possibleOptions = new ArrayList<String>();
            for (ErrorOption option : ErrorOption.values()) {
                possibleOptions.add(option.key);
            }
            throw new RuntimeException("Unknown onError attribute value '"+onError+"'.  Possible values: " + StringUtils.join(possibleOptions, ", "));
        }
    }

    public OnSqlOutputOption getOnSqlOutput() {
        return onSqlOutput;
    }

    public void setOnSqlOutput(String onSqlOutput) {
        if (onSqlOutput == null) {
            setOnSqlOutput((OnSqlOutputOption)null);
            return;
        }

        for (OnSqlOutputOption option : OnSqlOutputOption.values()) {
            if (option.key.equalsIgnoreCase(onSqlOutput)) {
                setOnSqlOutput(option);
                return;
            }
        }
        List<String> possibleOptions = new ArrayList<String>();
        for (OnSqlOutputOption option : OnSqlOutputOption.values()) {
            possibleOptions.add(option.key);
        }
        throw new RuntimeException("Unknown onSqlOutput attribute value '" + onSqlOutput + "'.  Possible values: " + StringUtils.join(possibleOptions, ", "));
    }

    public void setOnSqlOutput(OnSqlOutputOption onSqlOutput) {
        if (onSqlOutput == null) {
            this.onSqlOutput = OnSqlOutputOption.IGNORE;
        } else {
            this.onSqlOutput = onSqlOutput;
        }
    }

    public String getOnFailMessage() {
        return onFailMessage;
    }

    public void setOnFailMessage(String onFailMessage) {
        this.onFailMessage = onFailMessage;
    }

    public String getOnErrorMessage() {
        return onErrorMessage;
    }

    public void setOnErrorMessage(String onErrorMessage) {
        this.onErrorMessage = onErrorMessage;
    }

    @Override
    public String getSerializedObjectNamespace() {
        return LiquibaseSerializable.STANDARD_CHANGELOG_NAMESPACE;
    }

    @Override
    public void load(ParsedNode parsedNode, ResourceAccessor resourceAccessor) throws ParsedNodeException {
        this.setOnError(parsedNode.getChildValue(null, "onError", String.class));
        this.setOnErrorMessage(parsedNode.getChildValue(null, "onErrorMessage", String.class));
        this.setOnFail(parsedNode.getChildValue(null, "onFail", String.class));
        this.setOnFailMessage(parsedNode.getChildValue(null, "onFailMessage", String.class));

        super.load(parsedNode, resourceAccessor);
    }

}
