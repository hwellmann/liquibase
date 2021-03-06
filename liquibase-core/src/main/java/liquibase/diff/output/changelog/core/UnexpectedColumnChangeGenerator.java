package liquibase.diff.output.changelog.core;

import liquibase.action.DropColumnAction;
import liquibase.change.ExecutableChange;
import liquibase.change.core.DropColumnChange;
import liquibase.database.Database;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGenerator;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.UnexpectedObjectChangeGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.ForeignKey;
import liquibase.structure.core.PrimaryKey;
import liquibase.structure.core.Table;
import liquibase.structure.core.View;

import org.kohsuke.MetaInfServices;

@MetaInfServices(ChangeGenerator.class)
public class UnexpectedColumnChangeGenerator implements UnexpectedObjectChangeGenerator {
    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (Column.class.isAssignableFrom(objectType)) {
            return PRIORITY_DEFAULT;
        }
        return PRIORITY_NONE;
    }

    @Override
    public Class<? extends DatabaseObject>[] runAfterTypes() {
        return new Class[] {
                PrimaryKey.class,
                ForeignKey.class,
                Table.class,
        };
    }

    @Override
    public Class<? extends DatabaseObject>[] runBeforeTypes() {
        return null;
    }

    @Override
    public ExecutableChange[] fixUnexpected(DatabaseObject unexpectedObject, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
        Column column = (Column) unexpectedObject;
//        if (!shouldModifyColumn(column)) {
//            continue;
//        }
        if (column.getRelation() instanceof View) {
            return null;
        }

        if (column.getRelation().getSnapshotId() == null) { //not an actual table, maybe an alias, maybe in a different schema. Don't fix it.
            return null;
        }

        DropColumnAction action = new DropColumnAction();
        DropColumnChange change = action.getChange();
        change.setTableName(column.getRelation().getName());
        if (control.getIncludeCatalog()) {
            change.setCatalogName(column.getRelation().getSchema().getCatalogName());
        }
        if (control.getIncludeSchema()) {
            change.setSchemaName(column.getRelation().getSchema().getName());
        }
        change.setColumnName(column.getName());

        // FIXME
        return new ExecutableChange[] { null /*action*/ };

    }
}
