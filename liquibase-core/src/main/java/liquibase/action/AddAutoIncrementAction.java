package liquibase.action;

import java.math.BigInteger;

import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeStatus;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeNote;
import liquibase.change.ExecutableChange;
import liquibase.change.core.AddAutoIncrementChange;
import liquibase.database.Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.SequenceNextValueFunction;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AddAutoIncrementStatement;
import liquibase.statement.core.AddDefaultValueStatement;
import liquibase.statement.core.CreateSequenceStatement;
import liquibase.statement.core.SetNullableStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;

import org.kohsuke.MetaInfServices;

/**
 * Makes an existing column into an auto-increment column.
 * This change is only valid for databases with auto-increment/identity columns.
 * The current version does not support MS-SQL.
 */
@DatabaseChange(name="addAutoIncrement", description = "Converts an existing column to be an auto-increment (a.k.a 'identity') column",
        priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "column",
        databaseNotes = {@DatabaseChangeNote(database = "sqlite", notes = "If the column type is not INTEGER it is converted to INTEGER")}
)
@MetaInfServices(ExecutableChange.class)
public class AddAutoIncrementAction extends AbstractAction<AddAutoIncrementChange> {

    public AddAutoIncrementAction() {
        this(new AddAutoIncrementChange());
    }

    public AddAutoIncrementAction(AddAutoIncrementChange change) {
        super(change);
    }

    public String getCatalogName() {
        return change.getCatalogName();
    }

    public void setCatalogName(String catalogName) {
        change.setCatalogName(catalogName);
    }

    public String getSchemaName() {
        return change.getSchemaName();
    }

    public void setSchemaName(String schemaName) {
        change.setSchemaName(schemaName);
    }

    public String getTableName() {
        return change.getTableName();
    }

    public void setTableName(String tableName) {
        change.setTableName(tableName);
    }

    public String getColumnName() {
        return change.getColumnName();
    }

    public void setColumnName(String columnName) {
        change.setColumnName(columnName);
    }

    public String getColumnDataType() {
        return change.getColumnDataType();
    }

    public void setColumnDataType(String columnDataType) {
        change.setColumnDataType(columnDataType);
    }

    public BigInteger getStartWith() {
    	return change.getStartWith();
    }

    public void setStartWith(BigInteger startWith) {
    	change.setStartWith(startWith);
    }

    public BigInteger getIncrementBy() {
    	return change.getIncrementBy();
    }

    public void setIncrementBy(BigInteger incrementBy) {
    	change.setIncrementBy(incrementBy);
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        if (database instanceof PostgresDatabase) {
            String sequenceName = (getTableName() + "_" + getColumnName() + "_seq").toLowerCase();
            return new SqlStatement[]{
                    new CreateSequenceStatement(getCatalogName(), getSchemaName(), sequenceName),
                    new SetNullableStatement(getCatalogName(), getSchemaName(), getTableName(), getColumnName(), null, false),
                    new AddDefaultValueStatement(getCatalogName(), getSchemaName(), getTableName(), getColumnName(), getColumnDataType(), new SequenceNextValueFunction(sequenceName)),
            };
        }

        return new SqlStatement[]{new AddAutoIncrementStatement(getCatalogName(), getSchemaName(), getTableName(), getColumnName(), getColumnDataType(), getStartWith(), getIncrementBy())};
    }

    @Override
    public String getConfirmationMessage() {
        return "Auto-increment added to " + getTableName() + "." + getColumnName();
    }

    @Override
    public ChangeStatus checkStatus(Database database) {
        ChangeStatus result = new ChangeStatus();
        Column example = new Column(Table.class, getCatalogName(), getSchemaName(), getTableName(), getColumnName());
        try {
            Column column = SnapshotGeneratorFactory.getInstance().createSnapshot(example, database);
            if (column == null) return result.unknown("Column does not exist");


            result.assertComplete(column.isAutoIncrement(), "Column is not auto-increment");
            if (getStartWith() != null && column.getAutoIncrementInformation().getStartWith() != null) {
                result.assertCorrect(getStartWith().equals(column.getAutoIncrementInformation().getStartWith()), "startsWith incorrect");
            }

            if (getIncrementBy() != null && column.getAutoIncrementInformation().getIncrementBy() != null) {
                result.assertCorrect(getIncrementBy().equals(column.getAutoIncrementInformation().getIncrementBy()), "Increment by incorrect");
            }

            return result;
        } catch (Exception e) {
            return result.unknown(e);

        }


    }
}
