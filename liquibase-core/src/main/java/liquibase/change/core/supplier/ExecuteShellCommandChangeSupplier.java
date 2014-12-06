package liquibase.change.core.supplier;

import liquibase.change.IChange;
import liquibase.change.core.ExecuteShellCommandChange;
import liquibase.diff.DiffResult;
import liquibase.sdk.supplier.change.AbstractChangeSupplier;

public class ExecuteShellCommandChangeSupplier extends AbstractChangeSupplier<ExecuteShellCommandChange> {

    public ExecuteShellCommandChangeSupplier() {
        super(ExecuteShellCommandChange.class);
    }

    @Override
    public IChange[] prepareDatabase(ExecuteShellCommandChange change) throws Exception {
        return null;
    }

    @Override
    public void checkDiffResult(DiffResult diffResult, ExecuteShellCommandChange change) throws Exception {
    }
}