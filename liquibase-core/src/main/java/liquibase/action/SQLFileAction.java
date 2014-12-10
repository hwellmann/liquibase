package liquibase.action;

import java.io.IOException;
import java.io.InputStream;

import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.change.ExecutableChange;
import liquibase.change.core.SQLFileChange;

import org.kohsuke.MetaInfServices;

/**
 * Represents a Change for custom SQL stored in a File.
 * <p/>
 * To create an instance call the constructor as normal and then call
 *
 * @author <a href="mailto:csuml@yahoo.co.uk">Paul Keeble</a>
 * @link{#setResourceAccesssor(ResourceAccessor)} before calling setPath otherwise the
 * file will likely not be found.
 */
@DatabaseChange(name = "sqlFile",
        description = "The 'sqlFile' tag allows you to specify any sql statements and have it stored external in a file. It is useful for complex changes that are not supported through LiquiBase's automated refactoring tags such as stored procedures.\n" +
                "\n" +
                "The sqlFile refactoring finds the file by searching in the following order:\n" +
                "\n" +
                "The file is searched for in the classpath. This can be manually set and by default the liquibase startup script adds the current directory when run.\n" +
                "The file is searched for using the file attribute as a file name. This allows absolute paths to be used or relative paths to the working directory to be used.\n" +
                "The 'sqlFile' tag can also support multiline statements in the same file. Statements can either be split using a ; at the end of the last line of the SQL or a go on its own on the line between the statements can be used.Multiline SQL statements are also supported and only a ; or go statement will finish a statement, a new line is not enough. Files containing a single statement do not need to use a ; or go.\n" +
                "\n" +
                "The sql file can also contain comments of either of the following formats:\n" +
                "\n" +
                "A multiline comment that starts with /* and ends with */.\n" +
                "A single line comment starting with <space>--<space> and finishing at the end of the line",
        priority = ChangeMetaData.PRIORITY_DEFAULT)
@MetaInfServices(ExecutableChange.class)
public class SQLFileAction extends AbstractSQLAction<SQLFileChange> {

    public SQLFileAction() {
        super(new SQLFileChange());
    }

    public SQLFileAction(SQLFileChange change) {
        super(change);
    }

    @DatabaseChangeProperty(description = "The file path of the SQL file to load", requiredForDatabase = "all", exampleValue = "my/path/file.sql")
    public String getPath() {
        return change.getPath();
    }

    /**
     * Sets the file name but setUp must be called for the change to have impact.
     *
     * @param fileName The file to use
     */
    public void setPath(String fileName) {
        change.setPath(fileName);
    }

    /**
     * The encoding of the file containing SQL statements
     *
     * @return the encoding
     */
    @DatabaseChangeProperty(exampleValue = "utf8")
    public String getEncoding() {
        return change.getEncoding();
    }

    /**
     * @param encoding the encoding to set
     */
    public void setEncoding(String encoding) {
        change.setEncoding(encoding);
    }


    public Boolean isRelativeToChangelogFile() {
        return change.isRelativeToChangelogFile();
    }

    public void setRelativeToChangelogFile(Boolean relativeToChangelogFile) {
        change.setRelativeToChangelogFile(relativeToChangelogFile);
    }

    public InputStream openSqlStream() throws IOException {
        return change.openSqlStream();
    }

    @Override
    public String getConfirmationMessage() {
        return "SQL in file " + getPath() + " executed";
    }

    public String getSql() {
        return change.getSql();
    }

    public void setSql(String sql) {
        change.setSql(sql);
    }

}
