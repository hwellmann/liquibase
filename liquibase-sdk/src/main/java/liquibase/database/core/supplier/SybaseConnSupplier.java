package liquibase.database.core.supplier;

import java.util.Map;

import liquibase.sdk.supplier.database.ConnectionSupplier;

import org.kohsuke.MetaInfServices;

@MetaInfServices(ConnectionSupplier.class)
public class SybaseConnSupplier extends ConnectionSupplier {
    @Override
    public String getDatabaseShortName() {
        return "sybase";
    }

    @Override
    public ConfigTemplate getPuppetTemplate(Map<String, Object> context) {
        return null;
    }


    @Override
    public String getAdminUsername() {
        return "sa";
    }

    @Override
    public String getJdbcUrl() {
        return "jdbc:sybase:Tds:"+ getIpAddress()+":5000/liquibase";
    }
}
