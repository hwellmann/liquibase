package liquibase.diff.output.changelog.core;

import liquibase.action.CreateSequenceAction;
import liquibase.change.ExecutableChange;
import liquibase.database.Database;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGenerator;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.MissingObjectChangeGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Sequence;

import org.kohsuke.MetaInfServices;

@MetaInfServices(ChangeGenerator.class)
public class MissingSequenceChangeGenerator implements MissingObjectChangeGenerator {

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (Sequence.class.isAssignableFrom(objectType)) {
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
        return null;
    }

    @Override
    public ExecutableChange[] fixMissing(DatabaseObject missingObject, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
        Sequence sequence = (Sequence) missingObject;

        CreateSequenceAction action = new CreateSequenceAction();
        action.setSequenceName(sequence.getName());
        if (control.getIncludeCatalog()) {
            action.setCatalogName(sequence.getSchema().getCatalogName());
        }
        if (control.getIncludeSchema()) {
            action.setSchemaName(sequence.getSchema().getName());
        }
        action.setStartValue(sequence.getStartValue());
        action.setIncrementBy(sequence.getIncrementBy());
        action.setMinValue(sequence.getMinValue());
        action.setMaxValue(sequence.getMaxValue());
        action.setCacheSize(sequence.getCacheSize());
        action.setCycle(sequence.getWillCycle());
        action.setOrdered(sequence.getOrdered());

        return new ExecutableChange[] { action };

    }
}
