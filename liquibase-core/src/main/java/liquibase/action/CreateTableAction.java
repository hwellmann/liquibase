package liquibase.action;

import java.util.ArrayList;
import java.util.List;

import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeStatus;
import liquibase.change.ChangeWithColumns;
import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.change.DatabaseChange;
import liquibase.change.ExecutableChange;
import liquibase.change.core.CreateTableChange;
import liquibase.change.core.DropTableChange;
import liquibase.database.Database;
import liquibase.database.core.MySQLDatabase;
import liquibase.datatype.DataTypeFactory;
import liquibase.datatype.LiquibaseDataType;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.AutoIncrementConstraint;
import liquibase.statement.ForeignKeyConstraint;
import liquibase.statement.NotNullConstraint;
import liquibase.statement.SqlStatement;
import liquibase.statement.UniqueConstraint;
import liquibase.statement.core.CreateTableStatement;
import liquibase.statement.core.SetColumnRemarksStatement;
import liquibase.statement.core.SetTableRemarksStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.PrimaryKey;
import liquibase.structure.core.Table;
import liquibase.util.StringUtils;

import org.kohsuke.MetaInfServices;

/**
 * Creates a new table.
 */
@DatabaseChange(name="createTable", description = "Create Table", priority = ChangeMetaData.PRIORITY_DEFAULT)
@MetaInfServices(ExecutableChange.class)
public class CreateTableAction extends AbstractAction<CreateTableChange> implements ChangeWithColumns<ColumnConfig> {

    public CreateTableAction() {
        this(new CreateTableChange());
    }

    public CreateTableAction(CreateTableChange change) {
        super(change);
    }



    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.addAll(super.validate(database));

        if (getColumns() != null) {
            for (ColumnConfig columnConfig : getColumns()) {
                if (columnConfig.getType() == null) {
                    validationErrors.addError("column 'type' is required for all columns");
                }
                if (columnConfig.getName() == null) {
                    validationErrors.addError("column 'name' is required for all columns");
                }
            }
        }
        return validationErrors;
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {

        CreateTableStatement statement = generateCreateTableStatement();
        for (ColumnConfig column : getColumns()) {
            ConstraintsConfig constraints = column.getConstraints();
            boolean isAutoIncrement = column.isAutoIncrement() != null && column.isAutoIncrement();

            Object defaultValue = column.getDefaultValueObject();

            LiquibaseDataType columnType = DataTypeFactory.getInstance().fromDescription(column.getType() + (isAutoIncrement ? "{autoIncrement:true}" : ""), database);
            if (constraints != null && constraints.isPrimaryKey() != null && constraints.isPrimaryKey()) {

                statement.addPrimaryKeyColumn(column.getName(), columnType, defaultValue, constraints.getPrimaryKeyName(), constraints.getPrimaryKeyTablespace());

            } else {
                statement.addColumn(column.getName(),
                        columnType,
                        defaultValue,
                        column.getRemarks());
            }


            if (constraints != null) {
                if (constraints.isNullable() != null && !constraints.isNullable()) {
                    statement.addColumnConstraint(new NotNullConstraint(column.getName()));
                }

                if (constraints.getReferences() != null ||
                        (constraints.getReferencedTableName() != null && constraints.getReferencedColumnNames() != null)) {
                    if (StringUtils.trimToNull(constraints.getForeignKeyName()) == null) {
                        throw new UnexpectedLiquibaseException("createTable with references requires foreignKeyName");
                    }
                    ForeignKeyConstraint fkConstraint = new ForeignKeyConstraint(constraints.getForeignKeyName(),
                            constraints.getReferences(), constraints.getReferencedTableName(), constraints.getReferencedColumnNames());
                    fkConstraint.setColumn(column.getName());
                    fkConstraint.setDeleteCascade(constraints.isDeleteCascade() != null && constraints.isDeleteCascade());
                    fkConstraint.setInitiallyDeferred(constraints.isInitiallyDeferred() != null && constraints.isInitiallyDeferred());
                    fkConstraint.setDeferrable(constraints.isDeferrable() != null && constraints.isDeferrable());
                    statement.addColumnConstraint(fkConstraint);
                }

                if (constraints.isUnique() != null && constraints.isUnique()) {
                    statement.addColumnConstraint(new UniqueConstraint(constraints.getUniqueConstraintName()).addColumns(column.getName()));
                }
            }

            if (isAutoIncrement) {
                statement.addColumnConstraint(new AutoIncrementConstraint(column.getName(), column.getStartWith(), column.getIncrementBy()));
            }
        }

        statement.setTablespace(StringUtils.trimToNull(getTablespace()));

        List<SqlStatement> statements = new ArrayList<SqlStatement>();
        statements.add(statement);

        if (StringUtils.trimToNull(getRemarks()) != null) {
            SetTableRemarksStatement remarksStatement = new SetTableRemarksStatement(getCatalogName(), getSchemaName(), getTableName(), getRemarks());
            if (SqlGeneratorFactory.getInstance().supports(remarksStatement, database)) {
                statements.add(remarksStatement);
            }
        }

        for (ColumnConfig column : getColumns()) {
            String columnRemarks = StringUtils.trimToNull(column.getRemarks());
            if (columnRemarks != null) {
                SetColumnRemarksStatement remarksStatement = new SetColumnRemarksStatement(getCatalogName(), getSchemaName(), getTableName(), column.getName(), columnRemarks);
                if (!(database instanceof MySQLDatabase) && SqlGeneratorFactory.getInstance().supports(remarksStatement, database)) {
                    statements.add(remarksStatement);
                }
            }
        }

        return statements.toArray(new SqlStatement[statements.size()]);
    }

