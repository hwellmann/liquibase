package liquibase.diff;

import liquibase.database.Database;
import liquibase.diff.compare.CompareControl;
import liquibase.exception.DatabaseException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.util.PrioritizedService;

public interface DiffGenerator extends PrioritizedService {
    DiffResult compare(DatabaseSnapshot referenceSnapshot, DatabaseSnapshot comparisonSnapshot, CompareControl compareControl) throws DatabaseException;

    boolean supports(Database referenceDatabase, Database comparisonDatabase);

}
