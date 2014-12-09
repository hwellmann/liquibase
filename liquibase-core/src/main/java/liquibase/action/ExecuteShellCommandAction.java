package liquibase.action;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.change.ExecutableChange;
import liquibase.change.core.ExecuteShellCommandChange;
import liquibase.database.Database;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.executor.LoggingExecutor;
import liquibase.logging.LogFactory;
import liquibase.sql.Sql;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.CommentStatement;
import liquibase.statement.core.RuntimeStatement;
import liquibase.util.StreamUtil;
import liquibase.util.StringUtils;

import org.kohsuke.MetaInfServices;

/**
 * Executes a given shell executable.
 */
@DatabaseChange(name="executeCommand",
        description = "Executes a system command. Because this refactoring doesn't generate SQL like most, using LiquiBase commands such as migrateSQL may not work as expected. Therefore, if at all possible use refactorings that generate SQL.",
        priority = ChangeMetaData.PRIORITY_DEFAULT)
@MetaInfServices(ExecutableChange.class)
public class ExecuteShellCommandAction extends AbstractAction<ExecuteShellCommandChange> {

    public ExecuteShellCommandAction() {
        super(new ExecuteShellCommandChange());
    }

    public ExecuteShellCommandAction(ExecuteShellCommandChange change) {
        super(change);
    }

    @Override
    public boolean generateStatementsVolatile(Database database) {
        return true;
    }

    @Override
    public boolean generateRollbackStatementsVolatile(Database database) {
        return true;
    }

    public String getExecutable() {
        return change.getExecutable();
    }

    public void setExecutable(String executable) {
        change.setExecutable(executable);
    }

    public void addArg(String arg) {
        change.addArg(arg);
    }

    public List<String> getArgs() {
        return change.getArgs();
    }

    public void setOs(String os) {
        change.setOs(os);
    }

    @DatabaseChangeProperty(description = "List of operating systems on which to execute the command (taken from the os.name Java system property)", exampleValue = "Windows 7")
    public List<String> getOs() {
        return change.getOs();
    }

    @Override
    public ValidationErrors validate(Database database) {
        return new ValidationErrors();
    }


    @Override
    public Warnings warn(Database database) {
        return new Warnings();
    }

    @Override
    public SqlStatement[] generateStatements(final Database database) {
        boolean shouldRun = true;
        List<String> os = getOs();
        if (os != null && os.size() > 0) {
            String currentOS = System.getProperty("os.name");
            if (!os.contains(currentOS)) {
                shouldRun = false;
                LogFactory.getLogger().info("Not executing on os "+currentOS+" when "+os+" was specified");
            }
        }

    	// check if running under not-executed mode (logging output)
        boolean nonExecutedMode = false;
        Executor executor = ExecutorService.getInstance().getExecutor(database);
        if (executor instanceof LoggingExecutor) {
            nonExecutedMode = true;
        }

        if (shouldRun && !nonExecutedMode) {


            return new SqlStatement[]{new RuntimeStatement() {

                @Override
                public Sql[] generate(Database database) {
                    List<String> commandArray = new ArrayList<String>();
                    commandArray.add(getExecutable());
                    commandArray.addAll(getArgs());

                    try {
                        ProcessBuilder pb = new ProcessBuilder(commandArray);
                        pb.redirectErrorStream(true);
                        Process p = pb.start();
                        int returnCode = 0;
                        try {
                            returnCode = p.waitFor();
                        } catch (InterruptedException e) {
                            ;
                        }

                        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
                        ByteArrayOutputStream inputStream = new ByteArrayOutputStream();
                        StreamUtil.copy(p.getErrorStream(), errorStream);
                        StreamUtil.copy(p.getInputStream(), inputStream);

                        LogFactory.getLogger().severe(errorStream.toString());
                        LogFactory.getLogger().info(inputStream.toString());

                        if (returnCode != 0) {
                            throw new RuntimeException(getCommandString() + " returned an code of " + returnCode);
                        }
                    } catch (IOException e) {
                        throw new UnexpectedLiquibaseException("Error executing command: " + e);
                    }

                    return null;
                }
            }};
        }

        if (nonExecutedMode) {
        	return new SqlStatement[] {
        			new CommentStatement(getCommandString())
        	};
        }

        return new SqlStatement[0];
    }

    @Override
    public String getConfirmationMessage() {
        return "Shell command '" + getCommandString() + "' executed";
    }

    private String getCommandString() {
        return getExecutable() + " " + StringUtils.join(getArgs(), " ");
    }
}