    protected CreateTableStatement generateCreateTableStatement() {
        return new CreateTableStatement(getCatalogName(), getSchemaName(), getTableName(),getRemarks());
    }

    @Override
    protected ExecutableChange[] createInverses() {
        DropTableChange inverse = new DropTableChange();
        inverse.setCatalogName(getCatalogName());
        inverse.setSchemaName(getSchemaName());
        inverse.setTableName(getTableName());

        return new ExecutableChange[]{
                inverse
        };
    }

    @Override
    public ChangeStatus checkStatus(Database database) {
        try {
            Table example = (Table) new Table().setName(getTableName()).setSchema(getCatalogName(), getSchemaName());
            ChangeStatus status = new ChangeStatus();
            Table tableSnapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(example, database);
            status.assertComplete(tableSnapshot != null, "Table does not exist");

            if (tableSnapshot != null) {
                for (ColumnConfig columnConfig : getColumns()) {
                    Column columnSnapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(new Column(columnConfig).setRelation(tableSnapshot), database);
                    status.assertCorrect(columnSnapshot != null, "Column "+columnConfig.getName()+" is missing");
                    if (columnSnapshot != null) {
                        ConstraintsConfig constraints = columnConfig.getConstraints();
                        if (constraints != null) {
                            if (constraints.isPrimaryKey() != null && constraints.isPrimaryKey()) {
                                PrimaryKey tablePk = tableSnapshot.getPrimaryKey();
                                status.assertCorrect(tablePk != null && tablePk.getColumnNamesAsList().contains(columnConfig.getName()), "Column "+columnConfig.getName()+" is not part of the primary key");
                            }
                            if (constraints.isNullable() != null) {
                                if (constraints.isNullable()) {
                                    status.assertCorrect(columnSnapshot.isNullable() == null || columnSnapshot.isNullable(), "Column "+columnConfig.getName()+" nullability does not match");
                                } else {
                                    status.assertCorrect(columnSnapshot.isNullable() != null && !columnSnapshot.isNullable(), "Column "+columnConfig.getName()+" nullability does not match");
                                }
                            }
                        }
                    }
                }
            }

            return status;
        } catch (Exception e) {
            return new ChangeStatus().unknown(e);
        }
    }

    @Override
    public List<ColumnConfig> getColumns() {
        return change.getColumns();
    }

    @Override
    public void setColumns(List<ColumnConfig> columns) {
        change.setColumns(columns);
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


    public String getTablespace() {
        return change.getTablespace();
    }

    public void setTablespace(String tablespace) {
        change.setTablespace(tablespace);;
    }

    @Override
    public void addColumn(ColumnConfig column) {
        change.addColumn(column);;
    }

    public String getRemarks() {
        return change.getRemarks();
    }

    public void setRemarks(String remarks) {
        change.setRemarks(remarks);
    }

    @Override
    public String getConfirmationMessage() {
        return "Table " + getTableName() + " created";
    }

}
