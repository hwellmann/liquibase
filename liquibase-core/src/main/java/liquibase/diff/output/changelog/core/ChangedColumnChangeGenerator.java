package liquibase.diff.output.changelog.core;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import liquibase.change.ExecutableChange;
import liquibase.change.core.AddAutoIncrementChange;
import liquibase.change.core.AddDefaultValueChange;
import liquibase.change.core.AddNotNullConstraintChange;
import liquibase.change.core.DropDefaultValueChange;
import liquibase.change.core.DropNotNullConstraintChange;
import liquibase.change.core.ModifyDataTypeChange;
import liquibase.database.Database;
import liquibase.datatype.DataTypeFactory;
import liquibase.datatype.LiquibaseDataType;
import liquibase.diff.Difference;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGenerator;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.ChangedObjectChangeGenerator;
import liquibase.logging.LogFactory;
import liquibase.statement.DatabaseFunction;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.DataType;
import liquibase.structure.core.PrimaryKey;
import liquibase.structure.core.Table;
import liquibase.structure.core.View;
import liquibase.util.ISODateFormat;

import org.kohsuke.MetaInfServices;

@MetaInfServices(ChangeGenerator.class)
public class ChangedColumnChangeGenerator implements ChangedObjectChangeGenerator {
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
                Table.class
        };
    }

    @Override
    public Class<? extends DatabaseObject>[] runBeforeTypes() {
        return new Class[] {
                PrimaryKey.class
        };
    }

    @Override
    public ExecutableChange[] fixChanged(DatabaseObject changedObject, ObjectDifferences differences, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
        Column column = (Column) changedObject;
        if (column.getRelation() instanceof View) {
            return null;
        }

        if (column.getRelation().getSnapshotId() == null) { //not an actual table, maybe an alias, maybe in a different schema. Don't fix it.
            return null;
        }

        List<ExecutableChange> changes = new ArrayList<ExecutableChange>();

        handleTypeDifferences(column, differences, control, changes, referenceDatabase, comparisonDatabase);
        handleNullableDifferences(column, differences, control, changes, referenceDatabase, comparisonDatabase);
        handleDefaultValueDifferences(column, differences, control, changes, referenceDatabase, comparisonDatabase);
        handleAutoIncrementDifferences(column, differences, control, changes, referenceDatabase, comparisonDatabase);

        return changes.toArray(new ExecutableChange[changes.size()]);
    }

    protected void handleNullableDifferences(Column column, ObjectDifferences differences, DiffOutputControl control, List<ExecutableChange> changes, Database referenceDatabase, Database comparisonDatabase) {
        Difference nullableDifference = differences.getDifference("nullable");
        if (nullableDifference != null && nullableDifference.getReferenceValue() != null) {
            boolean nullable = (Boolean) nullableDifference.getReferenceValue();
            if (nullable) {
                DropNotNullConstraintChange change = new DropNotNullConstraintChange();
                if (control.getIncludeCatalog()) {
                    change.setCatalogName(column.getRelation().getSchema().getCatalog().getName());
                }
                if (control.getIncludeSchema()) {
                    change.setSchemaName(column.getRelation().getSchema().getName());
                }
                change.setTableName(column.getRelation().getName());
                change.setColumnName(column.getName());
                change.setColumnDataType(DataTypeFactory.getInstance().from(column.getType(), comparisonDatabase).toString());
                changes.add(change);
            } else {
                AddNotNullConstraintChange change = new AddNotNullConstraintChange();
                if (control.getIncludeCatalog()) {
                    change.setCatalogName(column.getRelation().getSchema().getCatalog().getName());
                }
                if (control.getIncludeSchema()) {
                    change.setSchemaName(column.getRelation().getSchema().getName());
                }
                change.setTableName(column.getRelation().getName());
                change.setColumnName(column.getName());
                change.setColumnDataType(DataTypeFactory.getInstance().from(column.getType(), comparisonDatabase).toString());
                changes.add(change);
            }
        }
    }

    protected void handleAutoIncrementDifferences(Column column, ObjectDifferences differences, DiffOutputControl control, List<ExecutableChange> changes, Database referenceDatabase, Database comparisonDatabase) {
        Difference difference = differences.getDifference("autoIncrementInformation");
        if (difference != null) {
            if (difference.getReferenceValue() == null) {
                LogFactory.getLogger().info("ChangedColumnChangeGenerator cannot fix dropped auto increment values");
                //todo: Support dropping auto increments
            } else {
                AddAutoIncrementChange change = new AddAutoIncrementChange();
                if (control.getIncludeCatalog()) {
                    change.setCatalogName(column.getRelation().getSchema().getCatalog().getName());
                }
                if (control.getIncludeSchema()) {
                    change.setSchemaName(column.getRelation().getSchema().getName());
                }
                change.setTableName(column.getRelation().getName());
                change.setColumnName(column.getName());
                change.setColumnDataType(DataTypeFactory.getInstance().from(column.getType(), comparisonDatabase).toString());
                changes.add(change);
            }
        }
    }

    protected void handleTypeDifferences(Column column, ObjectDifferences differences, DiffOutputControl control, List<ExecutableChange> changes, Database referenceDatabase, Database comparisonDatabase) {
        Difference typeDifference = differences.getDifference("type");
        if (typeDifference != null) {
            ModifyDataTypeChange change = new ModifyDataTypeChange();
            if (control.getIncludeCatalog()) {
                change.setCatalogName(column.getRelation().getSchema().getCatalog().getName());
            }
            if (control.getIncludeSchema()) {
                change.setSchemaName(column.getRelation().getSchema().getName());
            }
            change.setTableName(column.getRelation().getName());
            change.setColumnName(column.getName());
            DataType referenceType = (DataType) typeDifference.getReferenceValue();
            change.setNewDataType(DataTypeFactory.getInstance().from(referenceType, comparisonDatabase).toString());

            changes.add(change);
        }
    }

    protected void handleDefaultValueDifferences(Column column, ObjectDifferences differences, DiffOutputControl control, List<ExecutableChange> changes, Database referenceDatabase, Database comparisonDatabase) {
        Difference difference = differences.getDifference("defaultValue");

        if (difference != null) {
            Object value = difference.getReferenceValue();

            LiquibaseDataType columnDataType = DataTypeFactory.getInstance().from(column.getType(), comparisonDatabase);
            if (value == null) {
                DropDefaultValueChange change = new DropDefaultValueChange();
                if (control.getIncludeCatalog()) {
                    change.setCatalogName(column.getRelation().getSchema().getCatalog().getName());
                }
                if (control.getIncludeSchema()) {
                    change.setSchemaName(column.getRelation().getSchema().getName());
                }
                change.setTableName(column.getRelation().getName());
                change.setColumnName(column.getName());
                change.setColumnDataType(columnDataType.toString());

                changes.add(change);

            } else {
                AddDefaultValueChange change = new AddDefaultValueChange();
                if (control.getIncludeCatalog()) {
                    change.setCatalogName(column.getRelation().getSchema().getCatalog().getName());
                }
                if (control.getIncludeSchema()) {
                    change.setSchemaName(column.getRelation().getSchema().getName());
                }
                change.setTableName(column.getRelation().getName());
                change.setColumnName(column.getName());
                change.setColumnDataType(columnDataType.toString());

                if (value instanceof Boolean) {
                    change.setDefaultValueBoolean((Boolean) value);
                } else if (value instanceof Date) {
                    change.setDefaultValueDate(new ISODateFormat().format(((Date) value)));
                } else if (value instanceof Number) {
                    change.setDefaultValueNumeric(value.toString());
                } else if (value instanceof DatabaseFunction) {
                    change.setDefaultValueComputed(((DatabaseFunction) value));
                } else {
                    change.setDefaultValue(value.toString());
                }


                changes.add(change);
            }
        }
    }
}
