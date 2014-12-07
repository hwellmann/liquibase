package liquibase.exception;

import liquibase.change.ExecutableChange;
import liquibase.change.ExecutableChangeFactory;
import liquibase.changelog.ExecutableChangeSet;

public class InvalidChangeDefinitionException extends LiquibaseException {

    public InvalidChangeDefinitionException(String message, ExecutableChange change) {
        super(ExecutableChangeFactory.getInstance().getChangeMetaData(change).getName()+" in '"+((ExecutableChangeSet) change.getChangeSet()).toString(false)+"' is invalid: "+message);
    }
}
