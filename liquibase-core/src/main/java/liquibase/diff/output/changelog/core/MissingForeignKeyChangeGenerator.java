package liquibase.diff.output.changelog.core;

import liquibase.action.AddForeignKeyConstraintAction;
import liquibase.change.ExecutableChange;
import liquibase.database.Database;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGenerator;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.MissingObjectChangeGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.ForeignKey;
import liquibase.structure.core.Index;
import liquibase.structure.core.PrimaryKey;
import liquibase.structure.core.Table;
import liquibase.structure.core.UniqueConstraint;
import liquibase.util.StringUtils;

import org.kohsuke.MetaInfServices;

@MetaInfServices(ChangeGenerator.class)
public class MissingForeignKeyChangeGenerator implements MissingObjectChangeGenerator {
    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (ForeignKey.class.isAssignableFrom(objectType)) {
            return PRIORITY_DEFAULT;
        }
        return PRIORITY_NONE;
    }

    @Override
    public Class<? extends DatabaseObject>[] runAfterTypes() {
        return new Class[] {
                Table.class,
                Column.class,
                PrimaryKey.class,
                UniqueConstraint.class
        };
    }

    @Override
    public Class<? extends DatabaseObject>[] runBeforeTypes() {
        return new Class[] { Index.class };
    }

    @Override
    public ExecutableChange[] fixMissing(DatabaseObject missingObject, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
        ForeignKey fk = (ForeignKey) missingObject;

        AddForeignKeyConstraintAction action = new AddForeignKeyConstraintAction();
        action.setConstraintName(fk.getName());

        action.setReferencedTableName(fk.getPrimaryKeyTable().getName());
        if (!((ForeignKey) missingObject).getPrimaryKeyTable().getSchema().equals(((ForeignKey) missingObject).getForeignKeyTable().getSchema()) || control.getIncludeCatalog()) {
            action.setReferencedTableCatalogName(fk.getPrimaryKeyTable().getSchema().getCatalogName());
        }
        if (!((ForeignKey) missingObject).getPrimaryKeyTable().getSchema().equals(((ForeignKey) missingObject).getForeignKeyTable().getSchema()) || control.getIncludeSchema()) {
            action.setReferencedTableSchemaName(fk.getPrimaryKeyTable().getSchema().getName());
        }
        action.setReferencedColumnNames(StringUtils.join(fk.getPrimaryKeyColumns(), ",", new StringUtils.StringUtilsFormatter<Column>() {
            @Override
            public String toString(Column obj) {
                return obj.getName();
            }
        }));

        action.setBaseTableName(fk.getForeignKeyTable().getName());
        if (control.getIncludeCatalog()) {
            action.setBaseTableCatalogName(fk.getForeignKeyTable().getSchema().getCatalogName());
        }
        if (control.getIncludeSchema()) {
            action.setBaseTableSchemaName(fk.getForeignKeyTable().getSchema().getName());
        }
        action.setBaseColumnNames(StringUtils.join(fk.getForeignKeyColumns(), ",", new StringUtils.StringUtilsFormatter<Column>() {
            @Override
            public String toString(Column obj) {
                return obj.getName();
            }
        }));

        action.setDeferrable(fk.isDeferrable());
        action.setInitiallyDeferred(fk.isInitiallyDeferred());
        action.setOnUpdate(fk.getUpdateRule());
        action.setOnDelete(fk.getDeleteRule());

        Index backingIndex = fk.getBackingIndex();
//        if (backingIndex == null) {
//            Index exampleIndex = new Index().setTable(fk.getForeignKeyTable());
//            for (String col : fk.getForeignKeyColumns().split("\\s*,\\s*")) {
//                exampleIndex.getColumns().add(col);
//            }
//            control.setAlreadyHandledMissing(exampleIndex);
//        } else {
            control.setAlreadyHandledMissing(backingIndex);
//        }

        return new ExecutableChange[] { action };
    }
}
