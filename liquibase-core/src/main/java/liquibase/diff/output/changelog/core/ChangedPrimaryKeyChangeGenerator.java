package liquibase.diff.output.changelog.core;

import java.util.List;

import liquibase.action.AddPrimaryKeyAction;
import liquibase.action.DropPrimaryKeyAction;
import liquibase.change.ExecutableChange;
import liquibase.database.Database;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGenerator;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.ChangedObjectChangeGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.Index;
import liquibase.structure.core.PrimaryKey;
import liquibase.structure.core.UniqueConstraint;
import liquibase.util.StringUtils;

import org.kohsuke.MetaInfServices;

@MetaInfServices(ChangeGenerator.class)
public class ChangedPrimaryKeyChangeGenerator  implements ChangedObjectChangeGenerator {
    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (PrimaryKey.class.isAssignableFrom(objectType)) {
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
        PrimaryKey pk = (PrimaryKey) changedObject;

        DropPrimaryKeyAction dropPkAction = new DropPrimaryKeyAction();
        dropPkAction.setTableName(pk.getTable().getName());

        AddPrimaryKeyAction addPkAction = new AddPrimaryKeyAction();
        addPkAction.setTableName(pk.getTable().getName());
        addPkAction.setColumnNames(pk.getColumnNames());
        addPkAction.setConstraintName(pk.getName());


        if (control.getIncludeCatalog()) {
            dropPkAction.setCatalogName(pk.getSchema().getCatalogName());
            addPkAction.setCatalogName(pk.getSchema().getCatalogName());
        }
        if (control.getIncludeSchema()) {
            dropPkAction.setSchemaName(pk.getSchema().getName());
            addPkAction.setSchemaName(pk.getSchema().getName());
        }

        List<Column> referenceColumns = (List<Column>) differences.getDifference("columns").getReferenceValue();
        List<Column> comparedColumns = (List<Column>) differences.getDifference("columns").getComparedValue();

        StringUtils.ToStringFormatter formatter = new StringUtils.ToStringFormatter();

        control.setAlreadyHandledChanged(new Index().setTable(pk.getTable()).setColumns(referenceColumns));
        if (!StringUtils.join(referenceColumns, ",", formatter).equalsIgnoreCase(StringUtils.join(comparedColumns, ",", formatter))) {
            control.setAlreadyHandledChanged(new Index().setTable(pk.getTable()).setColumns(comparedColumns));
        }

        control.setAlreadyHandledChanged(new UniqueConstraint().setTable(pk.getTable()).setColumns(referenceColumns));
        if (!StringUtils.join(referenceColumns, ",", formatter).equalsIgnoreCase(StringUtils.join(comparedColumns, "," , formatter))) {
            control.setAlreadyHandledChanged(new UniqueConstraint().setTable(pk.getTable()).setColumns(comparedColumns));
        }

        return new ExecutableChange[] { dropPkAction, addPkAction };
    }
}
