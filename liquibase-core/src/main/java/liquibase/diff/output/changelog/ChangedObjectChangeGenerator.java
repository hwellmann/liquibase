package liquibase.diff.output.changelog;

import liquibase.change.ExecutableChange;
import liquibase.database.Database;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.output.DiffOutputControl;
import liquibase.structure.DatabaseObject;

public interface ChangedObjectChangeGenerator extends ChangeGenerator {

    public ExecutableChange[] fixChanged(DatabaseObject changedObject, ObjectDifferences differences, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain);
}
