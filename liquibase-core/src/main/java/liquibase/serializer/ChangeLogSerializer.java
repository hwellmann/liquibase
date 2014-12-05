package liquibase.serializer;

import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.changelog.ChangeSetImpl;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.sql.visitor.SqlVisitor;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.List;

public interface ChangeLogSerializer extends LiquibaseSerializer {

	void write(List<ChangeSetImpl> changeSets, OutputStream out) throws IOException;

    void append(ChangeSetImpl changeSet, File changeLogFile) throws IOException;
}
