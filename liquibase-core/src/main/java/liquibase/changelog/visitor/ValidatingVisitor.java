package liquibase.changelog.visitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import liquibase.change.Change;
import liquibase.change.IChange;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.DatabaseChangeLogImpl;
import liquibase.changelog.ExecutableChangeSet;
import liquibase.changelog.RanChangeSet;
import liquibase.changelog.filter.ChangeSetFilterResult;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.PreconditionFailedException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.logging.LogFactory;
import liquibase.precondition.ErrorPrecondition;
import liquibase.precondition.FailedPrecondition;
import liquibase.precondition.core.PreconditionContainer;
import liquibase.util.StringUtils;

public class ValidatingVisitor implements ChangeSetVisitor {

    private List<ExecutableChangeSet> invalidMD5Sums = new ArrayList<ExecutableChangeSet>();
    private List<FailedPrecondition> failedPreconditions = new ArrayList<FailedPrecondition>();
    private List<ErrorPrecondition> errorPreconditions = new ArrayList<ErrorPrecondition>();
    private Set<ExecutableChangeSet> duplicateChangeSets = new HashSet<ExecutableChangeSet>();
    private List<SetupException> setupExceptions = new ArrayList<SetupException>();
    private List<Throwable> changeValidationExceptions = new ArrayList<Throwable>();
    private ValidationErrors validationErrors = new ValidationErrors();
    private Warnings warnings = new Warnings();

    private Set<String> seenChangeSets = new HashSet<String>();

    private Map<String, RanChangeSet> ranIndex;
    private Database database;

    public ValidatingVisitor(List<RanChangeSet> ranChangeSets) {
        ranIndex = new HashMap<String, RanChangeSet>();
        for(RanChangeSet changeSet:ranChangeSets) {
            ranIndex.put(changeSet.toString(), changeSet);
        }
    }

    public void validate(Database database, DatabaseChangeLogImpl changeLog) {
        this.database = database;
        PreconditionContainer preconditions = (PreconditionContainer) changeLog.getPreconditions();
        try {
            if (preconditions == null) {
                return;
            }
            preconditions.check(database, changeLog, null);
        } catch (PreconditionFailedException e) {
            LogFactory.getLogger().debug("Precondition Failed: "+e.getMessage(), e);
            failedPreconditions.addAll(e.getFailedPreconditions());
        } catch (PreconditionErrorException e) {
            LogFactory.getLogger().debug("Precondition Error: "+e.getMessage(), e);
            errorPreconditions.addAll(e.getErrorPreconditions());
        } finally {
            try {
                if (database.getConnection() != null) {
                    database.rollback();
                }
            } catch (DatabaseException e) {
                LogFactory.getLogger().warning("Error rolling back after precondition check", e);
            }
        }
    }

    @Override
    public Direction getDirection() {
        return ChangeSetVisitor.Direction.FORWARD;
    }

    @Override
    public void visit(ExecutableChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database, Set<ChangeSetFilterResult> filterResults) throws LiquibaseException {
        RanChangeSet ranChangeSet = ranIndex.get(changeSet.toString(false));
        boolean ran = ranChangeSet != null;
        boolean shouldValidate = !ran || changeSet.shouldRunOnChange() || changeSet.shouldAlwaysRun();
        for (IChange c : changeSet.getChanges()) {
            Change change = (Change) c;
            try {
                change.finishInitialization();
            } catch (SetupException se) {
                setupExceptions.add(se);
            }


            if(shouldValidate){
                warnings.addAll(change.warn(database));

                try {
                    ValidationErrors foundErrors = change.validate(database);

                    if (foundErrors != null && foundErrors.hasErrors()) {
                        if (changeSet.getOnValidationFail().equals(ChangeSet.ValidationFailOption.MARK_RAN)) {
                            LogFactory.getLogger().info("Skipping changeSet "+changeSet+" due to validation error(s): "+ StringUtils.join(foundErrors.getErrorMessages(), ", "));
                            changeSet.setValidationFailed(true);
                        } else {
                            validationErrors.addAll(foundErrors, changeSet);
                        }
                    }
                } catch (Throwable e) {
                    changeValidationExceptions.add(e);
                }
            }
        }

        if(ranChangeSet != null){
            if (!changeSet.isCheckSumValid(ranChangeSet.getLastCheckSum())) {
                if (!changeSet.shouldRunOnChange()) {
                    invalidMD5Sums.add(changeSet);
                }
            }
        }


        String changeSetString = changeSet.toString(false);
        if (seenChangeSets.contains(changeSetString)) {
            duplicateChangeSets.add(changeSet);
        } else {
            seenChangeSets.add(changeSetString);
        }
    }

    public List<ExecutableChangeSet> getInvalidMD5Sums() {
        return invalidMD5Sums;
    }


    public List<FailedPrecondition> getFailedPreconditions() {
        return failedPreconditions;
    }

    public List<ErrorPrecondition> getErrorPreconditions() {
        return errorPreconditions;
    }

    public Set<ExecutableChangeSet> getDuplicateChangeSets() {
        return duplicateChangeSets;
    }

    public List<SetupException> getSetupExceptions() {
        return setupExceptions;
    }

    public List<Throwable> getChangeValidationExceptions() {
        return changeValidationExceptions;
    }

    public ValidationErrors getValidationErrors() {
        return validationErrors;
    }

    public Warnings getWarnings() {
        return warnings;
    }

    public boolean validationPassed() {
        return invalidMD5Sums.size() == 0
                && failedPreconditions.size() == 0
                && errorPreconditions.size() == 0
                && duplicateChangeSets.size() == 0
                && changeValidationExceptions.size() == 0
                && setupExceptions.size() == 0
                && !validationErrors.hasErrors();
    }

    public Database getDatabase() {
        return database;
    }
}
