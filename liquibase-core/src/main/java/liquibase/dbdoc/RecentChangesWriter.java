package liquibase.dbdoc;

import liquibase.change.ExecutableChange;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.DatabaseHistoryException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class RecentChangesWriter extends HTMLWriter {

    public RecentChangesWriter(File rootOutputDir, Database database) {
        super(new File(rootOutputDir, "recent"), database);
    }

    @Override
    protected String createTitle(Object object) {
        return "Recent Changes";
    }

    @Override
    protected void writeBody(FileWriter fileWriter, Object object, List<ExecutableChange> ranChanges, List<ExecutableChange> changesToRun) throws IOException, DatabaseHistoryException, DatabaseException {
        writeCustomHTML(fileWriter, object, ranChanges, database);
        writeChanges("Most Recent Changes", fileWriter, ranChanges);
    }

    @Override
    protected void writeCustomHTML(FileWriter fileWriter, Object object, List<ExecutableChange> changes, Database database) throws IOException {
    }
}
