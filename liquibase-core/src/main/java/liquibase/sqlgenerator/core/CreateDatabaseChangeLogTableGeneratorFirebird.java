package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.FirebirdDatabase;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.statement.core.CreateDatabaseChangeLogTableStatement;

import org.kohsuke.MetaInfServices;

@MetaInfServices(SqlGenerator.class)
public class CreateDatabaseChangeLogTableGeneratorFirebird extends CreateDatabaseChangeLogTableGenerator {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(CreateDatabaseChangeLogTableStatement statement, Database database) {
        return database instanceof FirebirdDatabase;
    }
}
