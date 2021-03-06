package liquibase.dbdoc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import liquibase.change.ExecutableChange;
import liquibase.change.ExecutableChangeFactory;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.ExecutableChangeSet;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.DatabaseHistoryException;
import liquibase.exception.MigrationFailedException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.executor.LoggingExecutor;

public class PendingSQLWriter extends HTMLWriter {

    private DatabaseChangeLog databaseChangeLog;

    public PendingSQLWriter(File rootOutputDir, Database database, DatabaseChangeLog databaseChangeLog) {
        super(new File(rootOutputDir, "pending"), database);
        this.databaseChangeLog = databaseChangeLog;
    }

    @Override
    protected String createTitle(Object object) {
        return "Pending SQL";
    }

    @Override
    protected void writeBody(FileWriter fileWriter, Object object, List<ExecutableChange> ranChanges, List<ExecutableChange> changesToRun) throws IOException, DatabaseHistoryException, DatabaseException {

        Executor oldTemplate = ExecutorService.getInstance().getExecutor(database);
        LoggingExecutor loggingExecutor = new LoggingExecutor(ExecutorService.getInstance().getExecutor(database), fileWriter, database);
        ExecutorService.getInstance().setExecutor(database, loggingExecutor);

        try {
            if (changesToRun.size() == 0) {
                fileWriter.append("<b>NONE</b>");
            }

            fileWriter.append("<code><pre>");

            ChangeSet lastRunChangeSet = null;

            for (ExecutableChange change : changesToRun) {
                ExecutableChangeSet thisChangeSet = (ExecutableChangeSet) change.getChangeSet();
                if (thisChangeSet.equals(lastRunChangeSet)) {
                    continue;
                }
                lastRunChangeSet = thisChangeSet;
                String anchor = thisChangeSet.toString(false).replaceAll("\\W","_");
                fileWriter.append("<a name='").append(anchor).append("'/>");
                try {
                    thisChangeSet.execute(databaseChangeLog, null, this.database);
                } catch (MigrationFailedException e) {
                    fileWriter.append("EXECUTION ERROR: ").append(ExecutableChangeFactory.getInstance().getChangeMetaData(change).getDescription()).append(": ").append(e.getMessage()).append("\n\n");
                }
            }
            fileWriter.append("</pre></code>");
        } finally {
            ExecutorService.getInstance().setExecutor(database, oldTemplate);
        }
    }

    @Override
    protected void writeCustomHTML(FileWriter fileWriter, Object object, List<ExecutableChange> changes, Database database) throws IOException {
    }
}
