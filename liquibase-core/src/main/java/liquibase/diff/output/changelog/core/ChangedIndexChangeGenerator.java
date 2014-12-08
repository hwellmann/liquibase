package liquibase.diff.output.changelog.core;

import java.util.ArrayList;
import java.util.List;

import liquibase.action.CreateIndexAction;
import liquibase.change.AddColumnConfig;
import liquibase.change.ExecutableChange;
import liquibase.change.core.DropIndexChange;
import liquibase.database.Database;
import liquibase.diff.Difference;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGenerator;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.ChangedObjectChangeGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.Index;
import liquibase.structure.core.UniqueConstraint;
import liquibase.util.StringUtils;

import org.kohsuke.MetaInfServices;

@MetaInfServices(ChangeGenerator.class)
public class ChangedIndexChangeGenerator implements ChangedObjectChangeGenerator {
    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (Index.class.isAssignableFrom(objectType)) {
            return PRIORITY_DEFAULT;
        }
        return PRIORITY_NONE;
    }

    @Override
    public Class<? extends DatabaseObject>[] runBeforeTypes() {
        return null;
    }

    @Override
    public Class<? extends DatabaseObject>[] runAfterTypes() {
        return null;
    }

    @Override
    public ExecutableChange[] fixChanged(DatabaseObject changedObject, ObjectDifferences differences, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
        Index index = (Index) changedObject;

        DropIndexChange dropIndexChange = new DropIndexChange();
        dropIndexChange.setTableName(index.getTable().getName());
        dropIndexChange.setIndexName(index.getName());

        CreateIndexAction addIndexAction = new CreateIndexAction();
        addIndexAction.setTableName(index.getTable().getName());
        List<AddColumnConfig> columns = new ArrayList<AddColumnConfig>();
        for (Column col : index.getColumns()) {
            columns.add(new AddColumnConfig(col));
        }
        addIndexAction.setColumns(columns);
        addIndexAction.setIndexName(index.getName());


        if (control.getIncludeCatalog()) {
            dropIndexChange.setCatalogName(index.getSchema().getCatalogName());
            addIndexAction.setCatalogName(index.getSchema().getCatalogName());
        }
        if (control.getIncludeSchema()) {
            dropIndexChange.setSchemaName(index.getSchema().getName());
            addIndexAction.setSchemaName(index.getSchema().getName());
        }

        Difference columnsDifference = differences.getDifference("columns");

        if (columns != null) {
            List<Column> referenceColumns = (List<Column>) columnsDifference.getReferenceValue();
            List<Column> comparedColumns = (List<Column>) columnsDifference.getComparedValue();

            StringUtils.StringUtilsFormatter<Column> formatter = new StringUtils.StringUtilsFormatter<Column>() {
                @Override
                public String toString(Column obj) {
                    return obj.toString(false);
                }
            };

            control.setAlreadyHandledChanged(new Index().setTable(index.getTable()).setColumns(referenceColumns));
            if (!StringUtils.join(referenceColumns, ",", formatter).equalsIgnoreCase(StringUtils.join(comparedColumns, ",", formatter))) {
                control.setAlreadyHandledChanged(new Index().setTable(index.getTable()).setColumns(comparedColumns));
            }

            if (index.isUnique() != null && index.isUnique()) {
                control.setAlreadyHandledChanged(new UniqueConstraint().setTable(index.getTable()).setColumns(referenceColumns));
                if (!StringUtils.join(referenceColumns, ",", formatter).equalsIgnoreCase(StringUtils.join(comparedColumns, ",", formatter))) {
                    control.setAlreadyHandledChanged(new UniqueConstraint().setTable(index.getTable()).setColumns(comparedColumns));
                }
            }
        }

        return new ExecutableChange[] { dropIndexChange, addIndexAction };
    }
}
