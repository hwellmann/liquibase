package liquibase.diff.output.changelog.core;

import liquibase.action.DropPrimaryKeyAction;
import liquibase.change.ExecutableChange;
import liquibase.database.Database;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGenerator;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.UnexpectedObjectChangeGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Index;
import liquibase.structure.core.PrimaryKey;

import org.kohsuke.MetaInfServices;

@MetaInfServices(ChangeGenerator.class)
public class UnexpectedPrimaryKeyChangeGenerator implements UnexpectedObjectChangeGenerator {
    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (PrimaryKey.class.isAssignableFrom(objectType)) {
            return PRIORITY_DEFAULT;
        }
        return PRIORITY_NONE;
    }

    @Override
    public Class<? extends DatabaseObject>[] runAfterTypes() {
        return null;
       }

    @Override
    public Class<? extends DatabaseObject>[] runBeforeTypes() {
        return new Class[] {
                Index.class
        };
    }

    @Override
    public ExecutableChange[] fixUnexpected(DatabaseObject unexpectedObject, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
//        if (!diffResult.getObjectDiff(Table.class).getUnexpected().contains(pk.getTable())) {
        PrimaryKey pk = (PrimaryKey) unexpectedObject;
        DropPrimaryKeyAction action = new DropPrimaryKeyAction();
        action.setTableName(pk.getTable().getName());
        if (control.getIncludeCatalog()) {
            action.setCatalogName(pk.getTable().getSchema().getCatalogName());
        }
        if (control.getIncludeSchema()) {
            action.setSchemaName(pk.getTable().getSchema().getName());
        }
        action.setConstraintName(pk.getName());

        Index backingIndex = pk.getBackingIndex();
        control.setAlreadyHandledUnexpected(backingIndex);


        return new ExecutableChange[] { action };
//        }

    }
}
