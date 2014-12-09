package liquibase.change.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import liquibase.change.BaseChange;
import liquibase.change.Change;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.ResourceAccessor;
import liquibase.util.StringUtils;

import org.kohsuke.MetaInfServices;

/**
 * Executes a given shell executable.
 */
@DatabaseChange(name="executeCommand",
        description = "Executes a system command. Because this refactoring doesn't generate SQL like most, using LiquiBase commands such as migrateSQL may not work as expected. Therefore, if at all possible use refactorings that generate SQL.",
        priority = ChangeMetaData.PRIORITY_DEFAULT)
@MetaInfServices(Change.class)
public class ExecuteShellCommandChange extends BaseChange {

    private String executable;
    private List<String> os;
    private List<String> args = new ArrayList<String>();

    @DatabaseChangeProperty(description = "Name of the executable to run", exampleValue = "mysqldump", requiredForDatabase = "all")
    public String getExecutable() {
        return executable;
    }

    public void setExecutable(String executable) {
        this.executable = executable;
    }

    public void addArg(String arg) {
        this.args.add(arg);
    }

    public List<String> getArgs() {
        return Collections.unmodifiableList(args);
    }

    public void setOs(String os) {
        this.os = StringUtils.splitAndTrim(os, ",");
    }

    @DatabaseChangeProperty(description = "List of operating systems on which to execute the command (taken from the os.name Java system property)", exampleValue = "Windows 7")
    public List<String> getOs() {
        return os;
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    @Override
    protected void customLoadLogic(ParsedNode parsedNode, ResourceAccessor resourceAccessor) throws ParsedNodeException {
        ParsedNode argsNode = parsedNode.getChild(null, "args");
        if (argsNode == null) {
            argsNode = parsedNode;
        }

        for (ParsedNode arg : argsNode.getChildren(null, "arg")) {
            addArg(arg.getChildValue(null, "value", String.class));
        }
        List<String> os = StringUtils.splitAndTrim(StringUtils.trimToEmpty(parsedNode.getChildValue(null, "os", String.class)), ",");
        if (os.size() > 0) {
            this.os = os;
        }
    }
}
