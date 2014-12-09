package liquibase.diff.output.changelog.core;

import liquibase.action.DropViewAction;
import liquibase.change.ExecutableChange;
import liquibase.database.Database;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGenerator;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.UnexpectedObjectChangeGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;
import liquibase.structure.core.View;

import org.kohsuke.MetaInfServices;

@MetaInfServices(ChangeGenerator.class)
public class UnexpectedViewChangeGenerator implements UnexpectedObjectChangeGenerator {
    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (View.class.isAssignableFrom(objectType)) {
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
                Column.class
        };
    }

    @Override
    public ExecutableChange[] fixUnexpected(DatabaseObject unexpectedObject, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
        View view = (View) unexpectedObject;

        DropViewAction action = new DropViewAction();
        action.setViewName(view.getName());
        if (control.getIncludeCatalog()) {
            action.setCatalogName(view.getSchema().getCatalogName());
        }
        if (control.getIncludeSchema()) {
            action.setSchemaName(view.getSchema().getName());
        }

        for (Column column : view.getColumns()) {
            control.setAlreadyHandledUnexpected(column);
        };


        return new ExecutableChange[]{action};


    }
}
