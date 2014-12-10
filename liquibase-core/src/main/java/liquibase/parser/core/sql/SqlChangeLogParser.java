package liquibase.parser.core.sql;

import java.io.IOException;
import java.io.InputStream;

import liquibase.change.core.RawSQLChange;
import liquibase.changelog.ChangeSetImpl;
import liquibase.changelog.DatabaseChangeLogImpl;
import liquibase.changelog.ChangeLogParameters;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.exception.ChangeLogParseException;
import liquibase.parser.ChangeLogParser;
import liquibase.resource.ResourceAccessor;
import liquibase.util.StreamUtil;

import org.kohsuke.MetaInfServices;

@MetaInfServices(ChangeLogParser.class)
public class SqlChangeLogParser implements ChangeLogParser {

    @Override
    public boolean supports(String changeLogFile, ResourceAccessor resourceAccessor) {
        return changeLogFile.endsWith(".sql");
    }

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    public DatabaseChangeLogImpl parse(String physicalChangeLogLocation, ChangeLogParameters changeLogParameters, ResourceAccessor resourceAccessor) throws ChangeLogParseException {

        DatabaseChangeLogImpl changeLog = new DatabaseChangeLogImpl();
        changeLog.setPhysicalFilePath(physicalChangeLogLocation);

        RawSQLChange change = new RawSQLChange();

        try {
            InputStream sqlStream = StreamUtil.singleInputStream(physicalChangeLogLocation, resourceAccessor);
            if (sqlStream == null) {
                throw new ChangeLogParseException("File does not exist: "+physicalChangeLogLocation);
            }
            String sql = StreamUtil.getStreamContents(sqlStream, null);
            change.setSql(sql);
        } catch (IOException e) {
            throw new ChangeLogParseException(e);
        }
        change.setResourceAccessor(resourceAccessor);
        change.setSplitStatements(false);
        change.setStripComments(false);

        ChangeSetImpl changeSet = new ChangeSetImpl("raw", "includeAll", false, false, physicalChangeLogLocation, null, null, true, ObjectQuotingStrategy.LEGACY, changeLog);
        changeSet.addChange(change);

        changeLog.addChangeSet(changeSet);

        return changeLog;
    }
}
