package liquibase.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeStatus;
import liquibase.change.ChangeWithColumns;
import liquibase.change.ColumnConfig;
import liquibase.change.DatabaseChange;
import liquibase.change.ExecutableChange;
import liquibase.change.core.DropColumnChange;
import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.database.core.SQLiteDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropColumnStatement;
import liquibase.statement.core.ReorganizeTableStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.Index;
import liquibase.structure.core.Table;
import liquibase.util.StringUtils;

import org.kohsuke.MetaInfServices;

/**
 * Drops an existing column from a table.
 */
@DatabaseChange(name = "dropColumn", description = "Drop existing column(s)", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "column")
@MetaInfServices(ExecutableChange.class)
public class DropColumnAction extends AbstractAction<DropColumnChange>  implements ChangeWithColumns<ColumnConfig> {
    
    
    public DropColumnAction() {
       this(new DropColumnChange());
    }


    public DropColumnAction(DropColumnChange change) {
        super(change);
    }

    @Override
    public boolean generateStatementsVolatile(Database database) {
        if (database instanceof SQLiteDatabase) {
            return true;
        }
        return super.generateStatementsVolatile(database);
    }

    @Override
    public boolean supports(Database database) {
        if (database instanceof SQLiteDatabase) {
            return true;
        }
        return super.supports(database);
    }

    @Override
    public ValidationErrors validate(Database database) {
        if (database instanceof SQLiteDatabase) {
            ValidationErrors validationErrors = new ValidationErrors();
            validationErrors.checkRequiredField("tableName", change.getTableName());
            validationErrors.checkRequiredField("columnName", change.getColumnName());

            return validationErrors;
        }
        return super.validate(database);
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        if (isMultiple()) {
            return generateMultipeColumns(database);
        }
        else {
            return generateSingleColumn(database);
        }
    }

    private SqlStatement[] generateMultipeColumns(Database database) {
        List<SqlStatement> statements = new ArrayList<SqlStatement>();
        List<DropColumnStatement> dropStatements = new ArrayList<DropColumnStatement>();

        for (ColumnConfig column : change.getColumns()) {
            if (database instanceof SQLiteDatabase) {
                statements.addAll(Arrays.asList(generateStatementsForSQLiteDatabase(database,
                    column.getName())));
            }
            else {
                dropStatements.add(new DropColumnStatement(getCatalogName(), getSchemaName(),
                    getTableName(), column.getName()));
            }
        }

        if (dropStatements.size() == 1) {
            statements.add(dropStatements.get(0));
        }
        else {
            statements.add(new DropColumnStatement(dropStatements));
        }

        if (database instanceof DB2Database) {
            statements.add(new ReorganizeTableStatement(getCatalogName(), getSchemaName(),
                getTableName()));
        }

        return statements.toArray(new SqlStatement[statements.size()]);
    }

    private SqlStatement[] generateSingleColumn(Database database) {
        if (database instanceof SQLiteDatabase) {
            // return special statements for SQLite databases
            return generateStatementsForSQLiteDatabase(database, getColumnName());
        }

        List<SqlStatement> statements = new ArrayList<SqlStatement>();

        statements.add(new DropColumnStatement(getCatalogName(), getSchemaName(), getTableName(),
            getColumnName()));
        if (database instanceof DB2Database) {
            statements.add(new ReorganizeTableStatement(getCatalogName(), getSchemaName(),
                getTableName()));
        }

        return statements.toArray(new SqlStatement[statements.size()]);
    }

    @Override
    public ChangeStatus checkStatus(Database database) {
        try {
            return new ChangeStatus().assertComplete(
                !SnapshotGeneratorFactory.getInstance().has(
                    new Column(Table.class, getCatalogName(), getSchemaName(), getTableName(),
                        getColumnName()), database), "Column exists");
        }
        catch (Exception e) {
            return new ChangeStatus().unknown(e);
        }

    }

    private SqlStatement[] generateStatementsForSQLiteDatabase(Database database,
        final String columnName) {

        // SQLite does not support this ALTER TABLE operation until now.
        // For more information see: http://www.sqlite.org/omitted.html.
        // This is a small work around...

        List<SqlStatement> statements = new ArrayList<SqlStatement>();

        // define alter table logic
        SQLiteDatabase.AlterTableVisitor rename_alter_visitor = new SQLiteDatabase.AlterTableVisitor() {

            @Override
            public ColumnConfig[] getColumnsToAdd() {
                return new ColumnConfig[0];
            }

            @Override
            public boolean createThisColumn(ColumnConfig column) {
                return !column.getName().equals(columnName);
            }

            @Override
            public boolean copyThisColumn(ColumnConfig column) {
                return !column.getName().equals(columnName);
            }

            @Override
            public boolean createThisIndex(Index index) {
                return !index.getColumns().contains(columnName);
            }
        };

        try {
            // alter table
            statements.addAll(SQLiteDatabase.getAlterTableStatements(rename_alter_visitor,
                database, getCatalogName(), getSchemaName(), getTableName()));

        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return statements.toArray(new SqlStatement[statements.size()]);
    }

    @Override
    public String getConfirmationMessage() {
        if (isMultiple()) {
            List<ColumnConfig> columns = change.getColumns();
            List<String> names = new ArrayList<String>(columns.size());
            for (ColumnConfig column : columns) {
                names.add(column.getName());
            }
            return "Columns " + StringUtils.join(names, ",") + " dropped from " + getTableName();
        }
        else {
            return "Column " + getTableName() + "." + getColumnName() + " dropped";
        }
    }


    private boolean isMultiple() {
        List<ColumnConfig> columns = change.getColumns();
        return columns != null && !columns.isEmpty();
    }

    
    /**
     * @return the catalogName
     */
    public String getCatalogName() {
        return change.getCatalogName();
    }

    
    /**
     * @return the columnName
     */
    public String getColumnName() {
        return change.getColumnName();
    }

    
    /**
     * @return the schemaName
     */
    public String getSchemaName() {
        return change.getSchemaName();
    }

    
    /**
     * @return the tableName
     */
    public String getTableName() {
        return change.getTableName();
    }


    @Override
    public void addColumn(ColumnConfig column) {
        change.addColumn(column);
        
    }


    @Override
    public List<ColumnConfig> getColumns() {
        return change.getColumns();
    }


    @Override
    public void setColumns(List<ColumnConfig> columns) {
        change.setColumns(columns);
    }
}
