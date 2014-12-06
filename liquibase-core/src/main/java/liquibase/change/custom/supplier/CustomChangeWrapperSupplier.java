package liquibase.change.custom.supplier;

import liquibase.change.IChange;
import liquibase.change.custom.CustomChangeWrapper;
import liquibase.diff.DiffResult;
import liquibase.sdk.supplier.change.AbstractChangeSupplier;

public class CustomChangeWrapperSupplier extends AbstractChangeSupplier<CustomChangeWrapper> {

    public CustomChangeWrapperSupplier() {
        super(CustomChangeWrapper.class);
    }

    @Override
    public IChange[] prepareDatabase(CustomChangeWrapper change) throws Exception {

        return new IChange[0];
    }

    @Override
    public void checkDiffResult(DiffResult diffResult, CustomChangeWrapper change) {
    }
}

