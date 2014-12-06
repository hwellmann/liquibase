package liquibase.precondition;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.PreconditionFailedException;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;

/**
 * Marker interface for preconditions.  May become an annotation in the future.
 */
public interface ExecutablePrecondition extends Precondition {
    public Warnings warn(Database database);

    public ValidationErrors validate(Database database);

    public void check(Database database, DatabaseChangeLog changeLog, ChangeSet changeSet) throws PreconditionFailedException, PreconditionErrorException;
}
