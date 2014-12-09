package liquibase.action;

import java.util.ArrayList;
import java.util.List;

import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.change.ExecutableChange;
import liquibase.change.core.RenameSequenceChange;
import liquibase.database.Database;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.RenameSequenceStatement;

import org.kohsuke.MetaInfServices;

/**
 * Renames an existing table.
 */
@DatabaseChange(name="renameSequence", description = "Renames an existing sequence", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "sequence")
@MetaInfServices(ExecutableChange.class)
public class RenameSequenceAction extends AbstractAction<RenameSequenceChange> {

    public RenameSequenceAction() {
        super(new RenameSequenceChange());
    }

    public RenameSequenceAction(RenameSequenceChange change) {
        super(change);
    }

    public String getCatalogName() {
        return change.getCatalogName();
    }

    public void setCatalogName(String catalogName) {
        change.setCatalogName(catalogName);
    }

    @DatabaseChangeProperty(mustEqualExisting ="sequence.schema")
    public String getSchemaName() {
        return change.getSchemaName();
    }

    public void setSchemaName(String schemaName) {
        change.setSchemaName(schemaName);
    }

    @DatabaseChangeProperty(mustEqualExisting = "sequence", description = "Name of the sequence to rename")
    public String getOldSequenceName() {
        return change.getOldSequenceName();
    }

    public void setOldSequenceName(String oldSequenceName) {
        change.setOldSequenceName(oldSequenceName);
    }

    @DatabaseChangeProperty(description = "New name for the sequence")
    public String getNewSequenceName() {
        return change.getNewSequenceName();
    }

    public void setNewSequenceName(String newSequenceName) {
        change.setNewSequenceName(newSequenceName);
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        List<SqlStatement> statements = new ArrayList<SqlStatement>();
        statements.add(new RenameSequenceStatement(getCatalogName(), getSchemaName(), getOldSequenceName(), getNewSequenceName()));
        return statements.toArray(new SqlStatement[statements.size()]);
    }

    @Override
    protected ExecutableChange[] createInverses() {
        RenameSequenceAction inverse = new RenameSequenceAction();
        inverse.setSchemaName(getSchemaName());
        inverse.setOldSequenceName(getNewSequenceName());
        inverse.setNewSequenceName(getOldSequenceName());

        return new ExecutableChange[]{
                inverse
        };
    }

    @Override
    public String getConfirmationMessage() {
        return "Sequence " + getOldSequenceName() + " renamed to " + getNewSequenceName();
    }
}
