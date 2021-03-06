package liquibase.changelog;

import java.util.Date;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.changelog.filter.ContextChangeSetFilter;
import liquibase.changelog.filter.DbmsChangeSetFilter;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.DatabaseHistoryException;
import liquibase.logging.LogFactory;

public abstract class AbstractChangeLogHistoryService implements ChangeLogHistoryService {

    private Database database;

    public Database getDatabase() {
        return database;
    }

    @Override
    public void setDatabase(Database database) {
        this.database = database;
    }

    @Override
    public void reset() {

    }

    @Override
    public ExecutableChangeSet.RunStatus getRunStatus(final ExecutableChangeSet changeSet) throws DatabaseException, DatabaseHistoryException {
        RanChangeSet foundRan = getRanChangeSet(changeSet);

        if (foundRan == null) {
            return ExecutableChangeSet.RunStatus.NOT_RAN;
        } else {
            if (foundRan.getLastCheckSum() == null) {
                try {
                    LogFactory.getLogger().info("Updating NULL md5sum for " + changeSet.toString());
                    replaceChecksum(changeSet);
                } catch (DatabaseException e) {
                    throw new DatabaseException(e);
                }

                return ExecutableChangeSet.RunStatus.ALREADY_RAN;
            } else {
                if (foundRan.getLastCheckSum().equals(changeSet.generateCheckSum())) {
                    return ExecutableChangeSet.RunStatus.ALREADY_RAN;
                } else {
                    if (changeSet.shouldRunOnChange()) {
                        return ExecutableChangeSet.RunStatus.RUN_AGAIN;
                    } else {
                        return ExecutableChangeSet.RunStatus.INVALID_MD5SUM;
//                        throw new DatabaseHistoryException("MD5 Check for " + changeSet.toString() + " failed");
                    }
                }
            }
        }
    }

    @Override
    public void upgradeChecksums(final DatabaseChangeLog databaseChangeLog, final Contexts contexts, LabelExpression labels) throws DatabaseException {
        for (RanChangeSet ranChangeSet : this.getRanChangeSets()) {
            if (ranChangeSet.getLastCheckSum() == null) {
                ChangeSet changeSet = ((DatabaseChangeLogImpl) databaseChangeLog).getChangeSet(ranChangeSet);
                ExecutableChangeSet ecs = new ExecutableChangeSetImpl((ChangeSetImpl) changeSet);
                if (changeSet != null && new ContextChangeSetFilter(contexts).accepts(ecs).isAccepted() && new DbmsChangeSetFilter(getDatabase()).accepts(ecs).isAccepted()) {
                    LogFactory.getLogger().debug("Updating null or out of date checksum on changeSet " + changeSet + " to correct value");
                    replaceChecksum(changeSet);
                }
            }
        }
    }

    @Override
    public RanChangeSet getRanChangeSet(final ChangeSet changeSet) throws DatabaseException, DatabaseHistoryException {
        for (RanChangeSet ranChange : getRanChangeSets()) {
            if (ranChange.isSameAs(changeSet)) {
                return ranChange;
            }
        }
        return null;
    }

    @Override
    public Date getRanDate(ChangeSet changeSet) throws DatabaseException, DatabaseHistoryException {
        RanChangeSet ranChange = getRanChangeSet(changeSet);
        if (ranChange == null) {
            return null;
        } else {
            return ranChange.getDateExecuted();
        }
    }

    protected abstract void replaceChecksum(ChangeSet changeSet) throws DatabaseException;


}
