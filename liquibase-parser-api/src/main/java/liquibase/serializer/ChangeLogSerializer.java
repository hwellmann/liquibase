package liquibase.serializer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import liquibase.changelog.ChangeSet;

public interface ChangeLogSerializer extends LiquibaseSerializer {

	void write(List<ChangeSet> changeSets, OutputStream out) throws IOException;

    void append(ChangeSet changeSet, File changeLogFile) throws IOException;
}
