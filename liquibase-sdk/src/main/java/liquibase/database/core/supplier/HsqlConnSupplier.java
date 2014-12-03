package liquibase.database.core.supplier;

import java.util.Map;

import liquibase.sdk.supplier.database.ConnectionSupplier;

import org.kohsuke.MetaInfServices;

@MetaInfServices(ConnectionSupplier.class)
public class HsqlConnSupplier extends ConnectionSupplier {
    @Override
    public String getDatabaseShortName() {
        return "hsqldb";
    }

    @Override
    public String getAdminUsername() {
        return null;
    }

    @Override
    public ConfigTemplate getPuppetTemplate(Map<String, Object> context) {
        return null;
    }

    @Override
    public String getJdbcUrl() {
        return "jdbc:hsqldb:mem:liquibase";
    }
}
