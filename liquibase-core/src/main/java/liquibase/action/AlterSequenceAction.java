package liquibase.action;

import java.math.BigInteger;

import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeStatus;
import liquibase.change.DatabaseChange;
import liquibase.change.ExecutableChange;
import liquibase.change.core.AlterSequenceChange;
import liquibase.database.Database;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AlterSequenceStatement;
import liquibase.structure.core.Sequence;

import org.kohsuke.MetaInfServices;

/**
 * Modifies properties of an existing sequence. StartValue is not allowed since we cannot alter the starting sequence number
 */
@DatabaseChange(name="alterSequence", description = "Alter properties of an existing sequence", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "sequence")
@MetaInfServices(ExecutableChange.class)
public class AlterSequenceAction extends AbstractAction<AlterSequenceChange> {

    public AlterSequenceAction() {
        super(new AlterSequenceChange());
    }

    public AlterSequenceAction(AlterSequenceChange change) {
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


    public BigInteger getIncrementBy() {
        return change.getIncrementBy();
    }

    public void setIncrementBy(BigInteger incrementBy) {
        change.setIncrementBy(incrementBy);
    }

    public BigInteger getMaxValue() {
        return change.getMaxValue();
    }

    public void setMaxValue(BigInteger maxValue) {
        change.setMaxValue(maxValue);
    }

    public BigInteger getMinValue() {
        return change.getMinValue();
    }

    public void setMinValue(BigInteger minValue) {
        change.setMinValue(minValue);
    }

    public Boolean isOrdered() {
        return change.isOrdered();
    }

    public void setOrdered(Boolean ordered) {
        change.setOrdered(ordered);
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[] {
                new AlterSequenceStatement(getCatalogName(), getSchemaName(), getSequenceName())
                .setIncrementBy(getIncrementBy())
                .setMaxValue(getMaxValue())
                .setMinValue(getMinValue())
                .setOrdered(isOrdered())
        };
    }

    @Override
    public ChangeStatus checkStatus(Database database) {
        ChangeStatus result = new ChangeStatus();
        try {
            Sequence sequence = SnapshotGeneratorFactory.getInstance().createSnapshot(new Sequence(getCatalogName(), getSchemaName(), getSequenceName()), database);
            if (sequence == null) {
                return result.unknown("Sequence " + getSequenceName() + " does not exist");
            }

            if (getIncrementBy() != null) {
                result.assertCorrect(getIncrementBy().equals(sequence.getIncrementBy()), "Increment by has a different value");
            }
            if (getMinValue() != null) {
                result.assertCorrect(getMinValue().equals(sequence.getMinValue()), "Min Value is different");
            }
            if (getMaxValue() != null) {
                result.assertCorrect(getMaxValue().equals(sequence.getMaxValue()), "Max Value is different");
            }
            if (isOrdered() != null) {
                result.assertCorrect(isOrdered().equals(sequence.getOrdered()), "Max Value is different");
            }
        } catch (Exception e) {
            return result.unknown(e);
        }
        return result;
    }

    @Override
    public String getConfirmationMessage() {
        return "Sequence " + getSequenceName() + " altered";
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}
