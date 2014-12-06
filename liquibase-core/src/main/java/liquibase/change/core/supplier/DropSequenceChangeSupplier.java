package liquibase.change.core.supplier;

import liquibase.change.IChange;
import liquibase.change.core.CreateSequenceChange;
import liquibase.change.core.DropSequenceChange;
import liquibase.diff.DiffResult;
import liquibase.exception.DatabaseException;
import liquibase.sdk.supplier.change.AbstractChangeSupplier;
import liquibase.structure.core.Sequence;
import static liquibase.Assert.assertNotNull;

public class DropSequenceChangeSupplier extends AbstractChangeSupplier<DropSequenceChange>  {

    public DropSequenceChangeSupplier() {
        super(DropSequenceChange.class);
    }

    @Override
    public IChange[]  prepareDatabase(DropSequenceChange change) throws DatabaseException {
        CreateSequenceChange createSequenceChange = new CreateSequenceChange();
        createSequenceChange.setCatalogName(change.getCatalogName());
        createSequenceChange.setSchemaName(change.getSchemaName());
        createSequenceChange.setSequenceName(change.getSequenceName());

        return new IChange[] {createSequenceChange };
    }

    @Override
    public void checkDiffResult(DiffResult diffResult, DropSequenceChange change) {
        assertNotNull(diffResult.getMissingObject(new Sequence(change.getCatalogName(), change.getSchemaName(), change.getSequenceName())));
    }
}
