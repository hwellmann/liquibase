package liquibase.action;

import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.ExecutableChange;
import liquibase.change.core.RawSQLChange;

import org.kohsuke.MetaInfServices;

/**
 * Allows execution of arbitrary SQL.  This change can be used when existing changes are either don't exist,
 * are not flexible enough, or buggy.
 */
@DatabaseChange(name="sql",
        description = "The 'sql' tag allows you to specify whatever sql you want. It is useful for complex changes that aren't supported through Liquibase's automated refactoring tags and to work around bugs and limitations of Liquibase. The SQL contained in the sql tag can be multi-line.\n" +
        "\n" +
        "The createProcedure refactoring is the best way to create stored procedures.\n" +
        "\n" +
        "The 'sql' tag can also support multiline statements in the same file. Statements can either be split using a ; at the end of the last line of the SQL or a go on its own on the line between the statements can be used.Multiline SQL statements are also supported and only a ; or go statement will finish a statement, a new line is not enough. Files containing a single statement do not need to use a ; or go.\n" +
        "\n" +
        "The sql change can also contain comments of either of the following formats:\n" +
        "\n" +
        "A multiline comment that starts with /* and ends with */.\n" +
        "A single line comment starting with <space>--<space> and finishing at the end of the line\n" +
        "Note: By default it will attempt to split statements on a ';' or 'go' at the end of lines. Because of this, if you have a comment or some other non-statement ending ';' or 'go', don't have it at the end of a line or you will get invalid SQL.",
        priority = ChangeMetaData.PRIORITY_DEFAULT)
@MetaInfServices(ExecutableChange.class)
public class RawSQLAction extends AbstractSQLAction<RawSQLChange> {

    public RawSQLAction() {
        super(new RawSQLChange());
    }

    public RawSQLAction(RawSQLChange change) {
        super(change);
    }

    public RawSQLAction(String sql) {
        super(new RawSQLChange(sql));
    }


    public String getSql() {
        return change.getSql();
    }

    public void setSql(String sql) {
        change.setSql(sql);
    }

    public String getComment() {
        return change.getComment();
    }

    public void setComment(String comment) {
        change.setComment(comment);
    }

    @Override
    public String getConfirmationMessage() {
        return "Custom SQL executed";
    }
}