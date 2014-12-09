package liquibase.diff.output.changelog.core;

import liquibase.action.AddForeignKeyConstraintAction;
import liquibase.action.DropForeignKeyConstraintAction;
import liquibase.change.ExecutableChange;
import liquibase.database.Database;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGenerator;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.ChangedObjectChangeGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.ForeignKey;
import liquibase.structure.core.Index;
import liquibase.structure.core.UniqueConstraint;
import liquibase.util.StringUtils;

import org.kohsuke.MetaInfServices;

@MetaInfServices(ChangeGenerator.class)
public class ChangedForeignKeyChangeGenerator implements ChangedObjectChangeGenerator {
    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (ForeignKey.class.isAssignableFrom(objectType)) {
            return PRIORITY_DEFAULT;
        }
        return PRIORITY_NONE;
    }

    @Override
    public Class<? extends DatabaseObject>[] runBeforeTypes() {
        return new Class[] {Index.class, UniqueConstraint.class };
    }

    @Override
    public Class<? extends DatabaseObject>[] runAfterTypes() {
        return null;
    }

    @Override
    public ExecutableChange[] fixChanged(DatabaseObject changedObject, ObjectDifferences differences, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
        ForeignKey fk = (ForeignKey) changedObject;

        StringUtils.StringUtilsFormatter formatter = new StringUtils.StringUtilsFormatter<Column>() {
            @Override
            public String toString(Column obj) {
                return obj.toString(false);
            }
        };

        DropForeignKeyConstraintAction dropFk = new DropForeignKeyConstraintAction();
        dropFk.setConstraintName(fk.getName());
        dropFk.setBaseTableName(fk.getForeignKeyTable().getName());

        AddForeignKeyConstraintAction addFkAction = new AddForeignKeyConstraintAction();
        addFkAction.setConstraintName(fk.getName());
        addFkAction.setBaseTableName(fk.getForeignKeyTable().getName());
        addFkAction.setBaseColumnNames(StringUtils.join(fk.getForeignKeyColumns(), ",", formatter));
        addFkAction.setReferencedTableName(fk.getPrimaryKeyTable().getName());
        addFkAction.setReferencedColumnNames(StringUtils.join(fk.getPrimaryKeyColumns(), ",", formatter));

        if (control.getIncludeCatalog()) {
            dropFk.setBaseTableCatalogName(fk.getForeignKeyTable().getSchema().getCatalogName());

            addFkAction.setBaseTableCatalogName(fk.getForeignKeyTable().getSchema().getCatalogName());
            addFkAction.setReferencedTableCatalogName(fk.getPrimaryKeyTable().getSchema().getCatalogName());
        }
        if (control.getIncludeSchema()) {
            dropFk.setBaseTableSchemaName(fk.getForeignKeyTable().getSchema().getName());

            addFkAction.setBaseTableSchemaName(fk.getForeignKeyTable().getSchema().getName());
            addFkAction.setReferencedTableSchemaName(fk.getPrimaryKeyTable().getSchema().getName());
        }

        if (fk.getBackingIndex() != null) {
            control.setAlreadyHandledChanged(fk.getBackingIndex());
        }

        return new ExecutableChange[] { dropFk, addFkAction };
    }
}
