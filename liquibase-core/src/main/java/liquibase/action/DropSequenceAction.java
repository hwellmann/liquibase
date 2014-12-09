package liquibase.action;

import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeStatus;
import liquibase.change.DatabaseChange;
import liquibase.change.ExecutableChange;
import liquibase.change.core.DropSequenceChange;
import liquibase.database.Database;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropSequenceStatement;
import liquibase.structure.core.Sequence;

import org.kohsuke.MetaInfServices;

/**
 * Drops an existing sequence.
 */
@DatabaseChange(name="dropSequence", description = "Drop an existing sequence", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "sequence")
@MetaInfServices(ExecutableChange.class)
public class DropSequenceAction extends AbstractAction<DropSequenceChange> {

    public DropSequenceAction() {
        super(new DropSequenceChange());
    }

    public DropSequenceAction(DropSequenceChange change) {
        super(change);
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

    public String getSequenceName() {
        return change.getSequenceName();
    }

    public void setSequenceName(String sequenceName) {
        change.setSequenceName(sequenceName);
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[]{new DropSequenceStatement(getCatalogName(), getSchemaName(), getSequenceName())};
    }

    @Override
    public ChangeStatus checkStatus(Database database) {
        try {
            return new ChangeStatus().assertComplete(!SnapshotGeneratorFactory.getInstance().has(new Sequence(getCatalogName(), getSchemaName(), getSequenceName()), database), "Sequence exists");
        } catch (Exception e) {
            return new ChangeStatus().unknown(e);
        }
    }


    @Override
    public String getConfirmationMessage() {
        return "Sequence " + getSequenceName() + " dropped";
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}
