package liquibase.dbdoc;

import liquibase.change.ExecutableChange;
import liquibase.database.Database;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class ColumnWriter extends HTMLWriter {


    public ColumnWriter(File rootOutputDir, Database database) {
        super(new File(rootOutputDir, "columns"), database);
    }

    @Override
    protected String createTitle(Object object) {
        return "Changes affecting column \""+object.toString() + "\"";
    }

    @Override
    protected void writeCustomHTML(FileWriter fileWriter, Object object, List<ExecutableChange> changes, Database database) throws IOException {
    }
}
