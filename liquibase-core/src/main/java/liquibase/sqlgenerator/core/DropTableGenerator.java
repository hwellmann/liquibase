package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.OracleDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.logging.LogFactory;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.DropTableStatement;
import liquibase.structure.core.Relation;
import liquibase.structure.core.Table;

import org.kohsuke.MetaInfServices;

@MetaInfServices(SqlGenerator.class)
public class DropTableGenerator extends AbstractSqlGenerator<DropTableStatement> {

    @Override
    public ValidationErrors validate(DropTableStatement dropTableStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", dropTableStatement.getTableName());
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(DropTableStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("DROP TABLE ").append(database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()));
        if (statement.isCascadeConstraints()) {
            if (!database.supportsDropTableCascadeConstraints()) {
                LogFactory.getLogger().warning("Database does not support drop with cascade");
            } else if (database instanceof OracleDatabase) {
                buffer.append(" CASCADE CONSTRAINTS");
            } else {
                buffer.append(" CASCADE");
            }
        }

        return new Sql[]{
                new UnparsedSql(buffer.toString(), getAffectedTable(statement))
        };
    }

    protected Relation getAffectedTable(DropTableStatement statement) {
        return new Table().setName(statement.getTableName()).setSchema(statement.getCatalogName(), statement.getSchemaName());
    }
}
