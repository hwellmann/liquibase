package liquibase.action;

import java.math.BigInteger;

import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeStatus;
import liquibase.change.DatabaseChange;
import liquibase.change.ExecutableChange;
import liquibase.change.core.CreateSequenceChange;
import liquibase.database.Database;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.CreateSequenceStatement;
import liquibase.structure.core.Sequence;

import org.kohsuke.MetaInfServices;

/**
 * Creates a new sequence.
 */
@DatabaseChange(name="createSequence", description = "Creates a new database sequence", priority = ChangeMetaData.PRIORITY_DEFAULT)
@MetaInfServices(ExecutableChange.class)
public class CreateSequenceAction extends AbstractAction<CreateSequenceChange> {

    public CreateSequenceAction() {
        super(new CreateSequenceChange());
    }

    public CreateSequenceAction(CreateSequenceChange change) {
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

    public BigInteger getStartValue() {
        return change.getStartValue();
    }

    public void setStartValue(BigInteger startValue) {
        change.setStartValue(startValue);
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

    public Boolean getCycle() {
        return change.getCycle();
    }

    public void setCycle(Boolean cycle) {
        change.setCycle(cycle);
    }

    public BigInteger getCacheSize() {
        return change.getCacheSize();
    }

    public void setCacheSize(BigInteger cacheSize) {
        change.setCacheSize(cacheSize);
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[] {
                new CreateSequenceStatement(getCatalogName(), getSchemaName(), getSequenceName())
                .setIncrementBy(getIncrementBy())
                .setMaxValue(getMaxValue())
                .setMinValue(getMinValue())
                .setOrdered(isOrdered())
                .setStartValue(getStartValue())
                .setCycle(getCycle())
                .setCacheSize(getCacheSize())
        };
    }

    @Override
    public ChangeStatus checkStatus(Database database) {
        ChangeStatus result = new ChangeStatus();
        try {
            Sequence sequence = SnapshotGeneratorFactory.getInstance().createSnapshot(new Sequence(getCatalogName(), getSchemaName(), getSequenceName()), database);
            result.assertComplete(sequence != null, "Sequence " + getSequenceName() + " does not exist");
            if (sequence != null) {
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
                if (getCycle() != null) {
                    result.assertCorrect(getCycle().equals(sequence.getWillCycle()), "Will Cycle is different");
                }
                if (getCacheSize() != null) {
                    result.assertCorrect(getCacheSize().equals(sequence.getCacheSize()), "Cache size is different");
                }
            }
        } catch (Exception e) {
            return result.unknown(e);
        }
        return result;
    }

    @Override
    protected ExecutableChange[] createInverses() {
        DropSequenceAction inverse = new DropSequenceAction();
        inverse.setSequenceName(getSequenceName());
        inverse.setSchemaName(getSchemaName());

        return new ExecutableChange[]{
                inverse
        };
    }

    @Override
    public String getConfirmationMessage() {
        return "Sequence " + getSequenceName() + " created";
    }
}
