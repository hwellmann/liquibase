package liquibase.action;

import java.io.IOException;
import java.io.InputStream;

import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeStatus;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.change.DbmsTargetedChange;
import liquibase.change.ExecutableChange;
import liquibase.change.ExecutableChangeFactory;
import liquibase.change.core.CreateProcedureChange;
import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrorHandler;
import liquibase.exception.ValidationErrors;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.CreateProcedureStatement;
import liquibase.util.StreamUtil;
import liquibase.util.StringUtils;

import org.kohsuke.MetaInfServices;

@DatabaseChange(name = "createProcedure",
        description = "Defines the definition for a stored procedure. This command is better to use for creating procedures than the raw sql command because it will not attempt to strip comments or break up lines.\n\nOften times it is best to use the CREATE OR REPLACE syntax along with setting runOnChange='true' on the enclosing changeSet tag. That way if you need to make a change to your procedure you can simply change your existing code rather than creating a new REPLACE PROCEDURE call. The advantage to this approach is that it keeps your change log smaller and allows you to more easily see what has changed in your procedure code through your source control system's diff command.",
        priority = ChangeMetaData.PRIORITY_DEFAULT)
@MetaInfServices(ExecutableChange.class)
public class CreateProcedureAction extends AbstractAction<CreateProcedureChange> implements DbmsTargetedChange {

    public CreateProcedureAction() {
        super(new CreateProcedureChange());
    }

    public CreateProcedureAction(CreateProcedureChange change) {
        super(change);
    }

    @Override
    public boolean generateStatementsVolatile(Database database) {
        return false;
    }

    @Override
    public boolean generateRollbackStatementsVolatile(Database database) {
        return false;
    }

    public String getCatalogName() {
        return change.getCatalogName();
    }

    public void setCatalogName(String catalogName) {
        change.setCatalogName(catalogName);
    }

    public String getSchemaName() {
        return change.getSchemaName();
    }

    public void setSchemaName(String schemaName) {
        change.setSchemaName(schemaName);
    }

    @DatabaseChangeProperty(exampleValue = "new_customer")
    public String getProcedureName() {
        return change.getProcedureName();
    }

    public void setProcedureName(String procedureName) {
        change.setProcedureName(procedureName);
    }

    @DatabaseChangeProperty(exampleValue = "utf8")
    public String getEncoding() {
        return change.getEncoding();
    }

    public void setEncoding(String encoding) {
        change.setEncoding(encoding);
    }

    @DatabaseChangeProperty(description = "File containing the procedure text. Either this attribute or a nested procedure text is required.", exampleValue = "com/example/my-logic.sql")
    public String getPath() {
        return change.getPath();
    }

    public void setPath(String path) {
        change.setPath(path);
    }

    public Boolean isRelativeToChangelogFile() {
        return change.isRelativeToChangelogFile();
    }

    public void setRelativeToChangelogFile(Boolean relativeToChangelogFile) {
        change.setRelativeToChangelogFile(relativeToChangelogFile);
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
        return change.getProcedureBody();
    }

    /**
     * @deprecated Use setProcedureText() instead
     */
    @Deprecated
    public void setProcedureBody(String procedureText) {
        change.setProcedureBody(procedureText);
    }

    @DatabaseChangeProperty(isChangeProperty = false)
    public String getProcedureText() {
        return change.getProcedureText();
    }

    public void setProcedureText(String procedureText) {
        change.setProcedureText(procedureText);
    }

    @Override
    @DatabaseChangeProperty(since = "3.1", exampleValue = "h2, oracle")
	public String getDbms() {
		return change.getDbms();
	}

	@Override
    public void setDbms(final String dbms) {
		change.setDbms(dbms);
	}

    public String getComments() {
        return change.getComments();
    }

    public void setComments(String comments) {
        change.setComments(comments);
    }

    @DatabaseChangeProperty
    public Boolean getReplaceIfExists() {
        return change.getReplaceIfExists();
    }

    public void setReplaceIfExists(Boolean replaceIfExists) {
        change.setReplaceIfExists(replaceIfExists);
    }

    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors validate = new ValidationErrors(); //not falling back to default because of path/procedureText option group. Need to specify everything
        if (StringUtils.trimToNull(getProcedureText()) != null && StringUtils.trimToNull(getPath()) != null) {
            validate.addError("Cannot specify both 'path' and a nested procedure text in "+ExecutableChangeFactory.getInstance().getChangeMetaData(this).getName());
        }

        if (StringUtils.trimToNull(getProcedureText()) == null && StringUtils.trimToNull(getPath()) == null) {
            validate.addError("Cannot specify either 'path' or a nested procedure text in "+ExecutableChangeFactory.getInstance().getChangeMetaData(this).getName());
        }

        if (this.getReplaceIfExists() != null) {
            if (database instanceof MSSQLDatabase) {
                if (this.getReplaceIfExists() && this.getProcedureName() == null) {
                    validate.addError("procedureName is required if replaceIfExists = true");
                }
            } else {
                ValidationErrorHandler handler = new ValidationErrorHandler(validate);
                handler.checkDisallowedField("replaceIfExists", this.getReplaceIfExists(), database);
            }

        }


        return validate;
    }

    public InputStream openSqlStream() throws IOException {
        return change.openSqlStream();
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        String endDelimiter = ";";
        if (database instanceof OracleDatabase) {
            endDelimiter = "\n/";
        } else if (database instanceof DB2Database) {
            endDelimiter = "";
        }

        String procedureText;
        String path = getPath();
        if (path == null) {
            procedureText = StringUtils.trimToNull(getProcedureText());
        } else {
            try {
                InputStream stream = openSqlStream();
                if (stream == null) {
                    throw new IOException("File does not exist: "+path);
                }
                procedureText = StreamUtil.getStreamContents(stream, getEncoding());
            } catch (IOException e) {
                throw new UnexpectedLiquibaseException(e);
            }
        }
        return generateStatements(procedureText, endDelimiter, database);
    }

    protected SqlStatement[] generateStatements(String logicText, String endDelimiter, Database database) {
        CreateProcedureStatement statement = new CreateProcedureStatement(getCatalogName(), getSchemaName(), getProcedureName(), logicText, endDelimiter);
        statement.setReplaceIfExists(getReplaceIfExists());
        return new SqlStatement[]{
                statement,
        };
    }

    @Override
    public ChangeStatus checkStatus(Database database) {
        return new ChangeStatus().unknown("Cannot check createProcedure status");
    }

    @Override
    public String getConfirmationMessage() {
        return "Stored procedure created";
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

//    protected Map<String, Object> createExampleValueMetaData(String parameterName, DatabaseChangeProperty changePropertyAnnotation) {
//
//        if (parameterName.equals("procedureText") || parameterName.equals("procedureBody")) {
//            Map<String, Object> returnMap = super.createExampleValueMetaData(parameterName, changePropertyAnnotation);
//            returnMap.put(new HsqlDatabase().getShortName(), "CREATE PROCEDURE new_customer(firstname VARCHAR(50), lastname VARCHAR(50))\n" +
//                    "   MODIFIES SQL DATA\n" +
//                    "   INSERT INTO CUSTOMERS (first_name, last_name) VALUES (firstname, lastname)");
//
//            return returnMap;
//        } else {
//            return super.createExampleValueMetaData(parameterName, changePropertyAnnotation);
//        }
//    }
}
