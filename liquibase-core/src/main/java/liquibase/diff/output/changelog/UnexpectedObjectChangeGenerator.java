package liquibase.diff.output.changelog;

import liquibase.change.ExecutableChange;
import liquibase.database.Database;
import liquibase.diff.output.DiffOutputControl;
import liquibase.structure.DatabaseObject;

public interface UnexpectedObjectChangeGenerator extends ChangeGenerator {

    public ExecutableChange[] fixUnexpected(DatabaseObject unexpectedObject, DiffOutputControl control, Database referenceDatabase, Database comparisionDatabase, ChangeGeneratorChain chain);
}
