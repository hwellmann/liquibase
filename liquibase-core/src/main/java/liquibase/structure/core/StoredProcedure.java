package liquibase.structure.core;

import liquibase.structure.DatabaseObject;

import org.kohsuke.MetaInfServices;

@MetaInfServices(DatabaseObject.class)
public class StoredProcedure extends StoredDatabaseLogic<StoredProcedure> {

    public StoredProcedure() {
    }

    public StoredProcedure(String catalogName, String schemaName, String procedureName) {
        this.setSchema(new Schema(catalogName, schemaName));
        setName(procedureName);
    }

}
