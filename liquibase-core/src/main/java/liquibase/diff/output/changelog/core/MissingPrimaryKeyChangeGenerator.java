package liquibase.diff.output.changelog.core;

import liquibase.action.AddPrimaryKeyAction;
import liquibase.change.ExecutableChange;
import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGenerator;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.MissingObjectChangeGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.Index;
import liquibase.structure.core.PrimaryKey;
import liquibase.structure.core.Table;

import org.kohsuke.MetaInfServices;

@MetaInfServices(ChangeGenerator.class)
public class MissingPrimaryKeyChangeGenerator implements MissingObjectChangeGenerator {

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (PrimaryKey.class.isAssignableFrom(objectType)) {
            return PRIORITY_DEFAULT;
        }
        return PRIORITY_NONE;

    }

    @Override
    public Class<? extends DatabaseObject>[] runAfterTypes() {
        return new Class[] {
                Table.class,
                Column.class
        };

    }

    @Override
    public Class<? extends DatabaseObject>[] runBeforeTypes() {
        return new Class[] {
                Index.class
        };
    }

    @Override
    public ExecutableChange[] fixMissing(DatabaseObject missingObject, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
        PrimaryKey pk = (PrimaryKey) missingObject;

        AddPrimaryKeyAction action = new AddPrimaryKeyAction();
        action.setTableName(pk.getTable().getName());
        if (control.getIncludeCatalog()) {
            action.setCatalogName(pk.getTable().getSchema().getCatalogName());
        }
        if (control.getIncludeSchema()) {
            action.setSchemaName(pk.getTable().getSchema().getName());
        }
        action.setConstraintName(pk.getName());
        action.setColumnNames(pk.getColumnNames());
        if (control.getIncludeTablespace()) {
            action.setTablespace(pk.getTablespace());
        }

        if (referenceDatabase instanceof MSSQLDatabase && pk.getBackingIndex() != null && pk.getBackingIndex().getClustered() != null && !pk.getBackingIndex().getClustered()) {
            action.setClustered(false);
        }

        control.setAlreadyHandledMissing(pk.getBackingIndex());

        return new ExecutableChange[] { action };

    }
}
