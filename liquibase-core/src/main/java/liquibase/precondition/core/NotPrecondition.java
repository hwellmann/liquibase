package liquibase.precondition.core;

import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.PreconditionFailedException;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.precondition.ExecutablePrecondition;
import liquibase.precondition.PreconditionLogic;

import org.kohsuke.MetaInfServices;

/**
 * Class for controling "not" logic in preconditions.
 */
@MetaInfServices(ExecutablePrecondition.class)
public class NotPrecondition extends PreconditionLogic {

    @Override
    public Warnings warn(Database database) {
        return new Warnings();
    }

    @Override
    public ValidationErrors validate(Database database) {
        return new ValidationErrors();
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    @Override
    public void check(Database database, DatabaseChangeLog changeLog, ChangeSet changeSet) throws PreconditionFailedException, PreconditionErrorException {
        for (ExecutablePrecondition precondition : getNestedPreconditions()) {
            boolean threwException = false;
            try {
                precondition.check(database, changeLog, changeSet);
            } catch (PreconditionFailedException e) {
                ; //that's what we want with a Not precondition
                threwException = true;
            }

            if (!threwException) {
                throw new PreconditionFailedException("Not precondition failed", changeLog, this);
            }
        }
    }

    @Override
    public String getName() {
        return "not";
    }
}
