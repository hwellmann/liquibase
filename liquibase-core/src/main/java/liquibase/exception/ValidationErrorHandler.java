package liquibase.exception;

import liquibase.database.Database;


public class ValidationErrorHandler {


    private ValidationErrors errors;

    public ValidationErrorHandler(ValidationErrors errors) {
        this.errors = errors;
    }

    public void checkDisallowedField(String disallowedFieldName, Object value, Database database, Class<? extends Database>... disallowedDatabases) {
        boolean isDisallowed = false;
        if (disallowedDatabases == null || disallowedDatabases.length == 0) {
            isDisallowed = true;
        } else {
            for (Class<? extends Database> databaseClass : disallowedDatabases) {
                if (databaseClass.isAssignableFrom(database.getClass())) {
                    isDisallowed = true;
                }
            }
        }

        if (isDisallowed && value != null) {
            errors.addError(disallowedFieldName + " is not allowed on "+(database == null?"unknown":database.getShortName()));
        }
    }

}
