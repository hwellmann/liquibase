package liquibase.diff.output.changelog.core;

import liquibase.action.AddUniqueConstraintAction;
import liquibase.change.ExecutableChange;
import liquibase.database.Database;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGenerator;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.MissingObjectChangeGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.Index;
import liquibase.structure.core.Table;
import liquibase.structure.core.UniqueConstraint;

import org.kohsuke.MetaInfServices;

@MetaInfServices(ChangeGenerator.class)
public class MissingUniqueConstraintChangeGenerator implements MissingObjectChangeGenerator {
    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (UniqueConstraint.class.isAssignableFrom(objectType)) {
            return PRIORITY_DEFAULT;
        }
        return PRIORITY_NONE;
    }

    @Override
    public Class<? extends DatabaseObject>[] runAfterTypes() {
        return new Class[]{
                Table.class,
                Column.class
        };
    }

    @Override
    public Class<? extends DatabaseObject>[] runBeforeTypes() {
        return new Class[]{Index.class};
    }

    @Override
    public ExecutableChange[] fixMissing(DatabaseObject missingObject, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
        UniqueConstraint uc = (UniqueConstraint) missingObject;

        if (uc.getTable() == null) {
            return null;
        }

        AddUniqueConstraintAction action = new AddUniqueConstraintAction();
        action.setTableName(uc.getTable().getName());
        if (uc.getBackingIndex() != null && control.getIncludeTablespace()) {
            action.setTablespace(uc.getBackingIndex().getTablespace());
        }
        if (control.getIncludeCatalog()) {
            action.setCatalogName(uc.getTable().getSchema().getCatalogName());
        }
        if (control.getIncludeSchema()) {
            action.setSchemaName(uc.getTable().getSchema().getName());
        }
        action.setConstraintName(uc.getName());
        action.setColumnNames(uc.getColumnNames());
        action.setDeferrable(uc.isDeferrable());
        action.setInitiallyDeferred(uc.isInitiallyDeferred());
        action.setDisabled(uc.isDisabled());

        Index backingIndex = uc.getBackingIndex();
//        if (backingIndex == null) {
//            Index exampleIndex = new Index().setTable(uc.getTable());
//            for (String col : uc.getColumns()) {
//                exampleIndex.getColumns().add(col);
//            }
//            control.setAlreadyHandledMissing(exampleIndex);
//        } else {
            control.setAlreadyHandledMissing(backingIndex);
//        }


        return new ExecutableChange[]{action};


    }
}
