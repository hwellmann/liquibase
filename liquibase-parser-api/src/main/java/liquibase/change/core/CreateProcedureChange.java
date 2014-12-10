package liquibase.change.core;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import liquibase.change.BaseChange;
import liquibase.change.BaseSQLChange;
import liquibase.change.Change;
import liquibase.change.ChangeFactory;
import liquibase.change.ChangeMetaData;
import liquibase.change.CheckSum;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.change.DbmsTargetedChange;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.util.StreamUtil;

import org.kohsuke.MetaInfServices;

@DatabaseChange(name = "createProcedure",
        description = "Defines the definition for a stored procedure. This command is better to use for creating procedures than the raw sql command because it will not attempt to strip comments or break up lines.\n\nOften times it is best to use the CREATE OR REPLACE syntax along with setting runOnChange='true' on the enclosing changeSet tag. That way if you need to make a change to your procedure you can simply change your existing code rather than creating a new REPLACE PROCEDURE call. The advantage to this approach is that it keeps your change log smaller and allows you to more easily see what has changed in your procedure code through your source control system's diff command.",
        priority = ChangeMetaData.PRIORITY_DEFAULT)
@MetaInfServices(Change.class)
public class CreateProcedureChange extends BaseChange implements DbmsTargetedChange {
    private String comments;
    private String catalogName;
    private String schemaName;
    private String procedureName;
    private String procedureText;
	private String dbms;

    private String path;
    private Boolean relativeToChangelogFile;
    private String encoding = null;
    private Boolean replaceIfExists;

    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @DatabaseChangeProperty(exampleValue = "new_customer")
    public String getProcedureName() {
        return procedureName;
    }

    public void setProcedureName(String procedureName) {
        this.procedureName = procedureName;
    }

    @DatabaseChangeProperty(exampleValue = "utf8")
    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    @DatabaseChangeProperty(description = "File containing the procedure text. Either this attribute or a nested procedure text is required.", exampleValue = "com/example/my-logic.sql")
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Boolean isRelativeToChangelogFile() {
        return relativeToChangelogFile;
    }

    public void setRelativeToChangelogFile(Boolean relativeToChangelogFile) {
        this.relativeToChangelogFile = relativeToChangelogFile;
    }


    @DatabaseChangeProperty(serializationType = SerializationType.DIRECT_VALUE,
    exampleValue = "CREATE OR REPLACE PROCEDURE testHello\n" +
            "    IS\n" +
            "    BEGIN\n" +
            "      DBMS_OUTPUT.PUT_LINE('Hello From The Database!');\n" +
            "    END;")
    /**
     * @deprecated Use getProcedureText() instead
     */
    public String getProcedureBody() {
        return procedureText;
    }

    /**
     * @deprecated Use setProcedureText() instead
     */
    @Deprecated
    public void setProcedureBody(String procedureText) {
        this.procedureText = procedureText;
    }

    @DatabaseChangeProperty(isChangeProperty = false)
    public String getProcedureText() {
        return procedureText;
    }

    public void setProcedureText(String procedureText) {
        this.procedureText = procedureText;
    }

    @Override
    @DatabaseChangeProperty(since = "3.1", exampleValue = "h2, oracle")
	public String getDbms() {
		return dbms;
	}

	@Override
    public void setDbms(final String dbms) {
		this.dbms = dbms;
	}

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    @DatabaseChangeProperty
    public Boolean getReplaceIfExists() {
        return replaceIfExists;
    }

    public void setReplaceIfExists(Boolean replaceIfExists) {
        this.replaceIfExists = replaceIfExists;
    }

    public InputStream openSqlStream() throws IOException {
        if (path == null) {
            return null;
        }

        try {
            return StreamUtil.openStream(getPath(), isRelativeToChangelogFile(), getChangeSet(), getResourceAccessor());
        } catch (IOException e) {
            throw new IOException("<"+ChangeFactory.getInstance().getChangeMetaData(this).getName()+" path=" + path + "> -Unable to read file", e);
        }
    }

    /**
     * Calculates the checksum based on the contained SQL.
     *
     * @see liquibase.change.AbstractChange#generateCheckSum()
     */
    @Override
    public CheckSum generateCheckSum() {
        if (this.path == null) {
            return super.generateCheckSum();
        }

        InputStream stream = null;
        try {
            stream = openSqlStream();
        } catch (IOException e) {
            throw new UnexpectedLiquibaseException(e);
        }

        String procedureText = this.procedureText;
        if (stream == null && procedureText == null) {
            procedureText = "";
        }

        if (procedureText != null) {
            try {
                stream = new ByteArrayInputStream(procedureText.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new AssertionError("UTF-8 is not supported by the JVM, this should not happen according to the JavaDoc of the Charset class");
            }
        }

        CheckSum checkSum = CheckSum.compute(new BaseSQLChange.NormalizingStream(";", false, false, stream), false);

        return CheckSum.compute(super.generateCheckSum().toString()+":"+checkSum.toString());

    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    @Override
    protected Map<String, Object> createExampleValueMetaData(String parameterName, DatabaseChangeProperty changePropertyAnnotation) {

        if (parameterName.equals("procedureText") || parameterName.equals("procedureBody")) {
            Map<String, Object> returnMap = super.createExampleValueMetaData(parameterName, changePropertyAnnotation);
            returnMap.put("hsqldb", "CREATE PROCEDURE new_customer(firstname VARCHAR(50), lastname VARCHAR(50))\n" +
                    "   MODIFIES SQL DATA\n" +
                    "   INSERT INTO CUSTOMERS (first_name, last_name) VALUES (firstname, lastname)");

            return returnMap;
        } else {
            return super.createExampleValueMetaData(parameterName, changePropertyAnnotation);
        }
    }
}
