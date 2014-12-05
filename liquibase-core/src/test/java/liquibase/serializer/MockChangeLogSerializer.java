package liquibase.serializer;

import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.changelog.ChangeSetImpl;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.sql.visitor.SqlVisitor;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.io.OutputStream;
import java.io.IOException;


public class MockChangeLogSerializer implements ChangeLogSerializer {

    private String[] validExtensions;

    public MockChangeLogSerializer(String... validExtensions) {
        this.validExtensions = validExtensions;
    }

    @Override
    public String[] getValidFileExtensions() {
        return validExtensions;
    }

	@Override
    public void write(List<ChangeSetImpl> changeSets, OutputStream out)
			throws IOException {
		;
	}

    @Override
    public void append(ChangeSetImpl changeSet, File changeLogFile) throws IOException {

    }

    @Override
    public String serialize(LiquibaseSerializable object, boolean pretty) {
        return null;
    }
}
