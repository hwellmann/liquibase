package liquibase.precondition.core;

import java.util.ArrayList;
import java.util.List;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.PreconditionFailedException;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.precondition.FailedPrecondition;
import liquibase.precondition.Precondition;
import liquibase.precondition.PreconditionLogic;

import org.kohsuke.MetaInfServices;

/**
 * Container class for all preconditions on a change log.
 */
@MetaInfServices(Precondition.class)
public class AndPrecondition extends PreconditionLogic {

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    @Override
    public Warnings warn(Database database) {
        return new Warnings();
    }

    @Override
    public ValidationErrors validate(Database database) {
        return new ValidationErrors();
    }

    @Override
    public void check(Database database, DatabaseChangeLog changeLog, ChangeSet changeSet) throws PreconditionFailedException, PreconditionErrorException {
        boolean allPassed = true;
        List<FailedPrecondition> failures = new ArrayList<FailedPrecondition>();
        for (Precondition precondition : getNestedPreconditions()) {
            try {
                precondition.check(database, changeLog, changeSet);
            } catch (PreconditionFailedException e) {
                failures.addAll(e.getFailedPreconditions());
                allPassed = false;
                break;
            }
        }
        if (!allPassed) {
            throw new PreconditionFailedException(failures);
        }
    }


    @Override
    public String getName() {
        return "and";
    }
}
