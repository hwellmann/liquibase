package liquibase.diff.output.changelog.core;

import liquibase.action.CreateViewAction;
import liquibase.change.ExecutableChange;
import liquibase.database.Database;
import liquibase.database.core.OracleDatabase;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGenerator;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.MissingObjectChangeGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;
import liquibase.structure.core.View;
import liquibase.util.StringUtils;

import org.kohsuke.MetaInfServices;

@MetaInfServices(ChangeGenerator.class)
public class MissingViewChangeGenerator implements MissingObjectChangeGenerator {
    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (View.class.isAssignableFrom(objectType)) {
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
        return null;
    }

    @Override
    public ExecutableChange[] fixMissing(DatabaseObject missingObject, DiffOutputControl control, Database referenceDatabase, final Database comparisonDatabase, ChangeGeneratorChain chain) {
        View view = (View) missingObject;

        CreateViewAction action = new CreateViewAction();
        action.setViewName(view.getName());
        if (control.getIncludeCatalog()) {
            action.setCatalogName(view.getSchema().getCatalogName());
        }
        if (control.getIncludeSchema()) {
            action.setSchemaName(view.getSchema().getName());
        }
        String selectQuery = view.getDefinition();
        boolean fullDefinitionOverridden = false;
        if (selectQuery == null) {
            selectQuery = "COULD NOT DETERMINE VIEW QUERY";
        } else if (comparisonDatabase instanceof OracleDatabase && view.getColumns() != null && view.getColumns().size() > 0) {
            String viewName;
            if (action.getCatalogName() == null && action.getSchemaName() == null) {
                viewName = comparisonDatabase.escapeObjectName(action.getViewName(), View.class);
            } else {
                viewName = comparisonDatabase.escapeViewName(action.getCatalogName(), action.getSchemaName(), action.getViewName());
            }
            selectQuery = "CREATE OR REPLACE FORCE VIEW "+ viewName
                    + " (" + StringUtils.join(view.getColumns(), ", ", new StringUtils.StringUtilsFormatter() {
                @Override
                public String toString(Object obj) {
                    if (((Column) obj).getComputed() != null && ((Column) obj).getComputed()) {
                        return ((Column) obj).getName();
                    } else {
                        return comparisonDatabase.escapeColumnName(null, null, null, ((Column) obj).getName(), false);
                    }
                }
            }) + ") AS "+selectQuery;
            action.setFullDefinition(true);
            fullDefinitionOverridden = true;

        }
        action.setSelectQuery(selectQuery);
        if (!fullDefinitionOverridden) {
            action.setFullDefinition(view.getContainsFullDefinition());
        }

        return new ExecutableChange[] { action };

    }
}
