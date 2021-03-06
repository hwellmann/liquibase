package liquibase.diff.output.changelog.core;

import liquibase.action.DropUniqueConstraintAction;
import liquibase.change.ExecutableChange;
import liquibase.database.Database;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGenerator;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.UnexpectedObjectChangeGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Index;
import liquibase.structure.core.Table;
import liquibase.structure.core.UniqueConstraint;

import org.kohsuke.MetaInfServices;

@MetaInfServices(ChangeGenerator.class)
public class UnexpectedUniqueConstraintChangeGenerator implements UnexpectedObjectChangeGenerator {
    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (UniqueConstraint.class.isAssignableFrom(objectType)) {
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
                Table.class,
                Index.class
        };
    }

    @Override
    public ExecutableChange[] fixUnexpected(DatabaseObject unexpectedObject, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
        UniqueConstraint uc = (UniqueConstraint) unexpectedObject;
        if (uc.getTable() == null) {
            return null;
        }

        DropUniqueConstraintAction action = new DropUniqueConstraintAction();
        action.setTableName(uc.getTable().getName());
        if (control.getIncludeCatalog()) {
            action.setCatalogName(uc.getTable().getSchema().getCatalogName());
        }
        if (control.getIncludeSchema()) {
            action.setSchemaName(uc.getTable().getSchema().getName());
        }
        action.setConstraintName(uc.getName());

        Index backingIndex = uc.getBackingIndex();
//        if (backingIndex == null) {
//            Index exampleIndex = new Index().setTable(uc.getTable());
//            for (String col : uc.getColumns()) {
//                exampleIndex.getColumns().add(col);
//            }
//            control.setAlreadyHandledUnexpected(exampleIndex);
//        } else {
            control.setAlreadyHandledUnexpected(backingIndex);
//        }

        return new ExecutableChange[] { action };
    }
}
