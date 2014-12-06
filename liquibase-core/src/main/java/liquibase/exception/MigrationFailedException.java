package liquibase.exception;

import liquibase.changelog.ExecutableChangeSet;

public class MigrationFailedException extends LiquibaseException {

    private static final long serialVersionUID = 1L;
    private ExecutableChangeSet failedChangeSet;
    
    public MigrationFailedException() {
    }

    public MigrationFailedException(ExecutableChangeSet failedChangeSet, String message) {
        super(message);
        this.failedChangeSet = failedChangeSet;
    }


    public MigrationFailedException(ExecutableChangeSet failedChangeSet, String message, Throwable cause) {
        super(message, cause);
        this.failedChangeSet = failedChangeSet;
    }

    public MigrationFailedException(ExecutableChangeSet failedChangeSet, Throwable cause) {
        super(cause);
        this.failedChangeSet = failedChangeSet;
    }


    @Override
    public String getMessage() {
        String message = "Migration failed";
        if (failedChangeSet != null) {
            message += " for change set "+failedChangeSet.toString(false);
        }
        message += ":\n     Reason: "+super.getMessage();
//        Throwable cause = this.getCause();
//        while (cause != null) {
//            message += ":\n          Caused By: "+cause.getMessage();
//            cause = cause.getCause();
//        }

        return message;
    }
}
