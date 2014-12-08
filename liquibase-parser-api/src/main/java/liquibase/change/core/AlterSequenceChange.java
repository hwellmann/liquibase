package liquibase.change.core;

import java.math.BigInteger;

import liquibase.change.BaseChange;
import liquibase.change.Change;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;

import org.kohsuke.MetaInfServices;

/**
 * Modifies properties of an existing sequence. StartValue is not allowed since we cannot alter the starting sequence number
 */
@DatabaseChange(name="alterSequence", description = "Alter properties of an existing sequence", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "sequence")
@MetaInfServices(Change.class)
public class AlterSequenceChange extends BaseChange {

    private String catalogName;
    private String schemaName;
    private String sequenceName;
    private BigInteger incrementBy;
    private BigInteger maxValue;
    private BigInteger minValue;
    private Boolean ordered;

    @DatabaseChangeProperty(mustEqualExisting ="sequence.catalog", since = "3.0")
    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    @DatabaseChangeProperty(mustEqualExisting ="sequence.schema")
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "sequence")
    public String getSequenceName() {
        return sequenceName;
    }

    public void setSequenceName(String sequenceName) {
        this.sequenceName = sequenceName;
    }


    @DatabaseChangeProperty(description = "New amount the sequence should increment by")
    public BigInteger getIncrementBy() {
        return incrementBy;
    }

    public void setIncrementBy(BigInteger incrementBy) {
        this.incrementBy = incrementBy;
    }

    @DatabaseChangeProperty(description = "New maximum value for the sequence")
    public BigInteger getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(BigInteger maxValue) {
        this.maxValue = maxValue;
    }

    @DatabaseChangeProperty(description = "New minimum value for the sequence")
    public BigInteger getMinValue() {
        return minValue;
    }

    public void setMinValue(BigInteger minValue) {
        this.minValue = minValue;
    }

    @DatabaseChangeProperty(description = "Does the sequence need to be guaranteed to be genererated inm the order of request?")
    public Boolean isOrdered() {
        return ordered;
    }

    public void setOrdered(Boolean ordered) {
        this.ordered = ordered;
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}
