package liquibase.change.core.supplier;

import liquibase.change.IChange;
import liquibase.change.core.EmptyChange;
import liquibase.diff.DiffResult;
import liquibase.sdk.supplier.change.AbstractChangeSupplier;
import static liquibase.Assert.assertEquals;

public class EmptyChangeSupplier extends AbstractChangeSupplier<EmptyChange>  {

    public EmptyChangeSupplier() {
        super(EmptyChange.class);
    }

    @Override
    public IChange[] prepareDatabase(EmptyChange change) throws Exception {
        return null;
    }

    @Override
    public void checkDiffResult(DiffResult diffResult, EmptyChange change) throws Exception {
        assertEquals(0, diffResult.getMissingObjects().size());
        assertEquals(0, diffResult.getUnexpectedObjects().size());
        assertEquals(0, diffResult.getChangedObjects().size());
    }
}
