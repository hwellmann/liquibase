package liquibase.database.core.supplier;

import java.util.Map;

import liquibase.sdk.supplier.database.ConnectionSupplier;

import org.kohsuke.MetaInfServices;

@MetaInfServices(ConnectionSupplier.class)
public class FirebirdConnSupplier extends ConnectionSupplier {
    @Override
    public String getDatabaseShortName() {
        return "firebird";
    }

    @Override
    public ConfigTemplate getPuppetTemplate(Map<String, Object> context) {
        return null;
    }


    @Override
    public String getAdminUsername() {
        return null;
    }

    @Override
    public String getJdbcUrl() {
        return "jdbc:firebirdsql:"+ getDatabaseShortName() +"/3050:c:\\firebird\\liquibase.fdb";
    }
}
