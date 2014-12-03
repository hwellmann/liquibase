package liquibase.database.core.supplier;

import java.util.Map;

import liquibase.sdk.supplier.database.ConnectionSupplier;

import org.kohsuke.MetaInfServices;

@MetaInfServices(ConnectionSupplier.class)
public class SQLiteConnSupplier extends ConnectionSupplier {
    @Override
    public String getDatabaseShortName() {
        return "sqlite";
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
        return "jdbc:sqlite:sqlite/liquibase.db";
    }
}
