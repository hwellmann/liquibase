package liquibase.diff.output.changelog;

import liquibase.change.ExecutableChange;
import liquibase.database.Database;
import liquibase.diff.output.DiffOutputControl;
import liquibase.structure.DatabaseObject;

public interface MissingObjectChangeGenerator extends ChangeGenerator {

    public ExecutableChange[] fixMissing(DatabaseObject missingObject, DiffOutputControl control, Database referenceDatabase, Database comparisionDatabase, ChangeGeneratorChain chain);
}
